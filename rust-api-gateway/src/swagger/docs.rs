use utoipa::OpenApi;

use crate::auth::routes::{
    AuthTokenResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, UserProfileResponse,
};
use crate::orders::routes::{
    CreateOrderItemRequest, CreateOrderRequest, CreateOrderResponse, GetOrdersQuery,
};
use crate::products::routes::{
    CreateOptionTypeRequest, CreateOptionValueRequest, CreateProductRequest, CreateVariantRequest,
    GetProductsQuery, GetVariantsQuery, ResourceCreatedResponse,
};

#[derive(OpenApi)]
#[openapi(
    info(title = "E-Commerce API Gateway",
        description = "API Gateway for the e-commerce microservice system. Provides unified access to auth, user, product, and order services.",
        version = "0.1.0",
        contact(name = "API Support", email = "support@example.com")),
    paths(
        crate::auth::routes::login,
        crate::auth::routes::register,
        crate::auth::routes::logout,
        crate::auth::routes::change_password,
        crate::users::routes::get_current_user,
        crate::users::routes::get_user_by_id,
        crate::products::routes::get_products,
        crate::products::routes::get_product_by_id,
        crate::products::routes::create_product,
        crate::products::routes::get_variants_by_ids,
        crate::products::routes::get_option_types,
        crate::products::routes::create_option_type,
        crate::products::routes::get_option_values,
        crate::products::routes::create_option_value,
        crate::products::routes::create_variant,
        crate::orders::routes::create_order,
        crate::orders::routes::get_order,
        crate::orders::routes::get_orders,
        crate::health_check
    ),
    components(schemas(
        LoginRequest,
        RegisterRequest,
        ChangePasswordRequest,
        AuthTokenResponse,
        UserProfileResponse,
        GetProductsQuery,
        GetVariantsQuery,
        CreateProductRequest,
        CreateOptionTypeRequest,
        CreateOptionValueRequest,
        CreateVariantRequest,
        ResourceCreatedResponse,
        CreateOrderItemRequest,
        CreateOrderRequest,
        CreateOrderResponse,
        GetOrdersQuery,
        crate::error::ErrorResponse,
    ))
)]
pub struct ApiDocs;
