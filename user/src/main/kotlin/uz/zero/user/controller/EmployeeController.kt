package uz.zero.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.user.EmployeeCreateRequest
import uz.zero.user.EmployeeDetailResponse
import uz.zero.user.EmployeeResponse
import uz.zero.user.EmployeeRole
import uz.zero.user.EmployeeUpdateRequest
import uz.zero.user.services.EmployeeService

@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @GetMapping("/my-organizations")
    fun getMyOrganizations(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): ResponseEntity<List<EmployeeResponse>> {
        val userId = currentUserId?.toLongOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(employeeService.getEmployeesByUser(userId))
    }

    @GetMapping("/organization/{organizationId}")
    fun getEmployeesByOrganization(
        @PathVariable organizationId: Long,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<List<EmployeeResponse>> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)
            && !hasRole(currentUserId, currentOrgId, EmployeeRole.MANAGER)
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(employeeService.getEmployeesByOrganization(organizationId))
    }

    @GetMapping("/{id}")
    fun getEmployeeById(@PathVariable id: Long): ResponseEntity<EmployeeDetailResponse> {
        return ResponseEntity.ok(employeeService.getEmployeeById(id))
    }

    @PostMapping
    fun assignEmployee(
        @Valid @RequestBody request: EmployeeCreateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<EmployeeResponse> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.assignEmployeeToOrganization(request))
    }

    @PutMapping("/{id}")
    fun updateEmployee(
        @PathVariable id: Long,
        @Valid @RequestBody request: EmployeeUpdateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<EmployeeResponse> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(employeeService.updateEmployee(id, request))
    }

    @DeleteMapping("/{id}")
    fun removeEmployee(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<Void> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        employeeService.removeEmployee(id)
        return ResponseEntity.noContent().build()
    }

    private fun hasRole(userId: String?, orgId: String?, role: EmployeeRole): Boolean {
        val uid = userId?.toLongOrNull() ?: return false
        val oid = orgId?.toLongOrNull() ?: return false
        val currentRole = employeeService.getUserRoleInOrg(uid, oid) ?: return false
        return currentRole == role
    }
}
