use axum::extract::{Path, State};
use axum::http::StatusCode;
use axum::Json;

use crate::error::AppError;
use crate::middleware::validate_auth;
use crate::state::{AppState, get_current_token};

#[derive(Debug, serde::Serialize)]
pub struct UserProfileResponse {
    pub id: String,
    pub name: String,
    pub email: String,
}

pub async fn get_current_user(
    State(state): State<AppState>,
) -> Result<(StatusCode, Json<UserProfileResponse>), AppError> {
    let token = get_current_token().ok_or_else(|| AppError::Unauthorized("Missing token".to_string()))?;
    let claims = validate_auth(&state, &token).await?;
    let user = state
        .user_client
        .get_user_by_id(&claims.id)
        .await
        .map_err(|e| AppError::Internal(format!("User service error: {}", e)))?;

    Ok((
        StatusCode::OK,
        Json(UserProfileResponse {
            id: user.id,
            name: user.name,
            email: user.email,
        }),
    ))
}

pub async fn get_user_by_id(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> Result<(StatusCode, Json<UserProfileResponse>), AppError> {
    let user = state
        .user_client
        .get_user_by_id(&id)
        .await
        .map_err(|e| AppError::Internal(format!("User service error: {}", e)))?;

    Ok((
        StatusCode::OK,
        Json(UserProfileResponse {
            id: user.id,
            name: user.name,
            email: user.email,
        }),
    ))
}
