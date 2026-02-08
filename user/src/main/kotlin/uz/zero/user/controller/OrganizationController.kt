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
import uz.zero.user.OrganizationCreateRequest
import uz.zero.user.OrganizationResponse
import uz.zero.user.services.OrganizationService
import uz.zero.user.OrganizationUpdateRequest

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @GetMapping
    fun getAllOrganizations(
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<List<OrganizationResponse>> {
        // Only ADMIN can see all organizations
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val organizations = organizationService.getAllOrganizations()
        return ResponseEntity.ok(organizations)
    }

    @GetMapping("/{id}")
    fun getOrganizationById(@PathVariable id: Long): ResponseEntity<OrganizationResponse> {
        val organization = organizationService.getOrganizationById(id)
        return ResponseEntity.ok(organization)
    }

    @GetMapping("/my-organizations")
    fun getMyOrganizations(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): ResponseEntity<List<OrganizationResponse>> {
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val organizations = organizationService.getOrganizationsByCreator(currentUserId.toLong())
        return ResponseEntity.ok(organizations)
    }

    @PostMapping
    fun createOrganization(
        @Valid @RequestBody request: OrganizationCreateRequest,
        @RequestHeader("X-User-Role", required = false) userRole: String?,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): ResponseEntity<OrganizationResponse> {
        // Only ADMIN can create organizations
        if (userRole != "ADMIN" || currentUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val organization = organizationService.createOrganization(request, currentUserId.toLong())
        return ResponseEntity.status(HttpStatus.CREATED).body(organization)
    }

    @PutMapping("/{id}")
    fun updateOrganization(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrganizationUpdateRequest,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<OrganizationResponse> {
        // Only ADMIN can update organizations
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val organization = organizationService.updateOrganization(id, request)
        return ResponseEntity.ok(organization)
    }

    @DeleteMapping("/{id}")
    fun deleteOrganization(
        @PathVariable id: Long,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<Void> {
        // Only ADMIN can delete organizations
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        organizationService.deleteOrganization(id)
        return ResponseEntity.noContent().build()
    }
}