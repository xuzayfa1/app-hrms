package uz.zero.user.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uz.zero.user.Organization
import uz.zero.user.OrganizationCreateRequest
import uz.zero.user.OrganizationNotFoundException
import uz.zero.user.OrganizationRepository
import uz.zero.user.OrganizationResponse
import uz.zero.user.OrganizationUpdateRequest

interface OrganizationService {
    fun getAllOrganizations(): List<OrganizationResponse>
    fun getOrganizationById(id: Long): OrganizationResponse
    fun getOrganizationsByCreator(createdBy: Long): List<OrganizationResponse>
    fun createOrganization(request: OrganizationCreateRequest, createdBy: Long): OrganizationResponse
    fun updateOrganization(id: Long, request: OrganizationUpdateRequest): OrganizationResponse
    fun deleteOrganization(id: Long)
}

@Service
class OrganizationServiceImpl(
    private val organizationRepository: OrganizationRepository
) : OrganizationService {

    @Transactional
    override fun getAllOrganizations(): List<OrganizationResponse> {
        return organizationRepository.findAllActive().map { it.toResponse() }
    }

    @Transactional
    override fun getOrganizationById(id: Long): OrganizationResponse {
        val organization = organizationRepository.findByIdAndDeletedFalse(id)
            ?: throw OrganizationNotFoundException("Organization not found with id: $id")
        return organization.toResponse()
    }

    @Transactional
    override fun getOrganizationsByCreator(createdBy: Long): List<OrganizationResponse> {
        return organizationRepository.findByCreatedBy(createdBy).map { it.toResponse() }
    }

    @Transactional
    override fun createOrganization(request: OrganizationCreateRequest, createdBy: Long): OrganizationResponse {
        val organization = Organization(
            name = request.name,
            description = request.description
        ).apply {
            this.createdBy = createdBy
        }

        val savedOrganization = organizationRepository.save(organization)
        return savedOrganization.toResponse()
    }

    @Transactional
    override fun updateOrganization(id: Long, request: OrganizationUpdateRequest): OrganizationResponse {
        val organization = organizationRepository.findByIdAndDeletedFalse(id)
            ?: throw OrganizationNotFoundException("Organization not found with id: $id")

        request.name?.let { organization.name = it }
        request.description?.let { organization.description = it }

        val savedOrganization = organizationRepository.save(organization)
        return savedOrganization.toResponse()
    }

    @Transactional
    override fun deleteOrganization(id: Long) {
        organizationRepository.trash(id)
            ?: throw OrganizationNotFoundException("Organization not found with id: $id")
    }

    private fun Organization.toResponse() = OrganizationResponse(
        id = id!!,
        name = name,
        description = description,
        isActive = isActive,
        createdBy = createdBy,
        createdAt = createdDate!!,
        updatedAt = createdDate!!
    )
}
