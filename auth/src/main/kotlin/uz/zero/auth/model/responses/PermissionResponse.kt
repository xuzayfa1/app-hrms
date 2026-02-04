package uz.zero.auth.model.responses


data class PermissionResponse(
    val id: Long,
    val key: String,
    val name: String,
    val description: String
)