use tonic::transport::Channel;

use crate::config::AppConfig;
use crate::error::AppError;

// Re-export generated types
pub mod product {
    include!("../proto/product/com.ecommerce.springboot.product.v1.rs");
}

#[derive(Clone)]
pub struct ProductsClient {
    product_client: product::product_service_client::ProductServiceClient<Channel>,
    option_client: product::option_service_client::OptionServiceClient<Channel>,
    variant_client: product::variant_service_client::VariantServiceClient<Channel>,
}

impl ProductsClient {
    pub async fn new(config: &AppConfig) -> Result<Self, AppError> {
        let url = config.product_service_url.clone();
        let connector = tonic::transport::Endpoint::new(url.clone())
            .map_err(|e| AppError::Internal(format!("Invalid product service URL: {}", e)))?
            .connect()
            .await
            .map_err(|e| AppError::ServiceUnavailable(format!("Cannot connect to product service: {}", e)))?;

        let product_client = product::product_service_client::ProductServiceClient::new(connector.clone());
        let option_client = product::option_service_client::OptionServiceClient::new(connector.clone());
        let variant_client = product::variant_service_client::VariantServiceClient::new(connector);

        Ok(Self {
            product_client,
            option_client,
            variant_client,
        })
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

        let response = self
            .product_client
            .clone()
            .get_products(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner().products)
    }

    pub async fn get_product_detail(&self, id: &str) -> Result<product::GetProductDetailResponse, AppError> {
        let request = product::GetProductDetailRequest {
            id: Some(id.to_string()),
        };

        let response = self
            .product_client
            .clone()
            .get_product_detail(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
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

        let response = self
            .product_client
            .clone()
            .create_product(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
    }

    pub async fn get_option_types(
        &self,
        product_id: &str,
    ) -> Result<Vec<product::OptionType>, AppError> {
        let request = product::GetOptionTypesRequest {
            product_id: Some(product_id.to_string()),
        };

        let response = self
            .option_client
            .clone()
            .get_option_types(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner().option_types)
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

        let response = self
            .option_client
            .clone()
            .create_option_type(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
    }

    pub async fn get_option_values(
        &self,
        option_type_id: &str,
    ) -> Result<Vec<product::OptionValue>, AppError> {
        let request = product::GetOptionValuesRequest {
            option_type_id: Some(option_type_id.to_string()),
        };

        let response = self
            .option_client
            .clone()
            .get_option_values(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner().option_values)
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

        let response = self
            .option_client
            .clone()
            .create_option_value(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
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

        let response = self
            .variant_client
            .clone()
            .create_variant(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner())
    }

    pub async fn get_variants_by_ids(
        &self,
        ids: Vec<String>,
    ) -> Result<Vec<product::get_variants_response::Variant>, AppError> {
        let request = product::GetVariantByIdsRequest { ids };

        let response = self
            .variant_client
            .clone()
            .get_variants_by_ids(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        Ok(response.into_inner().variants)
    }
}
