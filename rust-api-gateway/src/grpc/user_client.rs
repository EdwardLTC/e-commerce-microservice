use tonic::transport::Channel;

use crate::config::AppConfig;
use crate::error::AppError;

// Re-export generated types
pub mod user {
    include!("../proto/user/com.ecommerce.aspnet.user.rs");
}

#[derive(Clone)]
pub struct UsersClient {
    inner: user::user_service_client::UserServiceClient<Channel>,
}

impl UsersClient {
    pub async fn new(config: &AppConfig) -> Result<Self, AppError> {
        let connector = tonic::transport::Endpoint::new(config.user_service_url.clone())
            .map_err(|e| AppError::Internal(format!("Invalid user service URL: {}", e)))?
            .connect()
            .await
            .map_err(|e| AppError::ServiceUnavailable(format!("Cannot connect to user service: {}", e)))?;

        let client = user::user_service_client::UserServiceClient::new(connector);
        Ok(Self { inner: client })
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

        let response = self
            .inner
            .clone()
            .get_user_by_email_and_password(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        let user = response.into_inner();
        Ok(user)
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

        let response = self
            .inner
            .clone()
            .create_user(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        let user = response.into_inner();
        Ok(user)
    }

    pub async fn get_user_by_id(&self, id: &str) -> Result<user::UserProfile, AppError> {
        let request = user::GetUserByIdRequest {
            id: Some(id.to_string()),
        };

        let response = self
            .inner
            .clone()
            .get_user_by_id(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        let user = response.into_inner();
        Ok(user)
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

        let response = self
            .inner
            .clone()
            .change_password(tonic::Request::new(request))
            .await
            .map_err(|e| AppError::from(e))?;

        let user = response.into_inner();
        Ok(user)
    }
}
