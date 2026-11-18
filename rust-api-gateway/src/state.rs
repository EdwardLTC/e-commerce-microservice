use std::sync::Arc;

use crate::config::AppConfig;
use crate::grpc::order_client::OrdersClient;
use crate::grpc::product_client::ProductsClient;
use crate::grpc::user_client::UsersClient;

// Thread-local storage for the current request's Bearer token
use std::cell::RefCell;

thread_local! {
    static CURRENT_TOKEN: RefCell<Option<String>> = RefCell::new(None);
}

pub fn set_current_token(token: String) {
    CURRENT_TOKEN.with(|cell| {
        *cell.borrow_mut() = Some(token);
    });
}

pub fn get_current_token() -> Option<String> {
    CURRENT_TOKEN.with(|cell| {
        cell.borrow().clone()
    })
}

// Shared application state passed to all handlers
#[derive(Clone)]
pub struct AppState {
    pub config: Arc<AppConfig>,
    pub redis_client: redis::Client,
    pub user_client: UsersClient,
    pub product_client: ProductsClient,
    pub order_client: OrdersClient,
}

impl AppState {
    pub fn new(
        config: AppConfig,
        redis_client: redis::Client,
        user_client: UsersClient,
        product_client: ProductsClient,
        order_client: OrdersClient,
    ) -> Self {
        Self {
            config: Arc::new(config),
            redis_client,
            user_client,
            product_client,
            order_client,
        }
    }
}

// JWT claims extracted from the Bearer token
#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct AuthClaims {
    pub id: String,
    pub email: String,
    pub jti: String,
    pub exp: usize,
}

// Login session stored in Redis
#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct LoginSession {
    pub jti: String,
    pub user_id: String,
    pub status: String,
    pub ip: String,
    pub user_agent: String,
    pub created_at: String,
    pub expired_at: String,
}
