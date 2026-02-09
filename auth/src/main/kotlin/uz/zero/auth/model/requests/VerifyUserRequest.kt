package uz.zero.auth.model.requests

data class VerifyUserRequest(
    val username: String,
    val password: String
)
