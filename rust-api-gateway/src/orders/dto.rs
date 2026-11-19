fn default_page() -> i32 {
    1
}

fn default_page_size() -> i32 {
    20
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct CreateOrderItemRequest {
    /// Product variant ID
    pub variant_id: String,
    /// Quantity ordered
    pub quantity: i32,
    /// Unit price
    pub unit_price: f64,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct CreateOrderRequest {
    /// Order items
    pub items: Vec<CreateOrderItemRequest>,
    /// Shipping address
    pub shipping_address: String,
    /// Billing address
    pub billing_address: String,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema, utoipa::IntoParams)]
pub struct GetOrdersQuery {
    /// Page number
    #[serde(default = "default_page")]
    pub page: i32,
    /// Page size
    #[serde(default = "default_page_size")]
    pub page_size: i32,
    /// Filter by creation date
    #[serde(default)]
    pub created_after: Option<String>,
}

#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct CreateOrderResponse {
    /// Order ID
    pub id: String,
    /// Order status
    pub status: i32,
}
