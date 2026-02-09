package uz.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date


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
    val status: Status
) {
    companion object {
        fun toResponse(p: Project) = ProjectResponse(
            id = p.id!!,
            name = p.name,
            organizationId = p.organizationId,
            status = p.status
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
    val status: Status
) {
    companion object {
        fun toResponse(b: Board) = BoardResponse(
            id = b.id!!,
            name = b.name,
            projectId = b.project.id!!,
            status = b.status
        )
    }
}

data class CreateWorkflowRequest(
    val name: String
)

data class UpdateWorkflowRequest(
    val id: Long,
    val name: String
)

data class WorkflowResponse(
    val id: Long,
    val name: String,
    val organizationId: Long
) {
    companion object {
        fun toResponse(w: Workflow) = WorkflowResponse(
            w.id!!,
            w.name,
            w.organizationId!!
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
    val terminal: Boolean,
    val orderNumber: Long,
    val permission: Permission
) {
    companion object {
        fun toResponse(s: State) = StateResponse(
            id = s.id!!,
            workflowId = s.workflow.id!!,
            name = s.name,
            terminal = s.terminal,
            orderNumber = s.orderNumber,
            permission = s.permission
        )
    }
}

data class CreateTaskRequest(
    val boardId: Long,
    val stateId: Long,
    val title: String,
    val description: String,
    val deadline: Date,
)

data class UpdateTaskRequest(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val deadline: Date? = null
)

data class ChangeTaskStateRequest(
    val taskId: Long,
    val stateId: Long
)

data class TaskResponse(
    val id: Long,
    val boardId: Long,
    val stateId: Long,
    val title: String,
    val description: String,
    val ownerId: Long,
    val deadline: Date?
) {
    companion object {
        fun toResponse(t: Task) = TaskResponse(
            id = t.id!!,
            boardId = t.board.id!!,
            stateId = t.state.id!!,
            title = t.title,
            description = t.description,
            ownerId = t.ownerId,
            deadline = t.deadline
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)


