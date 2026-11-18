use axum::extract::{Path, Query, State};
use axum::http::StatusCode;
use axum::Json;

use crate::error::AppError;
use crate::state::AppState;

#[derive(Debug, serde::Deserialize)]
pub struct CreateOrderItemRequest {
    pub variant_id: String,
    pub quantity: i32,
    pub unit_price: f64,
}

#[derive(Debug, serde::Deserialize)]
pub struct CreateOrderRequest {
    pub customer_id: String,
    pub items: Vec<CreateOrderItemRequest>,
    pub shipping_address: String,
    pub billing_address: String,
}

#[derive(Debug, serde::Deserialize)]
pub struct GetOrdersQuery {
    #[serde(default = "default_page")]
    pub page: i32,
    #[serde(default = "default_page_size")]
    pub page_size: i32,
    #[serde(default)]
    pub customer_id: Option<String>,
    #[serde(default)]
    pub created_after: Option<String>,
}

fn default_page() -> i32 {
    1
}

fn default_page_size() -> i32 {
    20
}

#[derive(Debug, serde::Serialize)]
pub struct CreateOrderResponse {
    pub id: String,
    pub status: i32,
}

pub async fn create_order(
    State(state): State<AppState>,
    Json(req): Json<CreateOrderRequest>,
) -> Result<(StatusCode, Json<CreateOrderResponse>), AppError> {
    let items: Vec<crate::grpc::order_client::order::OrderItemRequest> = req
        .items
        .into_iter()
        .map(|item| crate::grpc::order_client::order::OrderItemRequest {
            variant_id: item.variant_id,
            quantity: item.quantity,
            unit_price: item.unit_price,
        })
        .collect();

    let response = state.order_client
        .create_order(&req.customer_id, items, &req.shipping_address, &req.billing_address)
        .await
        .map_err(|e| AppError::Internal(format!("Order service error: {}", e)))?;

    Ok((
        StatusCode::CREATED,
        Json(CreateOrderResponse {
            id: response.id,
            status: response.status,
        }),
    ))
}

pub async fn get_order(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> Result<(StatusCode, Json<serde_json::Value>), AppError> {
    let order = state.order_client
        .get_order(&id)
        .await
        .map_err(|e| AppError::Internal(format!("Order service error: {}", e)))?;

    let json = serde_json::json!({
        "id": order.id,
        "customerId": order.customer_id,
        "status": order.status,
        "subtotal": order.subtotal,
        "tax": order.tax,
        "shippingCost": order.shipping_cost,
        "total": order.total,
        "shippingAddress": order.shipping_address,
        "billingAddress": order.billing_address,
        "paymentIntentId": order.payment_intent_id,
        "createdAt": order.created_at.as_ref().map(|t| {
            let dt = chrono::DateTime::<chrono::Utc>::from_timestamp(t.seconds, t.nanos as u32).unwrap_or_default();
            dt.to_rfc3339()
        }).unwrap_or_default(),
        "items": order.items.iter().map(|item| {
            serde_json::json!({
                "id": item.id,
                "productId": item.product_id,
                "variantId": item.variant_id,
                "productName": item.product_name,
                "variantDescription": item.variant_description,
                "unitPrice": item.unit_price,
                "salePrice": item.sale_price,
                "quantity": item.quantity,
                "totalPrice": item.total_price,
                "imageUrl": item.image_url,
            })
        }).collect::<Vec<_>>(),
    });

    Ok((StatusCode::OK, Json(json)))
}

pub async fn get_orders(
    State(state): State<AppState>,
    Query(query): Query<GetOrdersQuery>,
) -> Result<(StatusCode, Json<serde_json::Value>), AppError> {
    let response = state.order_client
        .get_orders(query.page, query.page_size, query.customer_id, query.created_after)
        .await
        .map_err(|e| AppError::Internal(format!("Order service error: {}", e)))?;

    let json = serde_json::json!({
        "orders": response.orders.iter().map(|order| {
            serde_json::json!({
                "id": order.id,
                "customerId": order.customer_id,
                "status": order.status,
                "subtotal": order.subtotal,
                "tax": order.tax,
                "shippingCost": order.shipping_cost,
                "total": order.total,
                "shippingAddress": order.shipping_address,
                "billingAddress": order.billing_address,
                "items": order.items.iter().map(|item| {
                    serde_json::json!({
                        "id": item.id,
                        "productId": item.product_id,
                        "variantId": item.variant_id,
                        "productName": item.product_name,
                        "unitPrice": item.unit_price,
                        "quantity": item.quantity,
                        "totalPrice": item.total_price,
                    })
                }).collect::<Vec<_>>(),
            })
        }).collect::<Vec<_>>(),
        "totalCount": response.total_count,
        "currentPage": response.current_page,
        "totalPages": response.total_pages,
    });

    Ok((StatusCode::OK, Json(json)))
}
