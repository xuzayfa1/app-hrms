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
import java.util.Optional

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

@Repository
interface ProjectRepository : BaseRepository<Project>{
    fun findAllByOrganizationIdAndDeletedFalse(orgId: Long, pageable: Pageable): Page<Project>
}


@Repository
interface BoardRepository : BaseRepository<Board> {

    fun findAllByProjectIdAndDeletedFalse(projectId: Long, pageable: Pageable): Page<Board>
    fun existsByProjectIdAndDeletedFalse(id: Long): Boolean

}
@Repository
interface WorkflowRepository : BaseRepository<Workflow> {
    @Query("""
        select w from Workflow w
        where w.deleted = false and (w.organizationId = :orgId or w.organizationId is null)
    """)
    fun findAllByOrgId(@Param("orgId") orgId: Long, pageable: Pageable): Page<Workflow>

    fun findAllByBoardId(id:Long,pageable: Pageable):Page<Workflow>

    @Query("""
    select count(t) > 0
    from Task t
    where t.deleted = false
      and t.state.workflow.id = :workflowId
""")
    fun existsByWorkflowId(@Param("workflowId") workflowId: Long): Boolean
}
@Repository
interface StateRepository : BaseRepository<State> {
    fun findAllByWorkflowIdAndDeletedFalse(workflowId: Long, pageable: Pageable): Page<State>

    fun existsByWorkflowIdAndDeletedFalse(id:Long): Boolean

    @Query("""
        select s
        from State s
        where s.workflow.id = :workflowId
            and s.deleted = false
        order by s.orderNumber ASC
    """)
    fun findAllByWorkflowId(@Param("workflowId") workflowId: Long): List<State>
}
@Repository
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

    @Query("""
        select count(t) > 0
        from Task t
        join t.board b
        where (b.project.id = :projectId or t.board.id = :projectId)
          and t.deleted = false
    """)
    fun existsOpenTasksByProjectId(@Param("projectId") projectId: Long): Boolean
}
@Repository
interface TaskAssigneeRepository : BaseRepository<TaskAssignee> {
    fun existsByTaskIdAndEmployeeIdAndDeletedFalse(taskId: Long, employeeId: Long): Boolean
    fun findByTaskIdAndEmployeeIdAndDeletedFalse(taskId: Long, employeeId: Long): TaskAssignee?

    @Query("""
        select a.employeeId from TaskAssignee a
        where a.task.id = :taskId and a.deleted = false
    """)
    fun findAssigneeIds(@Param("taskId") taskId: Long): List<Long>
}

@Repository
interface TaskMediaRepository : BaseRepository<TaskMedia> {
    fun findAllByTask(task: Task): List<TaskMedia>
}

@Repository
interface TaskActionRepository : BaseRepository<TaskAction>{
    fun findAllByTaskId(id:Long,pageable: Pageable): Page<TaskAction>
}