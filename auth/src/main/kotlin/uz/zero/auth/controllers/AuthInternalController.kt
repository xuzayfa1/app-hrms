package uz.zero.auth.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.services.UserService


@RestController
@RequestMapping("/api/auth/internal")
class AuthInternalController(
    private val userService: UserService
) {
    @PostMapping("/create-user")
    fun createUser(@RequestBody request: UserCreateRequest) {
        userService.createEmployeeAccount(request)
    }

    @GetMapping("/user-info")
    fun getUserInfo() = userService.userMe()

    @GetMapping("/test")
    fun testUserInfo() = "test info user"

}