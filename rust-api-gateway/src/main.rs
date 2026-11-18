mod auth;
mod config;
mod error;
mod grpc;
mod middleware;
mod orders;
mod products;
mod state;
mod swagger;
mod users;

use std::net::SocketAddr;

use axum::middleware::from_fn;
use axum::routing::get;
use axum::Json;
use axum::Router;
use tracing::{info, warn};
use utoipa::OpenApi;

use crate::config::AppConfig;
use crate::grpc::order_client::OrdersClient;
use crate::grpc::product_client::ProductsClient;
use crate::grpc::user_client::UsersClient;
use crate::state::AppState;
use crate::swagger::docs::ApiDocs;

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

    // Initialize Redis client (lazy - no connection test at startup)
    let redis_client = match redis::Client::open(config.redis_url.clone()) {
        Ok(client) => {
            info!("Redis client configured (connection will be established lazily)");
            client
        }
        Err(e) => {
            warn!(
                "Failed to create Redis client: {}. Redis features will be unavailable.",
                e
            );
            // Create a dummy client that will fail gracefully on use
            redis::Client::open("redis://localhost:6379")
                .expect("fallback Redis client creation should not fail")
        }
    };

    // Initialize gRPC clients (lazy - no connection at startup)
    let user_client = UsersClient::new(&config);
    let product_client = ProductsClient::new(&config);
    let order_client = OrdersClient::new(&config);
    info!("gRPC clients configured (connections will be established lazily)");

    // Create shared state
    let app_state = AppState::new(
        config,
        redis_client,
        user_client,
        product_client,
        order_client,
    );

    // Build router
    let app = Router::new()
        // Health check
        .route("/health", get(health_check))
        // Swagger UI
        .merge(
            utoipa_swagger_ui::SwaggerUi::new("/swagger-ui")
                .url("/api-docs/openapi.json", ApiDocs::openapi()),
        )
        // Merge module routers
        .merge(auth::router())
        .merge(users::router())
        .merge(products::router())
        .merge(orders::router())
        // Apply token extraction middleware
        .layer(from_fn(extract_bearer_token))
        // Share state
        .with_state(app_state.clone());

    // Start server
    let addr = SocketAddr::from(([0, 0, 0, 0], app_state.config.port));
    info!("API Gateway listening on http://{}", addr);
    info!(
        "Swagger UI available at http://localhost:{}/swagger-ui",
        app_state.config.port
    );

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

#[utoipa::path(
    get,
    path = "/health",
    tag = "Health",
    responses(
        (status = 200, description = "Service is healthy"),
    ),
)]
pub async fn health_check() -> (axum::http::StatusCode, Json<serde_json::Value>) {
    (
        axum::http::StatusCode::OK,
        Json(serde_json::json!({
            "status": "ok",
            "service": "rust-api-gateway",
        })),
    )
}
