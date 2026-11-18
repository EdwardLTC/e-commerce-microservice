pub mod routes;

use axum::routing::{get, post};
use axum::Router;

use crate::state::AppState;

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/products", get(routes::get_products).post(routes::create_product))
        .route("/products/:id", get(routes::get_product_by_id))
        .route("/products/variants", get(routes::get_variants_by_ids))
        .route(
            "/products/:id/option-types",
            get(routes::get_option_types).post(routes::create_option_type),
        )
        .route(
            "/products/option-types/:optionTypeId/option-values",
            get(routes::get_option_values).post(routes::create_option_value),
        )
        .route("/products/:id/variants", post(routes::create_variant))
}
