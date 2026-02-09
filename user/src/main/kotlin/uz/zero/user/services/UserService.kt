package uz.zero.user.services

import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import uz.zero.user.*

interface UserService {
    fun getAllUsers(): List<UserResponse>
    fun getUserById(id: Long): UserResponse
    fun createUser(request: UserCreateRequest): UserResponse
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse
    fun deleteUser(id: Long)
    fun switchOrganization(userId: Long, orgId: Long): UserResponse
    fun verifyUser(request: VerifyUserRequest): UserAuthDto
}


@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    @Transactional
    override fun verifyUser(request: VerifyUserRequest): UserAuthDto {
        val user = userRepository.findByUsernameAndDeletedFalse(request.username)
            ?: throw UserNotFoundException("User not found")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UnauthorizedException("Invalid password")
        }

        if (!user.isActive) {
            throw InactiveUserException("User is not active")
        }

        val currentOrgId = user.currentOrgId

        val employee = if (currentOrgId != null) {
            employeeRepository.findActiveByUserIdAndOrgId(user.id!!, currentOrgId).orElse(null)
        } else {
            null
        }

        return UserAuthDto(
            id = user.id!!,
            username = user.username,
            role = employee?.role?.name ?: "USER",
            deleted = user.deleted,
            employeeId = employee?.id,
            employeeRole = employee?.role?.name,
            currentOrganizationId = currentOrgId
        )
    }

    @Transactional
    override fun switchOrganization(userId: Long, orgId: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException("User not found")

        val employee = employeeRepository.findActiveByUserIdAndOrgId(userId, orgId)
            .orElseThrow { ForbiddenException("Siz ushbu tashkilotda aktiv xodim emassiz!") }

        if (!employee.organization.isActive) {
            throw InactiveOrganizationException("Tashkilot aktiv emas!")
        }

        user.currentOrgId = orgId
        return userRepository.save(user).toResponse()
    }

    @Transactional
    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAllActive().map { it.toResponse() }
    }

    @Transactional
    override fun getUserById(id: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException("User not found with id: $id")
        return user.toResponse()
    }

    @Transactional
    override fun createUser(request: UserCreateRequest): UserResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw UserAlreadyExistsException("Username already exists: ${request.username}")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw UserAlreadyExistsException("Email already exists: ${request.email}")
        }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName
        )

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    @Transactional
    override fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException("User not found with id: $id")

        request.email?.let {
            if (userRepository.existsByEmail(it) && user.email != it) {
                throw UserAlreadyExistsException("Email already exists: $it")
            }
            user.email = it
        }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    @Transactional
    override fun deleteUser(id: Long) {
        userRepository.trash(id)
            ?: throw UserNotFoundException("User not found with id: $id")
    }

    private fun User.toResponse() = UserResponse(
        id = id!!,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        isActive = isActive,
        currentOrgId = currentOrgId,
        createdAt = createdDate!!,
        updatedAt = updatedDate ?: createdDate!!
    )
}
