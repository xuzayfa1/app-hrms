package uz.zero.user.controller

import org.springframework.web.bind.annotation.*
import uz.zero.user.UserAuthDto
import uz.zero.user.UserRepository
import uz.zero.user.VerifyUserRequest
import uz.zero.user.services.UserService

@RestController
class InternalController(
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    @PostMapping("/login")
    fun verifyUser(@RequestBody request: VerifyUserRequest): UserAuthDto {
        return userService.verifyUser(request)
    }
}


