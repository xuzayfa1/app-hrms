package uz.zero.notification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

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

data class TaskStateChangedEvent(
    val eventId: String,
    val eventType: String,
    val taskId: Long,
    val oldState: String?,
    val newState: String?,
    val assignedUserIds: List<Long>,
    val changedAt: Instant,

    val organizationName: String?,
    val projectName: String?,
    val ownerName: String?,
    val title: String?
)
data class TaskEvent(
    val taskId: Long,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)



