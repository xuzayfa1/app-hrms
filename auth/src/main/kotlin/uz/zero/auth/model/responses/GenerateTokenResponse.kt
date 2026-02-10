package uz.zero.auth.model.responses

data class GenerateTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)