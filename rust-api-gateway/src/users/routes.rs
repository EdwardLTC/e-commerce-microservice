use axum::extract::{Path, State};
use axum::http::StatusCode;
use axum::Json;

use crate::error::AppError;
use crate::middleware::AuthenticatedUser;
use crate::state::AppState;
use crate::users::dto::*;

#[utoipa::path(
    get,
    path = "/users/me",
    tag = "Users",
    responses(
        (status = 200, description = "Current user profile", body = UserProfileResponse),
        (status = 401, description = "Unauthorized", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_current_user(
    State(state): State<AppState>,
    AuthenticatedUser(claims): AuthenticatedUser,
) -> Result<(StatusCode, Json<UserProfileResponse>), AppError> {
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

#[utoipa::path(
    get,
    path = "/users/:id",
    tag = "Users",
    params(
        ("id" = String, Path, description = "User ID"),
    ),
    responses(
        (status = 200, description = "User profile", body = UserProfileResponse),
        (status = 401, description = "Unauthorized", body = crate::error::ErrorResponse),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_user_by_id(
    State(state): State<AppState>,
    _auth: AuthenticatedUser,
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
