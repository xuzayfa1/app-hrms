package uz.zero.auth.model.responses

data class OnePermissionGroupResponse(
    val id: Long,
    val key: String,
    val name: String,
    val description: String,
    val permissions: List<OnePermissionResponse>
)