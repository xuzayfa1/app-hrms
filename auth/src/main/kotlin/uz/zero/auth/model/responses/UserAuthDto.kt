package uz.zero.auth.model.responses

data class UserAuthDto(
    val id: Long,
    val username: String,
    val role: String,
    val deleted: Boolean,
    val employeeId: Long,
    val employeeRole: String,
    val currentOrganizationId: Long
)
