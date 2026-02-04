package uz.zero.auth.model.responses

data class RoleResponse(
    var id: Long,
    var key: String,
    var name: String,
    var description: String,
    var level: Int
)