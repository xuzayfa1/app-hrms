package uz.zero.user.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uz.zero.user.Employee
import uz.zero.user.EmployeeAlreadyExistsException
import uz.zero.user.EmployeeCreateRequest
import uz.zero.user.EmployeeRole
import uz.zero.user.EmployeeDetailResponse
import uz.zero.user.EmployeeNotFoundException
import uz.zero.user.EmployeeRepository
import uz.zero.user.EmployeeResponse
import uz.zero.user.EmployeeUpdateRequest
import uz.zero.user.InactiveOrganizationException
import uz.zero.user.InactiveUserException
import uz.zero.user.OrganizationNotFoundException
import uz.zero.user.OrganizationRepository
import uz.zero.user.OrganizationResponse
import uz.zero.user.UserNotFoundException
import uz.zero.user.UserRepository
import uz.zero.user.UserResponse

interface EmployeeService {
    fun getEmployeesByUser(userId: Long): List<EmployeeResponse>
    fun getEmployeesByOrganization(organizationId: Long): List<EmployeeResponse>
    fun getEmployeeById(id: Long): EmployeeDetailResponse
    fun assignEmployeeToOrganization(request: EmployeeCreateRequest): EmployeeResponse
    fun updateEmployee(id: Long, request: EmployeeUpdateRequest): EmployeeResponse
    fun removeEmployee(id: Long)
    fun getUserRoleInOrg(userId: Long, orgId: Long): EmployeeRole?
}


@Service
class EmployeeServiceImpl(
    private val employeeRepository: EmployeeRepository,
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository
) : EmployeeService {

    @Transactional
    override fun getEmployeesByUser(userId: Long): List<EmployeeResponse> {
        return employeeRepository.findActiveByUserId(userId).map { it.toResponse() }
    }

    @Transactional
    override fun getEmployeesByOrganization(organizationId: Long): List<EmployeeResponse> {
        return employeeRepository.findByOrganizationId(organizationId).map { it.toResponse() }
    }

    @Transactional
    override fun getEmployeeById(id: Long): EmployeeDetailResponse {
        val employee = employeeRepository.findByIdAndDeletedFalse(id)
            ?: throw EmployeeNotFoundException("Employee not found with id: $id")
        return employee.toDetailResponse()
    }

    @Transactional
    override fun assignEmployeeToOrganization(request: EmployeeCreateRequest): EmployeeResponse {
        val user = userRepository.findByIdAndDeletedFalse(request.userId)
            ?: throw UserNotFoundException("User not found with id: ${request.userId}")

        val organization = organizationRepository.findByIdAndDeletedFalse(request.organizationId)
            ?: throw OrganizationNotFoundException("Organization not found with id: ${request.organizationId}")

        if (employeeRepository.existsByUserIdAndOrganizationIdAndDeletedFalse(request.userId, request.organizationId)) {
            throw EmployeeAlreadyExistsException(
                "User ${request.userId} is already assigned to organization ${request.organizationId}"
            )
        }

        if (!user.isActive) {
            throw InactiveUserException("Cannot assign inactive user to organization")
        }

        if (!organization.isActive) {
            throw InactiveOrganizationException("Cannot assign user to inactive organization")
        }

        val employee = Employee(
            user = user,
            organization = organization,
            role = request.role
        )

        val savedEmployee = employeeRepository.save(employee)

        if (user.currentOrgId == null) {
            user.currentOrgId = organization.id
            userRepository.save(user)
        }

        return savedEmployee.toResponse()
    }

    override fun getUserRoleInOrg(userId: Long, orgId: Long): EmployeeRole? {
        return employeeRepository.findActiveByUserIdAndOrgId(userId, orgId)
            .map { it.role }
            .orElse(null)
    }

    @Transactional
    override fun updateEmployee(id: Long, request: EmployeeUpdateRequest): EmployeeResponse {
        val employee = employeeRepository.findByIdAndDeletedFalse(id)
            ?: throw EmployeeNotFoundException("Employee not found with id: $id")

        request.role?.let { employee.role = it }
        request.isActive?.let { employee.isActive = it }

        val savedEmployee = employeeRepository.save(employee)
        return savedEmployee.toResponse()
    }

    @Transactional
    override fun removeEmployee(id: Long) {
        employeeRepository.trash(id)
            ?: throw EmployeeNotFoundException("Employee not found with id: $id")
    }

    private fun Employee.toResponse() = EmployeeResponse(
        id = id!!,
        userId = user.id!!,
        username = user.username,
        userFullName = "${user.firstName} ${user.lastName}",
        organizationId = organization.id!!,
        organizationName = organization.name,
        role = role,
        isActive = isActive,
        joinedAt = joinedAt
    )

    private fun Employee.toDetailResponse() = EmployeeDetailResponse(
        id = id!!,
        user = UserResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive,
            currentOrgId = user.currentOrgId,
            createdAt = user.createdDate!!,
            updatedAt = user.updatedDate ?: user.createdDate!!
        ),
        organization = OrganizationResponse(
            id = organization.id!!,
            name = organization.name,
            description = organization.description,
            isActive = organization.isActive,
            createdBy = organization.createdBy,
            createdAt = organization.createdDate!!,
            updatedAt = organization.updatedDate ?: organization.createdDate!!
        ),
        role = role,
        isActive = isActive,
        joinedAt = joinedAt
    )
}
