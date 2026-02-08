package uz.zero.user.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uz.zero.user.*

interface UserService {
    fun getAllUsers(): List<UserResponse>
    fun getUserById(id: Long): UserResponse
    fun createUser(request: UserCreateRequest): UserResponse
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse
    fun deleteUser(id: Long)
    fun switchOrganization(userId: Long, orgId: Long): UserResponse
    fun getUserAuthDetails(authUserId: Long): UserAuthDto
}


@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val authClient: AuthClient
) : UserService {

    override fun getUserAuthDetails(authUserId: Long): UserAuthDto {
        val user = userRepository.findByAuthUserId(authUserId)
            .orElseThrow {UserNotFoundException()}

        val currentOrgId = user.currentOrgId

        val role = if(currentOrgId != null ){
            employeeRepository.findByUserIdAndOrganizationId(user.id!!, currentOrgId)
                .map { it.role.name }
                .orElse(null)
        }else{
            null
        }

        return UserAuthDto(
            userId = user.id!!,
            currentOrgId = currentOrgId,
            role = role ?: "USER"
        )
    }

    @Transactional
    override fun switchOrganization(userId: Long, orgId: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException("User not found")

        // Tekshirish: User haqiqatda shu tashkilotda ishlaydimi?
        val isMember = employeeRepository.existsByUserIdAndOrganizationIdAndDeletedFalse(userId, orgId)
        if (!isMember) {
            throw ForbiddenException("Siz ushbu tashkilot a'zosi emassiz!")
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
        val authResponse = authClient.registerInAuth(AuthRegisterRequest(request.username,"USER"))

        val user = User(
            username = request.username,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            authUserId = authResponse.id
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
        createdAt = createdDate!!,
        updatedAt = createdDate!!
    )
}
