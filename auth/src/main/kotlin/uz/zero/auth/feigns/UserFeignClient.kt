package uz.zero.auth.feigns

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import uz.zero.auth.model.requests.VerifyUserRequest
import uz.zero.auth.model.responses.UserAuthDto

@FeignClient(name = "user-service", url = "\${services.hosts.user}")
interface UserFeignClient {

    @PostMapping("/login")
    fun verifyUser(@RequestBody request: VerifyUserRequest): UserAuthDto
}