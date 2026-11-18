mod auth;
mod config;
mod error;
mod grpc;
mod middleware;
mod orders;
mod products;
mod state;
mod users;

use std::net::SocketAddr;

use axum::middleware::from_fn;
use axum::routing::{get, post};
use axum::Router;
use axum::Json;
use tracing::info;

use crate::auth::routes::{change_password, login, logout, register};
use crate::config::AppConfig;
use crate::grpc::order_client::OrdersClient;
use crate::grpc::product_client::ProductsClient;
use crate::grpc::user_client::UsersClient;
use crate::orders::routes::{create_order, get_order, get_orders};
use crate::products::routes::{
    create_option_type, create_option_value, create_product, create_variant, get_option_types,
    get_option_values, get_product_by_id, get_products, get_variants_by_ids,
};
use crate::state::AppState;
use crate::users::routes::{get_current_user, get_user_by_id};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    
    tracing_subscriber::fmt()
        .with_env_filter(
            tracing_subscriber::EnvFilter::from_default_env()
                .add_directive("rust_api_gateway=info".parse().unwrap()),
        )
        .init();

    // Load configuration
    let config = AppConfig::from_env();
    info!("Starting API Gateway on port {}", config.port);

    // Initialize Redis client
    let redis_client = redis::Client::open(config.redis_url.clone())
        .map_err(|e| format!("Failed to create Redis client: {}", e))?;

    // Test Redis connection
    redis_client
        .get_multiplexed_async_connection()
        .await
        .map_err(|e| format!("Failed to connect to Redis: {}", e))?;
    info!("Connected to Redis");

    // Initialize gRPC clients
    let user_client = UsersClient::new(&config).await.map_err(|e| {
        info!("Warning: Could not connect to user service: {}", e);
        e
    })?;

    let product_client = ProductsClient::new(&config).await.map_err(|e| {
        info!("Warning: Could not connect to product service: {}", e);
        e
    })?;

    let order_client = OrdersClient::new(&config).await.map_err(|e| {
        info!("Warning: Could not connect to order service: {}", e);
        e
    })?;

    info!("All gRPC clients initialized");

    // Create shared state
    let app_state = AppState::new(config, redis_client, user_client, product_client, order_client);

    // Build router
    let app = Router::new()
        // Health check
        .route("/health", get(health_check))
        // Auth routes (public)
        .route("/auth/login", post(login))
        .route("/auth/register", post(register))
        // Auth routes (protected)
        .route("/auth/logout", post(logout))
        .route("/auth/change-password", post(change_password))
        // User routes
        .route("/users/me", get(get_current_user))
        .route("/users/:id", get(get_user_by_id))
        // Product routes
        .route("/products", get(get_products).post(create_product))
        .route("/products/:id", get(get_product_by_id))
        .route("/products/variants", get(get_variants_by_ids))
        .route("/products/:id/option-types", get(get_option_types).post(create_option_type))
        .route(
            "/option-types/:optionTypeId/option-values",
            get(get_option_values).post(create_option_value),
        )
        .route("/products/:id/variants", post(create_variant))
        // Order routes
        .route("/orders", get(get_orders).post(create_order))
        .route("/orders/:id", get(get_order))
        // Apply token extraction middleware
        .layer(from_fn(extract_bearer_token))
        // Share state
        .with_state(app_state.clone());

    // Start server
    let addr = SocketAddr::from(([0, 0, 0, 0], app_state.config.port));
    info!("API Gateway listening on http://{}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;

    Ok(())
}

async fn extract_bearer_token(
    req: axum::http::Request<axum::body::Body>,
    next: axum::middleware::Next,
) -> axum::response::Response {
    if let Some(auth) = req
        .headers()
        .get("authorization")
        .and_then(|v| v.to_str().ok())
        .and_then(|v| v.strip_prefix("Bearer "))
    {
        crate::state::set_current_token(auth.to_string());
    } else {
        crate::state::set_current_token(String::new());
    }
    next.run(req).await
}

async fn health_check() -> (axum::http::StatusCode, Json<serde_json::Value>) {
    (
        axum::http::StatusCode::OK,
        Json(serde_json::json!({
            "status": "ok",
            "service": "rust-api-gateway",
        })),
    )
}
