package uz.zero.user

import java.time.LocalDateTime
import java.util.Date

data class BaseMessage(
    val code: Int,
    val message: String?
)

data class VerifyUserRequest(
    val username: String,
    val password: String
)

data class UserAuthDto(
    val id: Long,
    val username: String,
    val role: String,
    val deleted: Boolean,
    val employeeId: Long?,
    val employeeRole: String?,
    val currentOrganizationId: Long?
)

data class UserCreateRequest(
    val username: String,
    val password: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

data class UserUpdateRequest(
    val email: String?,
    val firstName: String?,
    val lastName: String?
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

data class OrganizationCreateRequest(
    val name: String,
    val description: String?
)

data class OrganizationUpdateRequest(
    val name: String?,
    val description: String?
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

data class EmployeeCreateRequest(
    val userId: Long,
    val organizationId: Long,
    val role: EmployeeRole = EmployeeRole.EMPLOYEE
)

data class EmployeeUpdateRequest(
    val role: EmployeeRole?,
    val isActive: Boolean?
)

data class EmployeeResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val userFullName: String,
    val organizationId: Long,
    val organizationName: String,
    val role: EmployeeRole,
    val isActive: Boolean,
    val joinedAt: LocalDateTime
)

data class EmployeeDetailResponse(
    val id: Long,
    val user: UserResponse,
    val organization: OrganizationResponse,
    val role: EmployeeRole,
    val isActive: Boolean,
    val joinedAt: LocalDateTime
)


