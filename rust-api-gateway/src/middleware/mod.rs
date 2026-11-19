use jsonwebtoken::{decode, Algorithm, DecodingKey, Validation};
use redis::AsyncCommands;
use tracing::debug;

use crate::error::AppError;
use crate::state::{AppState, AuthClaims, LoginSession};

/// Validate the Authorization header and return AuthClaims.
/// Called directly by each protected handler.
pub async fn validate_auth(state: &AppState, token: &str) -> Result<AuthClaims, AppError> {
    // Verify JWT token
    let mut validation = Validation::default();
    validation.algorithms = vec![Algorithm::HS256];

    let claims = decode::<AuthClaims>(
        token,
        &DecodingKey::from_secret(state.config.jwt_secret.as_bytes()).map_err(|e| {
            debug!("Failed to create JWT decoding key: {}", e);
            AppError::Unauthorized("Invalid JWT configuration".to_string())
        })?,
        &validation,
    )
    .map_err(|e| {
        debug!("JWT validation failed: {}", e);
        AppError::Unauthorized("Invalid or expired token".to_string())
    })?
    .claims;

    // Check session in Redis
    let session = get_session(state, &claims.jit).await?;

    let session = session.ok_or_else(|| AppError::Unauthorized("Session not found".to_string()))?;

    if session.status != "active" {
        return Err(AppError::Unauthorized("Session is not active".to_string()));
    }

    Ok(claims)
}

async fn get_session(state: &AppState, jit: &str) -> Result<Option<LoginSession>, AppError> {
    let mut conn = state
        .redis_client
        .get_multiplexed_async_connection()
        .await
        .map_err(|e| AppError::Internal(format!("Redis connection error: {}", e)))?;

    let session_key = format!("auth:session:{}", jit);
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
