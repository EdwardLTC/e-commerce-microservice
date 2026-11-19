use axum::http::StatusCode;
use axum::response::{IntoResponse, Json};
use serde::Serialize;
use std::fmt;
use tonic::Status as TonicStatus;

#[derive(Debug, Serialize, utoipa::ToSchema)]
pub struct ErrorResponse {
    /// HTTP status code
    pub status_code: u16,
    /// ISO 8601 timestamp
    pub timestamp: String,
    /// Error message
    pub message: String,
    /// Optional error cause details
    #[serde(skip_serializing_if = "Option::is_none")]
    pub cause: Option<serde_json::Value>,
}

impl ErrorResponse {
    pub fn new(status: StatusCode, message: impl Into<String>) -> Self {
        Self {
            status_code: status.as_u16(),
            timestamp: chrono::Utc::now().to_rfc3339(),
            message: message.into(),
            cause: None,
        }
    }

    pub fn with_cause(
        status: StatusCode,
        message: impl Into<String>,
        cause: impl Into<serde_json::Value>,
    ) -> Self {
        Self {
            status_code: status.as_u16(),
            timestamp: chrono::Utc::now().to_rfc3339(),
            message: message.into(),
            cause: Some(cause.into()),
        }
    }
}

#[derive(Debug)]
pub enum AppError {
    Unauthorized(String),
    Internal(String),
    ServiceUnavailable(String),
    GrpcError { code: u32, details: String },
}

impl fmt::Display for AppError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            AppError::Unauthorized(msg) => write!(f, "Unauthorized: {}", msg),
            AppError::Internal(msg) => write!(f, "Internal Error: {}", msg),
            AppError::ServiceUnavailable(msg) => write!(f, "Service Unavailable: {}", msg),
            AppError::GrpcError { code, details } => {
                write!(f, "gRPC Error (code {}): {}", code, details)
            }
        }
    }
}

impl std::error::Error for AppError {}

impl From<TonicStatus> for AppError {
    fn from(status: TonicStatus) -> Self {
        let code = status.code() as u32;
        let details = status.message().to_string();

        match status.code() {
            tonic::Code::Unavailable => AppError::ServiceUnavailable(details),

            tonic::Code::Internal => AppError::Internal(details),

            tonic::Code::Unauthenticated => AppError::Unauthorized(details),

            _ => AppError::GrpcError { code, details },
        }
    }
}

impl IntoResponse for AppError {
    fn into_response(self) -> axum::response::Response {
        let (status, message, cause) = match &self {
            AppError::Unauthorized(msg) => (StatusCode::UNAUTHORIZED, msg.clone(), None),
            AppError::Internal(msg) => (StatusCode::INTERNAL_SERVER_ERROR, msg.clone(), None),
            AppError::ServiceUnavailable(msg) => {
                (StatusCode::SERVICE_UNAVAILABLE, msg.clone(), None)
            }
            AppError::GrpcError { code, details } => {
                let status = grpc_code_to_http(*code);
                let message = http_500_message(status)
                    .map(|s| s.to_string())
                    .unwrap_or_else(|| details.clone());
                (
                    status,
                    message,
                    Some(serde_json::json!({"code": code, "details": details})),
                )
            }
        };

        let error_response = if let Some(cause) = cause {
            ErrorResponse::with_cause(status, message, cause)
        } else {
            ErrorResponse::new(status, message)
        };

        (status, Json(error_response)).into_response()
    }
}

fn grpc_code_to_http(code: u32) -> StatusCode {
    match code {
        0 => StatusCode::OK,
        1 => StatusCode::METHOD_NOT_ALLOWED,
        2 => StatusCode::INTERNAL_SERVER_ERROR,
        3 => StatusCode::UNPROCESSABLE_ENTITY,
        4 => StatusCode::REQUEST_TIMEOUT,
        5 => StatusCode::NOT_FOUND,
        6 => StatusCode::CONFLICT,
        7 => StatusCode::FORBIDDEN,
        8 => StatusCode::TOO_MANY_REQUESTS,
        9 => StatusCode::PRECONDITION_REQUIRED,
        10 => StatusCode::METHOD_NOT_ALLOWED,
        11 => StatusCode::PAYLOAD_TOO_LARGE,
        12 => StatusCode::NOT_IMPLEMENTED,
        13 => StatusCode::INTERNAL_SERVER_ERROR,
        14 => StatusCode::SERVICE_UNAVAILABLE,
        15 => StatusCode::INTERNAL_SERVER_ERROR,
        16 => StatusCode::UNAUTHORIZED,
        _ => StatusCode::INTERNAL_SERVER_ERROR,
    }
}

fn http_500_message(status: StatusCode) -> Option<&'static str> {
    match status.as_u16() {
        500 => Some("Internal server error"),
        501 => Some("Not implemented"),
        502 => Some("Bad gateway"),
        503 => Some("Service unavailable"),
        504 => Some("Gateway timeout"),
        507 => Some("Insufficient storage"),
        508 => Some("Loop detected"),
        _ => None,
    }
}
