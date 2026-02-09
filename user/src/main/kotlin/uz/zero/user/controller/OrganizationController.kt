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
import uz.zero.user.EmployeeRole
import uz.zero.user.OrganizationCreateRequest
import uz.zero.user.OrganizationResponse
import uz.zero.user.OrganizationUpdateRequest
import uz.zero.user.services.EmployeeService
import uz.zero.user.services.OrganizationService

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    private val organizationService: OrganizationService,
    private val employeeService: EmployeeService
) {

    @GetMapping
    fun getAllOrganizations(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<List<OrganizationResponse>> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(organizationService.getAllOrganizations())
    }

    @GetMapping("/{id}")
    fun getOrganizationById(@PathVariable id: Long): ResponseEntity<OrganizationResponse> {
        return ResponseEntity.ok(organizationService.getOrganizationById(id))
    }

    @GetMapping("/my-organizations")
    fun getMyOrganizations(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): ResponseEntity<List<OrganizationResponse>> {
        val userId = currentUserId?.toLongOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(organizationService.getOrganizationsByUserId(userId))
    }

    @PostMapping
    fun createOrganization(
        @Valid @RequestBody request: OrganizationCreateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<OrganizationResponse> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val organization = organizationService.createOrganization(request, currentUserId!!.toLong())
        return ResponseEntity.status(HttpStatus.CREATED).body(organization)
    }

    @PutMapping("/{id}")
    fun updateOrganization(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrganizationUpdateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<OrganizationResponse> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(organizationService.updateOrganization(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteOrganization(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<Void> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        organizationService.deleteOrganization(id)
        return ResponseEntity.noContent().build()
    }

    private fun hasRole(userId: String?, orgId: String?, role: EmployeeRole): Boolean {
        val uid = userId?.toLongOrNull() ?: return false
        val oid = orgId?.toLongOrNull() ?: return false
        val currentRole = employeeService.getUserRoleInOrg(uid, oid) ?: return false
        return currentRole == role
    }
}
