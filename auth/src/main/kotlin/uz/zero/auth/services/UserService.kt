package uz.zero.auth.services

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.zero.auth.entities.User
import uz.zero.auth.enums.UserStatus
import uz.zero.auth.exceptions.UsernameAlreadyTakenException
import uz.zero.auth.mappers.UserEntityMapper
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.repositories.UserRepository
import uz.zero.auth.utils.userId

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserEntityMapper,
    private val passwordEncoder: PasswordEncoder
) {
    fun userMe(): UserInfoResponse {
        return userMapper
            .toUserInfo(userRepository.findByIdAndDeletedFalse(userId())!!)
    }

    @Transactional
    fun createEmployeeAccount(request: UserCreateRequest) {
        if(userRepository.existsByUsername(request.username))
            throw UsernameAlreadyTakenException()
        val newUser = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = request.role,
            status = UserStatus.ACTIVE
        )

        userRepository.save(newUser)
    }
}