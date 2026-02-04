package uz.zero.auth.mappers

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.model.responses.UserResponse
import uz.zero.auth.entities.User
import uz.zero.auth.enums.Role

@Component
class UserEntityMapper(
    private val passwordEncoder: PasswordEncoder,
) {

    fun toUserInfo(user: User): UserInfoResponse {
        return UserInfoResponse(
            id = user.id!!,
            username = user.username,
            role = user.role.name,
        )
    }

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            username = user.username,
            role = user.role.name,
            status = user.status
        )
    }

    fun toEntity(request: UserCreateRequest, role: Role) = request.run {
        User(username, passwordEncoder.encode(password), role)
    }
}