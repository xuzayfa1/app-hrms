package uz.zero.user

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import java.util.Optional

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
}


class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    @Transactional
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}


interface UserRepository : BaseRepository<User> {

    fun findByAuthUserId(authUserId: Long): Optional<User>

    fun findByUsername(username: String): Optional<User>

    fun findByEmail(email: String): Optional<User>

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.deleted = false")
    fun findAllActive(): List<User>

    fun findByAuthUserIdAndDeletedFalse(authUserId: Long): Optional<User>
}

interface OrganizationRepository : BaseRepository<Organization> {

    @Query("SELECT o FROM Organization o WHERE o.isActive = true AND o.deleted = false")
    fun findAllActive(): List<Organization>

    fun findByCreatedBy(createdBy: Long): List<Organization>
}

interface EmployeeRepository : BaseRepository<Employee> {

    fun findByUserId(userId: Long): List<Employee>

    fun findByOrganizationId(organizationId: Long): List<Employee>

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId AND e.organization.id = :organizationId AND e.deleted = false")
    fun findByUserIdAndOrganizationId(userId: Long, organizationId: Long): Optional<Employee>

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId AND e.isActive = true AND e.deleted = false")
    fun findActiveByUserId(userId: Long): List<Employee>

    @Query("SELECT e FROM Employee e WHERE e.organization.id = :organizationId AND e.role = :role AND e.isActive = true AND e.deleted = false")
    fun findByOrganizationIdAndRole(organizationId: Long, role: EmployeeRole): List<Employee>

    fun existsByUserIdAndOrganizationIdAndDeletedFalse(userId: Long, organizationId: Long): Boolean
}
