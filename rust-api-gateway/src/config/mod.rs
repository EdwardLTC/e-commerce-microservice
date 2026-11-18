#[derive(Debug, Clone)]
pub struct AppConfig {
    pub port: u16,
    pub jwt_secret: String,
    pub redis_url: String,
    pub user_service_url: String,
    pub product_service_url: String,
    pub order_service_url: String,
}

impl AppConfig {
    pub fn from_env() -> Self {
        dotenvy::dotenv().ok();
        Self {
            port: env_var("PORT", "3000").parse().unwrap_or(3000),
            jwt_secret: env_var("JWT_SECRET", "default-secret-change-me"),
            redis_url: env_var("REDIS_CONNECTION", "redis://localhost:6379"),
            user_service_url: env_var("USER_SERVICE_URL", "localhost:5232"),
            product_service_url: env_var("PRODUCT_SERVICE_URL", "localhost:2003"),
            order_service_url: env_var("ORDER_SERVICE_URL", "http://localhost:2000"),
        }
    }
}

fn env_var(key: &str, default: &str) -> String {
    std::env::var(key).unwrap_or_else(|_| default.to_string())
}
