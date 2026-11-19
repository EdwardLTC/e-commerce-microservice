use std::sync::Arc;
use std::sync::RwLock;

use prost_types::Timestamp;
use tonic::transport::Channel;

use crate::config::AppConfig;
use crate::error::AppError;
use crate::grpc_call_with_retry;

// Re-export generated types
pub mod order {
    include!("../proto/order/com.ecommerce.golang.order.rs");
}

type InnerClient = order::order_service_client::OrderServiceClient<Channel>;

#[derive(Clone)]
pub struct OrdersClient {
    config: Arc<AppConfig>,
    inner: Arc<RwLock<Option<InnerClient>>>,
}

impl OrdersClient {
    pub fn new(config: &AppConfig) -> Self {
        Self {
            config: Arc::new(config.clone()),
            inner: Arc::new(RwLock::new(None)),
        }
    }

    async fn get_client(&self) -> Result<InnerClient, AppError> {
        // Try to reuse existing client
        {
            let guard = self.inner.read().map_err(|e| {
                AppError::Internal(format!("Failed to acquire read lock on order client: {}", e))
            })?;
            if let Some(ref client) = *guard {
                return Ok(client.clone());
            }
        }

        // Connect
        self.connect().await
    }

    async fn connect(&self) -> Result<InnerClient, AppError> {
        let url = self.config.order_service_url.clone();
        // Strip http:// or https:// prefix if present
        let url = url
            .strip_prefix("http://")
            .or_else(|| url.strip_prefix("https://"))
            .unwrap_or(&url);

        let connector = tonic::transport::Endpoint::new(url.to_string())
            .map_err(|e| AppError::Internal(format!("Invalid order service URL: {}", e)))?
            .connect()
            .await
            .map_err(|e| AppError::ServiceUnavailable(format!("Cannot connect to order service: {}", e)))?;

        let client = order::order_service_client::OrderServiceClient::new(connector);

        // Store the new client
        {
            let mut guard = self.inner.write().map_err(|e| {
                AppError::Internal(format!("Failed to acquire write lock on order client: {}", e))
            })?;
            *guard = Some(client.clone());
        }

        Ok(client)
    }

    pub async fn create_order(
        &self,
        customer_id: &str,
        items: Vec<order::OrderItemRequest>,
        shipping_address: &str,
        billing_address: &str,
    ) -> Result<order::CreateOrderResponse, AppError> {
        let request = order::CreateOrderRequest {
            customer_id: customer_id.to_string(),
            items,
            shipping_address: shipping_address.to_string(),
            billing_address: billing_address.to_string(),
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.create_order(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "Order"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn get_order(&self, id: &str) -> Result<order::Order, AppError> {
        let request = order::GetOrderRequest {
            id: id.to_string(),
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.get_order(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "Order"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn get_orders(
        &self,
        page: i32,
        page_size: i32,
        customer_id: Option<String>,
        created_after: Option<String>,
    ) -> Result<order::ListOrdersResponse, AppError> {
        let created_after_ts = created_after
            .as_ref()
            .and_then(|s| {
                let dt = chrono::DateTime::parse_from_rfc3339(s).ok()?;
                Some(Timestamp {
                    seconds: dt.timestamp(),
                    nanos: dt.timestamp_subsec_nanos() as i32,
                })
            });

        let request = order::ListOrdersRequest {
            page,
            page_size,
            customer_id: customer_id.clone().unwrap_or_default(),
            status: 0,
            created_after: created_after_ts,
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.get_orders(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "Order"
        })
        .map(|resp| resp.into_inner())
    }
}
