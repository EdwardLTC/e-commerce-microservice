use std::sync::Arc;
use std::sync::RwLock;

use prost_types::Timestamp;
use tonic::transport::Channel;
use tracing::warn;

use crate::config::AppConfig;
use crate::error::AppError;

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
        let customer_id = customer_id.to_string();
        let shipping_address = shipping_address.to_string();
        let billing_address = billing_address.to_string();

        let request = order::CreateOrderRequest {
            customer_id: customer_id.clone(),
            items: items.clone(),
            shipping_address: shipping_address.clone(),
            billing_address: billing_address.clone(),
        };

        let mut client = self.get_client().await?;
        match client.create_order(tonic::Request::new(request)).await {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("Order service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
                    let request = order::CreateOrderRequest {
                        customer_id,
                        items,
                        shipping_address,
                        billing_address,
                    };
                    let resp = client
                        .create_order(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
    }

    pub async fn get_order(&self, id: &str) -> Result<order::Order, AppError> {
        let id = id.to_string();

        let request = order::GetOrderRequest {
            id: id.clone(),
        };

        let mut client = self.get_client().await?;
        match client.get_order(tonic::Request::new(request)).await {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("Order service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
                    let request = order::GetOrderRequest { id };
                    let resp = client
                        .get_order(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
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
            created_after: created_after_ts.clone(),
        };

        let mut client = self.get_client().await?;
        match client.get_orders(tonic::Request::new(request)).await {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("Order service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
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
                        customer_id: customer_id.unwrap_or_default(),
                        status: 0,
                        created_after: created_after_ts,
                    };
                    let resp = client
                        .get_orders(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
    }
}
