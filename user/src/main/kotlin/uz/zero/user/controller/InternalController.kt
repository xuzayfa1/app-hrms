package uz.zero.user.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uz.zero.user.UserAuthDto
import uz.zero.user.VerifyUserRequest
import uz.zero.user.services.UserService

@RestController
class InternalController(
    private val userService: UserService
) {

    @PostMapping("/login")
    fun verifyUser(@RequestBody request: VerifyUserRequest): UserAuthDto {
        return userService.verifyUser(request)
    }
}
