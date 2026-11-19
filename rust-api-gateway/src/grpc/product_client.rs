use std::sync::Arc;
use std::sync::RwLock;

use tonic::transport::Channel;

use crate::config::AppConfig;
use crate::error::AppError;
use crate::grpc_call_with_retry;

// Re-export generated types
pub mod product {
    include!("../proto/product/com.ecommerce.springboot.product.v1.rs");
}

#[derive(Clone)]
struct InnerClients {
    product_client: product::product_service_client::ProductServiceClient<Channel>,
    option_client: product::option_service_client::OptionServiceClient<Channel>,
    variant_client: product::variant_service_client::VariantServiceClient<Channel>,
}

#[derive(Clone)]
pub struct ProductsClient {
    config: Arc<AppConfig>,
    inner: Arc<RwLock<Option<InnerClients>>>,
}

impl ProductsClient {
    pub fn new(config: &AppConfig) -> Self {
        Self {
            config: Arc::new(config.clone()),
            inner: Arc::new(RwLock::new(None)),
        }
    }

    async fn get_clients(&self) -> Result<InnerClients, AppError> {
        // Try to reuse existing clients
        {
            let guard = self.inner.read().map_err(|e| {
                AppError::Internal(format!(
                    "Failed to acquire read lock on product client: {}",
                    e
                ))
            })?;
            if let Some(ref clients) = *guard {
                return Ok(clients.clone());
            }
        }

        // Connect
        self.connect().await
    }

    async fn connect(&self) -> Result<InnerClients, AppError> {
        let url = self.config.product_service_url.clone();
        let connector = tonic::transport::Endpoint::new(url.clone())
            .map_err(|e| AppError::Internal(format!("Invalid product service URL: {}", e)))?
            .connect()
            .await
            .map_err(|e| {
                AppError::ServiceUnavailable(format!("Cannot connect to product service: {}", e))
            })?;

        let product_client =
            product::product_service_client::ProductServiceClient::new(connector.clone());
        let option_client =
            product::option_service_client::OptionServiceClient::new(connector.clone());
        let variant_client = product::variant_service_client::VariantServiceClient::new(connector);

        let clients = InnerClients {
            product_client,
            option_client,
            variant_client,
        };

        // Store the new clients
        {
            let mut guard = self.inner.write().map_err(|e| {
                AppError::Internal(format!(
                    "Failed to acquire write lock on product client: {}",
                    e
                ))
            })?;
            *guard = Some(clients.clone());
        }

        Ok(clients)
    }

    pub async fn get_products(
        &self,
        skip: i32,
        take: i32,
    ) -> Result<Vec<product::Product>, AppError> {
        let request = product::GetProductsRequest {
            skip: Some(skip),
            take: Some(take),
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.product_client.get_products(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner().products)
    }

    pub async fn get_product_detail(
        &self,
        id: &str,
    ) -> Result<product::GetProductDetailResponse, AppError> {
        let request = product::GetProductDetailRequest {
            id: Some(id.to_string()),
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.product_client.get_product_detail(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn create_product(
        &self,
        name: &str,
        seller_id: &str,
        description: &str,
        brand: &str,
        media_urls: Vec<String>,
    ) -> Result<product::CreateProductResponse, AppError> {
        let request = product::CreateProductRequest {
            name: Some(name.to_string()),
            seller_id: Some(seller_id.to_string()),
            description: Some(description.to_string()),
            brand: Some(brand.to_string()),
            media_urls,
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.product_client.create_product(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn get_option_types(
        &self,
        product_id: &str,
    ) -> Result<Vec<product::OptionType>, AppError> {
        let request = product::GetOptionTypesRequest {
            product_id: Some(product_id.to_string()),
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.option_client.get_option_types(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner().option_types)
    }

    pub async fn create_option_type(
        &self,
        product_id: &str,
        name: &str,
        display_order: Option<i32>,
    ) -> Result<product::CreateOptionTypeResponse, AppError> {
        let request = product::CreateOptionTypeRequest {
            product_id: Some(product_id.to_string()),
            name: Some(name.to_string()),
            display_order,
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.option_client.create_option_type(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn get_option_values(
        &self,
        option_type_id: &str,
    ) -> Result<Vec<product::OptionValue>, AppError> {
        let request = product::GetOptionValuesRequest {
            option_type_id: Some(option_type_id.to_string()),
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.option_client.get_option_values(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner().option_values)
    }

    pub async fn create_option_value(
        &self,
        option_type_id: &str,
        value: &str,
        media_url: Option<String>,
        display_order: Option<i32>,
    ) -> Result<product::CreateOptionValueResponse, AppError> {
        let request = product::CreateOptionValueRequest {
            option_type_id: Some(option_type_id.to_string()),
            value: Some(value.to_string()),
            media_url,
            display_order,
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.option_client.create_option_value(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn create_variant(
        &self,
        product_id: &str,
        sku: &str,
        price: f64,
        stock: i32,
        media_url: Option<String>,
        options: Vec<String>,
    ) -> Result<product::CreateVariantResponse, AppError> {
        let request = product::CreateVariantRequest {
            product_id: Some(product_id.to_string()),
            sku: Some(sku.to_string()),
            price: Some(price),
            stock: Some(stock),
            media_url,
            options,
        };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.variant_client.create_variant(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn get_variants_by_ids(
        &self,
        ids: Vec<String>,
    ) -> Result<Vec<product::get_variants_response::Variant>, AppError> {
        let request = product::GetVariantByIdsRequest { ids };

        grpc_call_with_retry!({
            let mut clients = self.get_clients().await?,
            clients.variant_client.get_variants_by_ids(tonic::Request::new(request.clone())).await,
            clients = self.connect().await?,
            "Product"
        })
        .map(|resp| resp.into_inner().variants)
    }
}
