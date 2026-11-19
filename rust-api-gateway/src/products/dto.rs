#[derive(Debug, serde::Deserialize, utoipa::ToSchema, utoipa::IntoParams)]
pub struct GetProductsQuery {
    /// Number of items to skip
    #[serde(default)]
    pub skip: i32,
    /// Number of items to take
    #[serde(default = "default_take")]
    pub take: i32,
}

fn default_take() -> i32 {
    10
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema, utoipa::IntoParams)]
pub struct GetVariantsQuery {
    /// List of variant IDs
    pub ids: Vec<String>,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct CreateProductRequest {
    /// Product name
    pub name: String,
    /// Product description
    pub description: String,
    /// Product brand
    pub brand: String,
    /// Media URLs for the product
    #[serde(default)]
    pub media_urls: Vec<String>,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct CreateOptionTypeRequest {
    /// Option type name
    pub name: String,
    /// Display order
    #[serde(default)]
    pub display_order: Option<i32>,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct CreateOptionValueRequest {
    /// Option value
    pub value: String,
    /// Media URL for the option value
    #[serde(default)]
    pub media_url: Option<String>,
    /// Display order
    #[serde(default)]
    pub display_order: Option<i32>,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct CreateVariantRequest {
    /// Stock keeping unit
    pub sku: String,
    /// Variant price
    pub price: f64,
    /// Stock quantity
    pub stock: i32,
    /// Media URL for the variant
    #[serde(default)]
    pub media_url: Option<String>,
    /// Selected option value IDs
    #[serde(default)]
    pub options: Vec<String>,
}

#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct ResourceCreatedResponse {
    /// ID of the created resource
    pub id: String,
}
