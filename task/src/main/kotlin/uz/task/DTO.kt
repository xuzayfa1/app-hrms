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

data class IdDto(
    val id: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)


