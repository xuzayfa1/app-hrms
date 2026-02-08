package uz.zero.user

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "auth-service")
interface AuthClient {
    @PostMapping("/api/auth/internal/register")
    fun registerInAuth(@RequestBody request: AuthRegisterRequest): AuthUserResponse
}

