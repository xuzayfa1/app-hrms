package uz.zero.auth.model.responses

data class PermissionGroupResponse(
    val id: Long,
    val key: String,
    val name: String,
    val description: String
)