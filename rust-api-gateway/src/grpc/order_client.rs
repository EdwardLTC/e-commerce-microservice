use tonic::transport::Channel;
use prost_types::Timestamp;

use crate::config::AppConfig;
use crate::error::AppError;

// Re-export generated types
pub mod order {
    include!("../proto/order/com.ecommerce.golang.order.rs");
}

#[derive(Clone)]
pub struct OrdersClient {
    inner: order::order_service_client::OrderServiceClient<Channel>,
}

impl OrdersClient {
    pub async fn new(config: &AppConfig) -> Result<Self, AppError> {
        let url = config.order_service_url.clone();
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
        Ok(Self { inner: client })
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

        let response = self
            .inner
            .clone()
            .create_order(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
    }

    pub async fn get_order(&self, id: &str) -> Result<order::Order, AppError> {
        let request = order::GetOrderRequest {
            id: id.to_string(),
        };

        let response = self
            .inner
            .clone()
            .get_order(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
    }

    pub async fn get_orders(
        &self,
        page: i32,
        page_size: i32,
        customer_id: Option<String>,
        created_after: Option<String>,
    ) -> Result<order::ListOrdersResponse, AppError> {
        let created_after_ts = created_after
            .and_then(|s| {
                let dt = chrono::DateTime::parse_from_rfc3339(&s).ok()?;
                Some(Timestamp {
                    seconds: dt.timestamp(),
                    nanos: dt.timestamp_subsec_nanos() as i32,
                })
            });

        let request = order::ListOrdersRequest {
            page,
            page_size,
            customer_id: customer_id.unwrap_or_default(),
            status: 0, // No status filter
            created_after: created_after_ts,
        };

        let response = self
            .inner
            .clone()
            .get_orders(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
    }
}
