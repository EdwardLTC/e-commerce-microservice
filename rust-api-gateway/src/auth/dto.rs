#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct LoginRequest {
    /// User email address
    pub email: String,
    /// User password
    pub password: String,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct RegisterRequest {
    /// User email address
    pub email: String,
    /// User password
    pub password: String,
    /// User display name
    pub name: String,
}

#[derive(Debug, serde::Deserialize, utoipa::ToSchema)]
pub struct ChangePasswordRequest {
    /// Current password
    pub old_password: String,
    /// New password to set
    pub new_password: String,
}

#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct AuthTokenResponse {
    /// JWT access token
    pub access_token: String,
}

#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct UserProfileResponse {
    /// User ID
    pub id: String,
    /// User display name
    pub name: String,
    /// User email address
    pub email: String,
}
