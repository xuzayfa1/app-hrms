package uz.zero.auth.model.responses

data class UserPermissionsResponse(
    val group: PermissionGroupResponse,
    val permissions: Set<PermissionSelectResponse>
)