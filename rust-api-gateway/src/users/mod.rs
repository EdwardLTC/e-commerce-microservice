pub mod routes;
mod dto;

use axum::routing::get;
use axum::Router;

use crate::state::AppState;

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/users/me", get(routes::get_current_user))
        .route("/users/{id}", get(routes::get_user_by_id))
}
