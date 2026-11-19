// gRPC clients for communicating with microservices
pub mod order_client;
pub mod product_client;
pub mod user_client;

/// Macro that wraps a gRPC call with automatic reconnect-and-retry on transient failures.
///
/// The `call` expression references the client variable declared in the init block.
/// It is evaluated twice (once for the initial call, once for the retry after reconnect).
/// Use `request.clone()` in the call expression so the retry can reuse the request.
///
/// # Usage
/// ```ignore
/// let result = grpc_call_with_retry!({
///     let mut client = self.get_client().await?,
///     client.get_order(tonic::Request::new(request.clone())).await,
///     client = self.connect().await?,
///     "Order"
/// });
/// ```

#[macro_export]
macro_rules! grpc_call_with_retry {
    ({
        $init_stmt:stmt,
        $call_expr:expr,
        $reconnect_stmt:stmt,
        $service_name:expr
    }) => {{
        $init_stmt
        match $call_expr {
            Ok(__resp) => Ok(__resp),

            Err(__status) => {
                if matches!(
                    __status.code(),
                    tonic::Code::Unavailable
                ) {
                    ::tracing::warn!(
                        "{} service call failed, attempting reconnection: {}",
                        $service_name,
                        __status
                    );

                    $reconnect_stmt

                    match $call_expr {
                        Ok(__resp) => Ok(__resp),
                        Err(__e) => {
                            Err($crate::error::AppError::from(__e))
                        }
                    }
                } else {
                    Err($crate::error::AppError::from(__status))
                }
            }
        }
    }};
    }
