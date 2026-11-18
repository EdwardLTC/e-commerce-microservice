use axum::extract::State;
use axum::http::StatusCode;
use axum::Json;
use chrono::Utc;
use jsonwebtoken::{encode, EncodingKey, Header};
use redis::AsyncCommands;
use uuid::Uuid;

use crate::error::AppError;
use crate::middleware::validate_auth;
use crate::state::{get_current_token, AppState, AuthClaims, LoginSession};

// Request/Response DTOs

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct LoginRequest {
    /// User email address
    pub email: String,
    /// User password
    pub password: String,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct RegisterRequest {
    /// User email address
    pub email: String,
    /// User password
    pub password: String,
    /// User display name
    pub name: String,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct ChangePasswordRequest {
    /// Current password
    pub old_password: String,
    /// New password to set
    pub new_password: String,
}

#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct AuthTokenResponse {
    /// JWT access token
    pub access_token: String,
}

#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct UserProfileResponse {
    /// User ID
    pub id: String,
    /// User display name
    pub name: String,
    /// User email address
    pub email: String,
}

// Handlers

#[utoipa::path(
    post,
    path = "/auth/login",
    tag = "Auth",
    request_body = LoginRequest,
    responses(
        (status = 200, description = "Login successful", body = AuthTokenResponse),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn login(
    State(state): State<AppState>,
    Json(login_req): Json<LoginRequest>,
) -> Result<(StatusCode, Json<AuthTokenResponse>), AppError> {
    let user = state
        .user_client
        .get_user_by_email_and_password(&login_req.email, &login_req.password)
        .await
        .map_err(|e| AppError::Internal(format!("User service error: {}", e)))?;

    let (token, jti) = create_token(&state, &user.id, &user.email);

    // Store session in Redis
    store_session(&state, &jti, &user.id, "", "").await?;

    Ok((
        StatusCode::OK,
        Json(AuthTokenResponse {
            access_token: token,
        }),
    ))
}

#[utoipa::path(
    post,
    path = "/auth/register",
    tag = "Auth",
    request_body = RegisterRequest,
    responses(
        (status = 201, description = "User registered successfully", body = UserProfileResponse),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn register(
    State(state): State<AppState>,
    Json(register_req): Json<RegisterRequest>,
) -> Result<(StatusCode, Json<UserProfileResponse>), AppError> {
    let user = state
        .user_client
        .create_user(
            &register_req.email,
            &register_req.password,
            &register_req.name,
        )
        .await
        .map_err(|e| AppError::Internal(format!("User service error: {}", e)))?;

    Ok((
        StatusCode::CREATED,
        Json(UserProfileResponse {
            id: user.id,
            name: user.name,
            email: user.email,
        }),
    ))
}

#[utoipa::path(
    post,
    path = "/auth/logout",
    tag = "Auth",
    responses(
        (status = 200, description = "Logout successful"),
        (status = 401, description = "Unauthorized", body = crate::error::ErrorResponse),
    ),
)]
pub async fn logout(State(state): State<AppState>) -> Result<StatusCode, AppError> {
    let token =
        get_current_token().ok_or_else(|| AppError::Unauthorized("Missing token".to_string()))?;
    let claims = validate_auth(&state, &token).await?;
    delete_session(&state, &claims.jti).await?;
    Ok(StatusCode::OK)
}

#[utoipa::path(
    post,
    path = "/auth/change-password",
    tag = "Auth",
    request_body = ChangePasswordRequest,
    responses(
        (status = 200, description = "Password changed successfully"),
        (status = 401, description = "Unauthorized", body = crate::error::ErrorResponse),
    ),
)]
pub async fn change_password(
    State(state): State<AppState>,
    Json(change_req): Json<ChangePasswordRequest>,
) -> Result<StatusCode, AppError> {
    let token =
        get_current_token().ok_or_else(|| AppError::Unauthorized("Missing token".to_string()))?;
    let claims = validate_auth(&state, &token).await?;
    state
        .user_client
        .change_password(
            &claims.id,
            &change_req.old_password,
            &change_req.new_password,
        )
        .await
        .map_err(|e| AppError::Internal(format!("User service error: {}", e)))?;

    invalidate_user_sessions(&state, &claims.id).await?;

    Ok(StatusCode::OK)
}

// JWT helpers

