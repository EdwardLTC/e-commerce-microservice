pub mod dto;
pub mod routes;

use axum::routing::post;
use axum::Router;

use crate::state::AppState;

pub fn router() -> Router<AppState> {
    Router::new()
        // Public routes
        .route("/auth/login", post(routes::login))
        .route("/auth/register", post(routes::register))
        // Protected routes
        .route("/auth/logout", post(routes::logout))
        .route("/auth/change-password", post(routes::change_password))
}
