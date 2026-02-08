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
import uz.zero.user.UserAuthDto
import uz.zero.user.UserCreateRequest
import uz.zero.user.UserResponse
import uz.zero.user.services.UserService
import uz.zero.user.UserUpdateRequest

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/details/{authUserId}")
    fun getUserDetailsForAuth(@PathVariable authUserId: Long): UserAuthDto {
        return userService.getUserAuthDetails(authUserId)
    }

    @GetMapping
    fun getAllUsers(): List<UserResponse> {
        val users = userService.getAllUsers()
        return users
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse {
        val user = userService.getUserById(id)
        return user
    }

    @PostMapping
    fun createUser(
        @Valid @RequestBody request: UserCreateRequest,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<UserResponse> {
        // Only ADMIN can create users
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest,
        @RequestHeader("X-User-Role", required = false) userRole: String?,
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): ResponseEntity<UserResponse> {
        // ADMIN can update any user, regular users can only update themselves
        if (userRole != "ADMIN" && currentUserId != id.toString()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<Void> {
        // Only ADMIN can delete users
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/switch-organization/{orgId}")
    fun switchOrg(
        @PathVariable orgId: Long,
        @RequestHeader("X-User-Id") currentUserId: String,
    ): UserResponse{
        val updateUser = userService.switchOrganization(currentUserId.toLong(),orgId)
        return updateUser
    }
}