pub mod routes;

use axum::routing::get;
use axum::Router;

use crate::state::AppState;

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/orders", get(routes::get_orders).post(routes::create_order))
        .route("/orders/:id", get(routes::get_order))
}
