package uz.zero.user


import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "auth-service", url = "\${services.hosts.auth}")
interface AuthServiceClient {
    @PostMapping("/internal/token/generate")
    fun generateToken(@RequestBody request: GenerateTokenRequest): GenerateTokenResponse
}

data class GenerateTokenRequest(
    val userId: Long,
    val username: String,
    val role: String,
    val organizationId: Long,
    val employeeId: Long,
    val employeeRole: String
)

data class GenerateTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)

// BaseMessage wrapper generic bilan
//data class BaseMessage1<T>(
//    val success: Boolean,
//    val message: String?,
//    val data: T?
//) {
//    companion object {
//        fun <T> ok(data: T) = BaseMessage1(true, null, data)
//        fun <T> error(message: String) = BaseMessage1<T>(false, message, null)
//    }
//}