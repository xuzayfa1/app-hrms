package uz.zero.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.user.EmployeeRole
import uz.zero.user.UserAuthDto
import uz.zero.user.UserCreateRequest
import uz.zero.user.UserResponse
import uz.zero.user.UserUpdateRequest
import uz.zero.user.services.EmployeeService
import uz.zero.user.services.UserService

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val employeeService: EmployeeService
) {

    @GetMapping("/details/{authUserId}")
    fun getUserDetailsForAuth(@PathVariable authUserId: Long): UserAuthDto {
        return userService.getUserAuthDetails(authUserId)
    }

    @GetMapping
    fun getAllUsers(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<List<UserResponse>> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        return ResponseEntity.ok(userService.getAllUsers())
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse {
        return userService.getUserById(id)
    }

    @PostMapping
    fun createUser(
        @Valid @RequestBody request: UserCreateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<UserResponse> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<UserResponse> {
        val userId = currentUserId?.toLongOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val isAdmin = hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)
        if (!isAdmin && userId != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?,
        @RequestHeader("X-Org-Id", required = false) currentOrgId: String?
    ): ResponseEntity<Void> {
        if (!hasRole(currentUserId, currentOrgId, EmployeeRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/switch-organization/{orgId}")
    fun switchOrg(
        @PathVariable orgId: Long,
        @RequestHeader("X-User-Id") currentUserId: String
    ): UserResponse {
        return userService.switchOrganization(currentUserId.toLong(), orgId)
    }

    private fun hasRole(userId: String?, orgId: String?, role: EmployeeRole): Boolean {
        val uid = userId?.toLongOrNull() ?: return false
        val oid = orgId?.toLongOrNull() ?: return false
        val currentRole = employeeService.getUserRoleInOrg(uid, oid) ?: return false
        return currentRole == role
    }
}