fn create_token(state: &AppState, user_id: &str, email: &str) -> (String, String) {
    let jti = Uuid::new_v4().to_string();
    let exp = Utc::now()
        .checked_add_days(chrono::Days::new(7))
        .unwrap()
        .timestamp() as usize;

    let claims = AuthClaims {
        id: user_id.to_string(),
        email: email.to_string(),
        jti: jti.clone(),
        exp,
    };

    let token = encode(
        &Header::default(),
        &claims,
        &EncodingKey::from_secret(state.config.jwt_secret.as_bytes())
            .expect("Failed to create JWT encoding key"),
    )
    .expect("Failed to create JWT token");

    (token, jti)
}

// Redis session helpers

async fn store_session(
    state: &AppState,
    jti: &str,
    user_id: &str,
    ip: &str,
    user_agent: &str,
) -> Result<(), AppError> {
    let mut conn = state
        .redis_client
        .get_multiplexed_async_connection()
        .await
        .map_err(|e| AppError::Internal(format!("Redis connection error: {}", e)))?;

    let session = LoginSession {
        jti: jti.to_string(),
        user_id: user_id.to_string(),
        status: "active".to_string(),
        ip: ip.to_string(),
        user_agent: user_agent.to_string(),
        created_at: Utc::now().to_rfc3339(),
        expired_at: Utc::now()
            .checked_add_days(chrono::Days::new(7))
            .unwrap()
            .to_rfc3339(),
    };

    let session_key = format!("auth:session:{}", jti);
    let session_ttl = 60 * 60 * 24 * 7; // 7 days

    let session_json = serde_json::to_string(&session).unwrap();
    let _: () = conn
        .set_ex(&session_key, &session_json, session_ttl)
        .await
        .map_err(|e| AppError::Internal(format!("Redis set error: {}", e)))?;

    let user_sessions_key = format!("user:sessions:{}", user_id);
    let _: () = conn
        .sadd(&user_sessions_key, &session_key)
        .await
        .map_err(|e| AppError::Internal(format!("Redis sadd error: {}", e)))?;

    let _: () = conn
        .expire(&user_sessions_key, session_ttl as i64)
        .await
        .map_err(|e| AppError::Internal(format!("Redis expire error: {}", e)))?;

    Ok(())
}

async fn delete_session(state: &AppState, jti: &str) -> Result<(), AppError> {
    let session = get_session(state, jti).await?;
    let mut conn = state
        .redis_client
        .get_multiplexed_async_connection()
        .await
        .map_err(|e| AppError::Internal(format!("Redis connection error: {}", e)))?;

    let session_key = format!("auth:session:{}", jti);
    let _: () = conn
        .del(&session_key)
        .await
        .map_err(|e| AppError::Internal(format!("Redis del error: {}", e)))?;

    if let Some(session) = session {
        let user_sessions_key = format!("user:sessions:{}", session.user_id);
        let _: () = conn
            .srem(&user_sessions_key, &session_key)
            .await
            .map_err(|e| AppError::Internal(format!("Redis srem error: {}", e)))?;
    }

    Ok(())
}

async fn get_session(state: &AppState, jti: &str) -> Result<Option<LoginSession>, AppError> {
    let mut conn = state
        .redis_client
        .get_multiplexed_async_connection()
        .await
        .map_err(|e| AppError::Internal(format!("Redis connection error: {}", e)))?;

    let session_key = format!("auth:session:{}", jti);
    let session_json: Option<String> = conn
        .get(&session_key)
        .await
        .map_err(|e| AppError::Internal(format!("Redis get error: {}", e)))?;

    match session_json {
        Some(json) => {
            let session: LoginSession = serde_json::from_str(&json)
                .map_err(|e| AppError::Internal(format!("Session parse error: {}", e)))?;
            Ok(Some(session))
        }
        None => Ok(None),
    }
}

async fn invalidate_user_sessions(state: &AppState, user_id: &str) -> Result<(), AppError> {
    let mut conn = state
        .redis_client
        .get_multiplexed_async_connection()
        .await
        .map_err(|e| AppError::Internal(format!("Redis connection error: {}", e)))?;

    let user_sessions_key = format!("user:sessions:{}", user_id);
    let sessions: Vec<String> = conn
        .smembers(&user_sessions_key)
        .await
        .map_err(|e| AppError::Internal(format!("Redis smembers error: {}", e)))?;

    if !sessions.is_empty() {
        let _: () = conn
            .del::<Vec<&str>, ()>(sessions.iter().map(|s| s.as_str()).collect())
            .await
            .map_err(|e| AppError::Internal(format!("Redis del error: {}", e)))?;
    }

    let _: () = conn
        .del(&user_sessions_key)
        .await
        .map_err(|e| AppError::Internal(format!("Redis del error: {}", e)))?;

    Ok(())
}
