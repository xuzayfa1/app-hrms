package uz.zero.notification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.Date

data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        var OK = BaseMessage(0, "OK")
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoResponse(
    @JsonProperty("uid")
    val id: Long,

    @JsonProperty("sub")
    val username: String,

    @JsonProperty("rol")
    val role: String,

    val enabled: Boolean = true,

    @JsonProperty("eid")
    val employeeId: Long,

    @JsonProperty("per")
    val employeeRole: String,

    @JsonProperty("oid")
    val currentOrganizationId: Long
)

data class TaskEvent(

    val orgName: String,
    val taskId: Long,

    val ownerEmployeeId: Long,
    val ownerName: String,

    val assignees: List<Long>,
    val projectName: String,
    val fromState: String,
    val toState: String? = null,

    val newTitle: String,
    val assigneeEmployeeId: Long? = null,
    val newFileAttach: List<String>? = null,
    val newDeadline: Date? = null,

    val createdDate: Date? = null,

    )

data class EmployeeDetailResponse(
    val id: Long,
    val user: UserResponse,
    val organization: OrganizationResponse,
    val role: EmployeeRole,
    val isActive: Boolean,
    val joinedAt: LocalDateTime
)

data class OrganizationResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val createdBy: Long?,
    val createdAt: Date,
    val updatedAt: Date
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean,
    val currentOrgId: Long?,
    val createdAt: Date,
    val updatedAt: Date
)

