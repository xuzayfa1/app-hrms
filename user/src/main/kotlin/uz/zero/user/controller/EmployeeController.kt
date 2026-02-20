package uz.zero.user.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import uz.zero.user.*
import uz.zero.user.services.EmployeeService

@RestController
@RequestMapping("employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @GetMapping("/my-organizations")
    fun getMyOrganizations(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): List<EmployeeResponse> {
        val userId = currentUserId?.toLongOrNull()
            ?: throw UnauthorizedException()

        return employeeService.getEmployeesByUser(userId)
    }

    @GetMapping("/organization/{organizationId}")
    fun getEmployeesByOrganization(
        @PathVariable organizationId: Long,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): List<EmployeeResponse> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)
            && !hasRole(currentUserId, currentOrgId, EmployeeRole.MANAGER)
        ) {
            throw ForbiddenException()
        }

        return employeeService.getEmployeesByOrganization(organizationId)
    }

    @GetMapping("/{id}")
    fun getEmployeeById(@PathVariable id: Long): EmployeeDetailResponse {
        return employeeService.getEmployeeById(id)
    }

    @PostMapping
    fun assignEmployee(
        @Valid @RequestBody request: EmployeeCreateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): EmployeeResponse {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN))
            throw ForbiddenException()

        return employeeService.assignEmployeeToOrganization(request)
    }

    @PutMapping("/{id}")
    fun updateEmployee(
        @PathVariable id: Long,
        @Valid @RequestBody request: EmployeeUpdateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): EmployeeResponse {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN))
            throw ForbiddenException()

        return employeeService.updateEmployee(id, request)
    }

    @DeleteMapping("/{id}")
    fun removeEmployee(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ) {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) throw ForbiddenException()

        employeeService.removeEmployee(id)
    }

    private fun hasRole(userId: String?, orgId: String?, role: EmployeeRole): Boolean {
        val uid = userId?.toLongOrNull() ?: return false
        val oid = orgId?.toLongOrNull() ?: return false
        val currentRole = employeeService.getUserRoleInOrg(uid, oid) ?: return false
        return currentRole == role
    }
}
