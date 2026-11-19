#[derive(Debug, serde::Serialize, utoipa::ToSchema)]
pub struct UserProfileResponse {
    /// User ID
    pub id: String,
    /// User display name
    pub name: String,
    /// User email address
    pub email: String,
}
