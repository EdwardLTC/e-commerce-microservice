use std::sync::Arc;
use std::sync::RwLock;

use tonic::transport::Channel;

use crate::config::AppConfig;
use crate::error::AppError;
use crate::grpc_call_with_retry;

// Re-export generated types
pub mod user {
    include!("../proto/user/com.ecommerce.aspnet.user.rs");
}

type InnerClient = user::user_service_client::UserServiceClient<Channel>;

#[derive(Clone)]
pub struct UsersClient {
    config: Arc<AppConfig>,
    inner: Arc<RwLock<Option<InnerClient>>>,
}

impl UsersClient {
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
                AppError::Internal(format!("Failed to acquire read lock on user client: {}", e))
            })?;
            if let Some(ref client) = *guard {
                return Ok(client.clone());
            }
        }

        // Connect
        self.connect().await
    }

    async fn connect(&self) -> Result<InnerClient, AppError> {
        let connector = tonic::transport::Endpoint::new(self.config.user_service_url.clone())
            .map_err(|e| AppError::Internal(format!("Invalid user service URL: {}", e)))?
            .connect()
            .await
            .map_err(|e| {
                AppError::ServiceUnavailable(format!("Cannot connect to user service: {}", e))
            })?;

        let client = user::user_service_client::UserServiceClient::new(connector);

        // Store the new client
        {
            let mut guard = self.inner.write().map_err(|e| {
                AppError::Internal(format!(
                    "Failed to acquire write lock on user client: {}",
                    e
                ))
            })?;
            *guard = Some(client.clone());
        }

        Ok(client)
    }

    pub async fn get_user_by_email_and_password(
        &self,
        email: &str,
        password: &str,
    ) -> Result<user::UserProfile, AppError> {
        let request = user::GetUserByEmailAndPasswordRequest {
            email: Some(email.to_string()),
            password: Some(password.to_string()),
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.get_user_by_email_and_password(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "User"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn create_user(
        &self,
        email: &str,
        password: &str,
        name: &str,
    ) -> Result<user::UserProfile, AppError> {
        let request = user::CreateUserRequest {
            name: Some(name.to_string()),
            email: Some(email.to_string()),
            password: Some(password.to_string()),
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.create_user(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "User"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn get_user_by_id(&self, id: &str) -> Result<user::UserProfile, AppError> {
        let request = user::GetUserByIdRequest {
            id: Some(id.to_string()),
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.get_user_by_id(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "User"
        })
        .map(|resp| resp.into_inner())
    }

    pub async fn change_password(
        &self,
        id: &str,
        old_password: &str,
        new_password: &str,
    ) -> Result<user::UserProfile, AppError> {
        let request = user::ChangePasswordRequest {
            id: Some(id.to_string()),
            old_password: Some(old_password.to_string()),
            new_password: Some(new_password.to_string()),
        };

        grpc_call_with_retry!({
            let mut client = self.get_client().await?,
            client.change_password(tonic::Request::new(request.clone())).await,
            client = self.connect().await?,
            "User"
        })
        .map(|resp| resp.into_inner())
    }
}
