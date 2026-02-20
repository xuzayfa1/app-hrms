package uz.task

import java.util.Date
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime


data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        var OK = BaseMessage(0, "OK")
    }
}


data class CreateProjectRequest(
    val name: String,
)

data class UpdateProjectRequest(
    val id: Long,
    val name: String? = null,
    val status: Status? = null
)

data class ProjectResponse(
    val id: Long,
    val name: String,
    val organizationId: Long,
    val status: Status,
    val createdBy:Long?,
    val createdAt:Date?,
    val updatedAt:Date? = null,
    val updatedBy:Long? = null,
) {
    companion object {
        fun toResponse(p: Project) = ProjectResponse(
            id = p.id!!,
            name = p.name,
            organizationId = p.organizationId,
            status = p.status,
            createdBy = p.createdBy,
            createdAt = p.createdDate,
            updatedAt = p.modifiedDate,
            updatedBy = p.lastModifiedBy

        )
    }
}

data class CreateBoardRequest(
    val name: String,
    val projectId: Long
)

data class UpdateBoardRequest(
    val id: Long,
    val name: String? = null,
    val status: Status? = null
)

data class BoardResponse(
    val id: Long,
    val name: String,
    val projectId: Long,
    val status: Status,
    val createdBy:Long?,
    val createdAt:Date?,
    val updatedAt:Date? = null,
    val updatedBy:Long? = null,
) {
    companion object {
        fun toResponse(b: Board) = BoardResponse(
            id = b.id!!,
            name = b.name,
            projectId = b.project.id!!,
            status = b.status,
            createdBy = b.createdBy,
            createdAt = b.createdDate,
            updatedAt = b.modifiedDate,
            updatedBy = b.lastModifiedBy
        )
    }
}

data class CreateWorkflowRequest(
    val boardId:Long,
    val name: String
)

data class UpdateWorkflowRequest(
    val id: Long,
    val name: String
)

data class WorkflowResponse(
    val id: Long,
    val name: String,
    val organizationId: Long?,
    val boardId:Long?,
    val createdBy:Long?,
    val createdAt:Date?,
    val updatedAt:Date? = null,
    val updatedBy:Long? = null,
) {
    companion object {
        fun toResponse(w: Workflow) = WorkflowResponse(
            w.id!!,
            w.name,
            w.organizationId,
            w.board?.id,
            w.createdBy,
            w.createdDate,
            w.modifiedDate,
            w.lastModifiedBy
        )
    }
}

data class CreateStateRequest(
    val workflowId: Long,
    val name: String,
    val orderNumber: Long,
    val permission: Permission
)

data class UpdateStateRequest(
    val id: Long,
    val name: String? = null,
    val orderNumber: Long? = null,
    val permission: Permission? = null
)

data class StateResponse(
    val id: Long,
    val workflowId: Long,
    val name: String,
    val orderNumber: Long,
    val permission: Permission,
    val createdBy:Long?,
    val createdAt:Date?,
    val updatedAt:Date? = null,
    val updatedBy:Long? = null,
) {
    companion object {
        fun toResponse(s: State) = StateResponse(
            id = s.id!!,
            workflowId = s.workflow.id!!,
            name = s.name,
            orderNumber = s.orderNumber,
            permission = s.permission,
            createdBy = s.createdBy,
            createdAt = s.createdDate,
            updatedAt = s.modifiedDate,
            updatedBy = s.lastModifiedBy
        )
    }
}

data class CreateTaskRequest(
    val boardId: Long,
    val stateId: Long,
    val title: String,
    val description: String,
    val medias: List<String>? = emptyList(),
    val deadline: Date? = null,
)

data class UpdateTaskRequest(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val deadline: Date? = null,
    val medias: List<String>? = null,
)

data class ChangeTaskStateRequest(
    val taskId: Long,
    val stateId: Long
)

data class AssignTaskRequest(
    val taskId: Long,
    val employeeId: Long
)

data class RemoveAssigneeRequest(
    val taskId: Long,
    val employeeId: Long
)


data class StateDto(
    val id: Long,
    val name: String,
)
data class TaskResponse(
    val id: Long,
    val boardId: Long,
    val state: StateDto,
    val title: String,
    val description: String,
    val ownerId: Long,
    val deadline: Date?,
    val createdBy:Long?,
    val createdAt:Date?,
    val updatedAt:Date? = null,
    val updatedBy:Long? = null,
) {
    companion object {
        fun toResponse(t: Task) = TaskResponse(
            id = t.id!!,
            boardId = t.board.id!!,
            state = StateDto(
                id = t.state.id!!,
                name = t.state.name
            ),
            title = t.title,
            description = t.description,
            ownerId = t.ownerId,
            deadline = t.deadline,
            createdBy = t.createdBy,
            createdAt = t.createdDate,
            updatedAt = t.modifiedDate,
            updatedBy = t.lastModifiedBy
        )
    }
}
data class TaskResponseMedia(
    val id: Long,
    val boardId: Long,
    val stateId: Long,
    val title: String,
    val description: String,
    val ownerId: Long,
    val medias: List<String>?,
    val deadline: Date?
) {
    companion object {
        fun toResponse(t: Task,media:List<String>) = TaskResponseMedia(
            id = t.id!!,
            boardId = t.board.id!!,
            stateId = t.state.id!!,
            title = t.title,
            description = t.description,
            ownerId = t.ownerId,
            deadline = t.deadline,
            medias = media
        )
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

data class TaskActionEvent(
    val type: TaskActionType,
    val orgId: Long,
    val taskId: Long,
    val boardId: Long,

    val actorEmployeeId: Long,
    val actorName: String,

    val recipients: List<Long>,

    val fromStateId: Long? = null,
    val toStateId: Long? = null,

    val newTitle: String? = null,
    val assigneeEmployeeId: Long? = null,
    val newFileAttach: List<String>? = null,
    val newDeadline: Date? = null,

)

