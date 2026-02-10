package uz.zero.auth.model.requests

data class GenerateTokenRequest(
    val userId: Long,
    val username: String,
    val role: String,
    val organizationId: Long,
    val employeeId: Long,
    val employeeRole: String
)