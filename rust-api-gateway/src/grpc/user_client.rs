use std::sync::Arc;
use std::sync::RwLock;

use tonic::transport::Channel;
use tracing::warn;

use crate::config::AppConfig;
use crate::error::AppError;

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
            .map_err(|e| AppError::ServiceUnavailable(format!("Cannot connect to user service: {}", e)))?;

        let client = user::user_service_client::UserServiceClient::new(connector);

        // Store the new client
        {
            let mut guard = self.inner.write().map_err(|e| {
                AppError::Internal(format!("Failed to acquire write lock on user client: {}", e))
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
        let email = email.to_string();
        let password = password.to_string();

        let request = user::GetUserByEmailAndPasswordRequest {
            email: Some(email.clone()),
            password: Some(password.clone()),
        };

        let mut client = self.get_client().await?;
        match client
            .get_user_by_email_and_password(tonic::Request::new(request))
            .await
        {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("User service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
                    let request = user::GetUserByEmailAndPasswordRequest {
                        email: Some(email),
                        password: Some(password),
                    };
                    let resp = client
                        .get_user_by_email_and_password(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
    }

    pub async fn create_user(
        &self,
        email: &str,
        password: &str,
        name: &str,
    ) -> Result<user::UserProfile, AppError> {
        let email = email.to_string();
        let password = password.to_string();
        let name = name.to_string();

        let request = user::CreateUserRequest {
            name: Some(name.clone()),
            email: Some(email.clone()),
            password: Some(password.clone()),
        };

        let mut client = self.get_client().await?;
        match client.create_user(tonic::Request::new(request)).await {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("User service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
                    let request = user::CreateUserRequest {
                        name: Some(name),
                        email: Some(email),
                        password: Some(password),
                    };
                    let resp = client
                        .create_user(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
    }

    pub async fn get_user_by_id(&self, id: &str) -> Result<user::UserProfile, AppError> {
        let id = id.to_string();

        let request = user::GetUserByIdRequest {
            id: Some(id.clone()),
        };

        let mut client = self.get_client().await?;
        match client.get_user_by_id(tonic::Request::new(request)).await {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("User service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
                    let request = user::GetUserByIdRequest {
                        id: Some(id),
                    };
                    let resp = client
                        .get_user_by_id(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
    }

    pub async fn change_password(
        &self,
        id: &str,
        old_password: &str,
        new_password: &str,
    ) -> Result<user::UserProfile, AppError> {
        let id = id.to_string();
        let old_password = old_password.to_string();
        let new_password = new_password.to_string();

        let request = user::ChangePasswordRequest {
            id: Some(id.clone()),
            old_password: Some(old_password.clone()),
            new_password: Some(new_password.clone()),
        };

        let mut client = self.get_client().await?;
        match client.change_password(tonic::Request::new(request)).await {
            Ok(resp) => Ok(resp.into_inner()),
            Err(status) => {
                if status.code() == tonic::Code::Unavailable
                    || status.code() == tonic::Code::Internal
                {
                    warn!("User service call failed, attempting reconnection: {}", status);
                    client = self.connect().await?;
                    let request = user::ChangePasswordRequest {
                        id: Some(id),
                        old_password: Some(old_password),
                        new_password: Some(new_password),
                    };
                    let resp = client
                        .change_password(tonic::Request::new(request))
                        .await
                        .map_err(AppError::from)?;
                    Ok(resp.into_inner())
                } else {
                    Err(AppError::from(status))
                }
            }
        }
    }
}
