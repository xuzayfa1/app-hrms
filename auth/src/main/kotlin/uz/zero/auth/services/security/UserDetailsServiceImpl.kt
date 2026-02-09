package uz.zero.auth.services.security

import org.springframework.stereotype.Service
import uz.zero.auth.feigns.UserFeignClient
import uz.zero.auth.model.requests.VerifyUserRequest
import uz.zero.auth.model.security.CustomUserDetails


@Service
class UserVerifyService(
    private val userFeignClient: UserFeignClient
) {
    fun verify(username: String, password: String): CustomUserDetails {
        val user = userFeignClient.verifyUser(VerifyUserRequest(username, password))

        return CustomUserDetails(
            id = user.id,
            username = user.username,
            role = user.role,
            enabled = !user.deleted,
            employeeId = user.employeeId,
            employeeRole = user.employeeRole,
            currentOrganizationId = user.currentOrganizationId
        )
    }
}