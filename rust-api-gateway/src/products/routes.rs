use axum::extract::{Path, Query, State};
use axum::http::StatusCode;
use axum::Json;

use crate::error::AppError;
use crate::middleware::validate_auth;
use crate::products::dto::*;
use crate::state::{get_current_token, AppState};

#[utoipa::path(
    get,
    path = "/products",
    tag = "Products",
    params(GetProductsQuery),
    responses(
        (status = 200, description = "List of products"),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_products(
    State(state): State<AppState>,
    Query(query): Query<GetProductsQuery>,
) -> Result<(StatusCode, Json<Vec<serde_json::Value>>), AppError> {
    let products = state
        .product_client
        .get_products(query.skip, query.take)
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    // Convert prost messages to JSON
    let json_products: Vec<serde_json::Value> = products
        .iter()
        .map(|p| {
            serde_json::json!({
                "id": p.id,
                "name": p.name,
                "description": p.description,
                "brand": p.brand,
                "minPrice": p.min_price,
                "maxPrice": p.max_price,
                "totalSaleCount": p.total_sale_count,
                "rating": p.rating,
                "mediaUrls": p.media_urls,
            })
        })
        .collect();

    Ok((StatusCode::OK, Json(json_products)))
}

#[utoipa::path(
    get,
    path = "/products/:id",
    tag = "Products",
    params(
        ("id" = String, Path, description = "Product ID"),
    ),
    responses(
        (status = 200, description = "Product detail"),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_product_by_id(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> Result<(StatusCode, Json<serde_json::Value>), AppError> {
    let detail = state
        .product_client
        .get_product_detail(&id)
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    let json = serde_json::json!({
        "id": detail.id,
        "name": detail.name,
        "description": detail.description,
        "brand": detail.brand,
        "totalSaleCount": detail.total_sale_count,
        "averageRating": detail.average_rating,
        "mediaUrls": detail.media_urls,
        "optionTypes": detail.option_types.iter().map(|ot| {
            serde_json::json!({
                "id": ot.id,
                "name": ot.name,
                "displayOrder": ot.display_order,
                "optionValues": ot.option_values.iter().map(|ov| {
                    serde_json::json!({
                        "id": ov.id,
                        "value": ov.value,
                        "mediaUrl": ov.media_url,
                        "displayOrder": ov.display_order,
                    })
                }).collect::<Vec<_>>(),
            })
        }).collect::<Vec<_>>(),
        "variants": detail.variants.iter().map(|v| {
            serde_json::json!({
                "id": v.id,
                "sku": v.sku,
                "price": v.price,
                "salePrice": v.sale_price,
                "stock": v.stock,
                "status": v.status,
                "mediaUrl": v.media_url,
                "selectedOptions": v.selected_options.iter().map(|so| {
                    serde_json::json!({
                        "optionTypeId": so.option_type_id,
                        "optionValueId": so.option_value_id,
                    })
                }).collect::<Vec<_>>(),
            })
        }).collect::<Vec<_>>(),
    });

    Ok((StatusCode::OK, Json(json)))
}

#[utoipa::path(
    get,
    path = "/products/variants",
    tag = "Products",
    params(GetVariantsQuery),
    responses(
        (status = 200, description = "List of variants"),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_variants_by_ids(
    State(state): State<AppState>,
    Query(query): Query<GetVariantsQuery>,
) -> Result<(StatusCode, Json<Vec<serde_json::Value>>), AppError> {
    let variants = state
        .product_client
        .get_variants_by_ids(query.ids)
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    let json_variants: Vec<serde_json::Value> = variants
        .iter()
        .map(|v| {
            serde_json::json!({
                "id": v.id,
                "sku": v.sku,
                "price": v.price,
                "salePrice": v.sale_price,
                "stock": v.stock,
                "mediaUrl": v.media_url,
            })
        })
        .collect();

    Ok((StatusCode::OK, Json(json_variants)))
}

#[utoipa::path(
    post,
    path = "/products",
    tag = "Products",
    request_body = CreateProductRequest,
    responses(
        (status = 201, description = "Product created", body = ResourceCreatedResponse),
        (status = 401, description = "Unauthorized", body = crate::error::ErrorResponse),
    ),
)]
pub async fn create_product(
    State(state): State<AppState>,
    Json(req_body): Json<CreateProductRequest>,
) -> Result<(StatusCode, Json<ResourceCreatedResponse>), AppError> {
    let token =
        get_current_token().ok_or_else(|| AppError::Unauthorized("Missing token".to_string()))?;
    let claims = validate_auth(&state, &token).await?;
    let response = state
        .product_client
        .create_product(
            &req_body.name,
            &claims.id,
            &req_body.description,
            &req_body.brand,
            req_body.media_urls,
        )
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    Ok((
        StatusCode::CREATED,
        Json(ResourceCreatedResponse { id: response.id }),
    ))
}

#[utoipa::path(
    post,
    path = "/products/:id/option-types",
    tag = "Products",
    params(
        ("id" = String, Path, description = "Product ID"),
    ),
    request_body = CreateOptionTypeRequest,
    responses(
        (status = 201, description = "Option type created", body = ResourceCreatedResponse),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn create_option_type(
    State(state): State<AppState>,
    Path(product_id): Path<String>,
    Json(req): Json<CreateOptionTypeRequest>,
) -> Result<(StatusCode, Json<ResourceCreatedResponse>), AppError> {
    let response = state
        .product_client
        .create_option_type(&product_id, &req.name, req.display_order)
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    Ok((
        StatusCode::CREATED,
        Json(ResourceCreatedResponse { id: response.id }),
    ))
}

#[utoipa::path(
    get,
    path = "/products/:id/option-types",
    tag = "Products",
    params(
        ("id" = String, Path, description = "Product ID"),
    ),
    responses(
        (status = 200, description = "List of option types"),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_option_types(
    State(state): State<AppState>,
    Path(product_id): Path<String>,
) -> Result<(StatusCode, Json<Vec<serde_json::Value>>), AppError> {
    let option_types = state
        .product_client
        .get_option_types(&product_id)
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    let json: Vec<serde_json::Value> = option_types
        .iter()
        .map(|ot| {
            serde_json::json!({
                "id": ot.id,
                "name": ot.name,
                "displayOrder": ot.display_order,
                "optionValues": ot.option_values.iter().map(|ov| {
                    serde_json::json!({
                        "id": ov.id,
                        "value": ov.value,
                        "mediaUrl": ov.media_url,
                        "displayOrder": ov.display_order,
                    })
                }).collect::<Vec<_>>(),
            })
        })
        .collect();

    Ok((StatusCode::OK, Json(json)))
}

#[utoipa::path(
    post,
    path = "/products/option-types/:optionTypeId/option-values",
    tag = "Products",
    params(
        ("optionTypeId" = String, Path, description = "Option Type ID"),
    ),
    request_body = CreateOptionValueRequest,
    responses(
        (status = 201, description = "Option value created", body = ResourceCreatedResponse),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn create_option_value(
    State(state): State<AppState>,
    Path(option_type_id): Path<String>,
    Json(req): Json<CreateOptionValueRequest>,
) -> Result<(StatusCode, Json<ResourceCreatedResponse>), AppError> {
    let response = state
        .product_client
        .create_option_value(
            &option_type_id,
            &req.value,
            req.media_url,
            req.display_order,
        )
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    Ok((
        StatusCode::CREATED,
        Json(ResourceCreatedResponse { id: response.id }),
    ))
}

#[utoipa::path(
    get,
    path = "/products/option-types/:optionTypeId/option-values",
    tag = "Products",
    params(
        ("optionTypeId" = String, Path, description = "Option Type ID"),
    ),
    responses(
        (status = 200, description = "List of option values"),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn get_option_values(
    State(state): State<AppState>,
    Path(option_type_id): Path<String>,
) -> Result<(StatusCode, Json<Vec<serde_json::Value>>), AppError> {
    let option_values = state
        .product_client
        .get_option_values(&option_type_id)
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    let json: Vec<serde_json::Value> = option_values
        .iter()
        .map(|ov| {
            serde_json::json!({
                "id": ov.id,
                "value": ov.value,
                "mediaUrl": ov.media_url,
                "displayOrder": ov.display_order,
            })
        })
        .collect();

    Ok((StatusCode::OK, Json(json)))
}

#[utoipa::path(
    post,
    path = "/products/:id/variants",
    tag = "Products",
    params(
        ("id" = String, Path, description = "Product ID"),
    ),
    request_body = CreateVariantRequest,
    responses(
        (status = 201, description = "Variant created", body = ResourceCreatedResponse),
        (status = 500, description = "Internal server error", body = crate::error::ErrorResponse),
    ),
)]
pub async fn create_variant(
    State(state): State<AppState>,
    Path(product_id): Path<String>,
    Json(req): Json<CreateVariantRequest>,
) -> Result<(StatusCode, Json<ResourceCreatedResponse>), AppError> {
    let response = state
        .product_client
        .create_variant(
            &product_id,
            &req.sku,
            req.price,
            req.stock,
            req.media_url,
            req.options,
        )
        .await
        .map_err(|e| AppError::Internal(format!("Product service error: {}", e)))?;

    Ok((
        StatusCode::CREATED,
        Json(ResourceCreatedResponse { id: response.id }),
    ))
}
