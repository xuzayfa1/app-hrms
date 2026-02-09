package uz.task

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
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
    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)
    @Transactional
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}

interface ProjectRepository : BaseRepository<Project>{
    fun findAllByOrganizationIdAndDeletedFalse(orgId: Long, pageable: Pageable): Page<Project>
}



interface BoardRepository : BaseRepository<Board> {

    fun findAllByProjectId(projectId: Long, pageable: Pageable): Page<Board>

}

interface WorkflowRepository : BaseRepository<Workflow> {
    @Query("""
        select w from Workflow w
        where w.deleted = false and (w.organizationId = :orgId or w.organizationId is null)
    """)
    fun findAllByOrgId(@Param("orgId") orgId: Long, pageable: Pageable): Page<Workflow>
}

interface StateRepository : BaseRepository<State> {
    fun findAllByWorkflowIdAndDeletedFalse(workflowId: Long, pageable: Pageable): Page<State>
    fun findFirstByWorkflowIdAndDeletedFalseOrderByOrderNumberAsc(workflowId: Long): State?
}

interface TaskRepository : BaseRepository<Task> {
    fun findAllByBoardIdAndDeletedFalse(boardId: Long, pageable: Pageable): Page<Task>

    @Query("""
        select distinct t from Task t
        join t.board b
        join b.project p
        left join TaskAssignee a on a.task = t and a.deleted = false
        where t.deleted = false
          and p.organizationId = :orgId
          and (t.ownerId = :employeeId or a.employeeId = :employeeId)
    """)
    fun findMyTasks(
        @Param("orgId") orgId: Long,
        @Param("employeeId") employeeId: Long,
        pageable: Pageable
    ): Page<Task>
}

interface TaskAssigneeRepository : BaseRepository<TaskAssignee> {
    fun existsByTaskIdAndEmployeeIdAndDeletedFalse(taskId: Long, employeeId: Long): Boolean
}