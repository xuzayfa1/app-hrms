package uz.task

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Date


interface ProjectService {
    fun create(req: CreateProjectRequest): ProjectResponse
    fun update(req: UpdateProjectRequest): ProjectResponse
    fun delete(id: Long)
    fun getOne(id: Long): ProjectResponse
    fun getAll(pageable: Pageable): Page<ProjectResponse>
}

@Service
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository
) : ProjectService {

    @Transactional
    override fun create(req: CreateProjectRequest): ProjectResponse {
        //todo bu yerda rolega ham tekshirish kerak

        val orgId = Context.orgId()// id contexdan

        val project = Project(
            name = req.name.trim(),
            organizationId = orgId,
            status = Status.ACTIVE
        )

        return ProjectResponse.toResponse(projectRepository.save(project))
    }

    @Transactional
    override fun update(req: UpdateProjectRequest): ProjectResponse {
        //todo bu yerda rolega ham tekshirish kerak

        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(req.id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        req.name?.let { project.name = it.trim() }
        //TODO shu yerda agar tugamagan task bor bolsa tekshirishni yozish kerak
        req.status?.let { project.status = it }

        return ProjectResponse.toResponse(projectRepository.save(project))
    }

    @Transactional
    override fun delete(id: Long) {
        //todo bu yerda rolega ham tekshirish kerak

        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        projectRepository.trash(project.id!!)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): ProjectResponse {
        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        return ProjectResponse.toResponse(project)
    }

    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<ProjectResponse> {
        val orgId = Context.orgId()

        return projectRepository.findAllByOrganizationIdAndDeletedFalse(orgId,pageable)
            .map { ProjectResponse.toResponse(it) }
    }
}



interface BoardService {
    fun create(req: CreateBoardRequest): BoardResponse
    fun update(req: UpdateBoardRequest): BoardResponse
    fun delete(id: Long)
    fun getOne(id: Long): BoardResponse
    fun getAllByProjectId(projectId: Long, pageable: Pageable): Page<BoardResponse>
}

@Service
class BoardServiceImpl(
    private val boardRepository: BoardRepository,
    private val projectRepository: ProjectRepository
) : BoardService {

    @Transactional
    override fun create(req: CreateBoardRequest): BoardResponse {

        //todo bu yerda rolega ham tekshirish kerak
        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(req.projectId) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        val board = Board(
            name = req.name.trim(),
            project = project,
            status = Status.ACTIVE
        )

        return BoardResponse.toResponse(boardRepository.save(board))
    }

    @Transactional
    override fun update(req: UpdateBoardRequest): BoardResponse {
        //todo bu yerda rolega ham tekshirish kerak

        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(req.id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId) throw AccessDeniedException()

        req.name?.let { board.name = it.trim() }
        req.status?.let { board.status = it }

        return BoardResponse.toResponse(boardRepository.save(board))
    }

    @Transactional
    override fun delete(id: Long) {
        //todo bu yerda rolega ham tekshirish kerak

        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId) throw AccessDeniedException()

        boardRepository.trash(board.id!!)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): BoardResponse {
        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId) throw AccessDeniedException()

        return BoardResponse.toResponse(board)
    }

    @Transactional(readOnly = true)
    override fun getAllByProjectId(projectId: Long, pageable: Pageable): Page<BoardResponse> {
        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(projectId) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        return boardRepository.findAllByProjectId(projectId, pageable)
            .map { BoardResponse.toResponse(it) }
    }
}

interface WorkflowService {
    fun create(req: CreateWorkflowRequest): WorkflowResponse
    fun update(req: UpdateWorkflowRequest): WorkflowResponse
    fun delete(id: Long)
    fun getOne(id: Long): WorkflowResponse
    fun getAll(pageable: Pageable): Page<WorkflowResponse>
}

@Service
class WorkflowServiceImpl(
    private val workflowRepository: WorkflowRepository
) : WorkflowService {

    @Transactional
    override fun create(req: CreateWorkflowRequest): WorkflowResponse {
        val orgId = Context.orgId()

        val w = Workflow(
            name = req.name.trim(),
            organizationId = orgId
        )
        return WorkflowResponse.toResponse(workflowRepository.save(w))
    }
    @Transactional
    override fun update(req: UpdateWorkflowRequest): WorkflowResponse {
        val orgId = Context.orgId()
        val w = workflowRepository.findByIdAndDeletedFalse(req.id) ?: throw WorkflowNotFoundException()

        // system default readonly
        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId) throw AccessDeniedException()

        req.name.let { w.name = it.trim() }
        return WorkflowResponse.toResponse(workflowRepository.save(w))
    }

    @Transactional
    override fun delete(id: Long) {
        val orgId = Context.orgId()
        val w = workflowRepository.findByIdAndDeletedFalse(id) ?: throw WorkflowNotFoundException()

        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId) throw AccessDeniedException()

        workflowRepository.trash(w.id!!)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): WorkflowResponse {
        val orgId = Context.orgId()
        val w = workflowRepository.findByIdAndDeletedFalse(id) ?: throw WorkflowNotFoundException()

        if (w.organizationId == null) return WorkflowResponse.toResponse(w)
        if (w.organizationId != null && w.organizationId != orgId) throw AccessDeniedException()


        return WorkflowResponse.toResponse(w)
    }


    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<WorkflowResponse> {
        val orgId = Context.orgId()
        return workflowRepository.findAllByOrgId(orgId, pageable)
            .map { WorkflowResponse.toResponse(it) }
    }
}

interface StateService {
    fun create(req: CreateStateRequest): StateResponse
    fun update(req: UpdateStateRequest): StateResponse
    fun delete(id: Long)
    fun getOne(id: Long): StateResponse
    fun getAllByWorkflowId(workflowId: Long, pageable: Pageable): Page<StateResponse>
}

@Service
class StateServiceImpl(
    private val workflowRepository: WorkflowRepository,
    private val stateRepository: StateRepository
) : StateService {

    @Transactional
    override fun create(req: CreateStateRequest): StateResponse {
        val orgId = Context.orgId()

        val workflow = workflowRepository.findByIdAndDeletedFalse(req.workflowId) ?: throw WorkflowNotFoundException()

        if (workflow.organizationId != orgId) throw AccessDeniedException()

        val state = State(
            name = req.name.trim(),
            orderNumber = req.orderNumber,
            workflow = workflow,
            permission = req.permission
        )

        return StateResponse.toResponse(stateRepository.save(state))
    }

    @Transactional
    override fun update(req: UpdateStateRequest): StateResponse {
        val orgId = Context.orgId()
        val s = stateRepository.findByIdAndDeletedFalse(req.id) ?: throw StateNotFoundException()

        val w = s.workflow
        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId) throw AccessDeniedException()

        req.name?.let { s.name = it.trim() }
        req.orderNumber?.let { s.orderNumber = it }
        req.permission?.let { s.permission = it }

        return StateResponse.toResponse(stateRepository.save(s))
    }

    @Transactional
    override fun delete(id: Long) {
        val orgId = Context.orgId()
        val s = stateRepository.findByIdAndDeletedFalse(id) ?: throw StateNotFoundException()

        val w = s.workflow
        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId) throw AccessDeniedException()

        stateRepository.trash(s.id!!)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): StateResponse {
        val orgId = Context.orgId()
        val s = stateRepository.findByIdAndDeletedFalse(id) ?: throw StateNotFoundException()

        // system state ham ko‘rinadi
        val wOrg = s.workflow.organizationId
        if (wOrg != null && wOrg != orgId) throw AccessDeniedException()

        return StateResponse.toResponse(s)
    }


    @Transactional(readOnly = true)
    override fun getAllByWorkflowId(workflowId: Long, pageable: Pageable): Page<StateResponse> {
        val orgId = Context.orgId()

        val workflow = workflowRepository.findByIdAndDeletedFalse(workflowId) ?: throw WorkflowNotFoundException()
        if (workflow.organizationId != orgId) throw AccessDeniedException()

        return stateRepository.findAllByWorkflowIdAndDeletedFalse(workflowId, pageable)
            .map { StateResponse.toResponse(it) }
    }
}

interface TaskService {
    fun create(req: CreateTaskRequest): TaskResponse
    fun update(req: UpdateTaskRequest): TaskResponse
    fun delete(id: Long)
    fun getOne(id: Long): TaskResponse
    fun getAllByBoardId(boardId: Long, pageable: Pageable): Page<TaskResponse>
    fun getMyTasks(pageable: Pageable): Page<TaskResponse>
    fun changeState(req: ChangeTaskStateRequest): TaskResponse
}

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
    private val stateRepository: StateRepository,
    private val taskAssigneeRepository: TaskAssigneeRepository,
) : TaskService {

    @Transactional
    override fun create(req: CreateTaskRequest): TaskResponse {
        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        val board = boardRepository.findByIdAndDeletedFalse(req.boardId) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId) throw AccessDeniedException()

        val state = stateRepository.findByIdAndDeletedFalse(req.stateId) ?: throw StateNotFoundException()

        val workflowOrgId = state.workflow.organizationId ?: throw AccessDeniedException()
        if (workflowOrgId != orgId) throw AccessDeniedException()

        val task = Task(
            title = req.title.trim(),
            description = req.description.trim(),
            board = board,
            state = state,
            ownerId = employeeId,
            deadline = req.deadline
        )

        return TaskResponse.toResponse(taskRepository.save(task))
    }

    @Transactional
    override fun update(req: UpdateTaskRequest): TaskResponse {
        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        val t = taskRepository.findByIdAndDeletedFalse(req.id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId) throw AccessDeniedException()

        if (t.ownerId != employeeId) throw AccessDeniedException()

        req.title?.let { t.title = it.trim() }
        req.description?.let { t.description = it.trim() }
        req.deadline?.let { t.deadline = it }

        return TaskResponse.toResponse(taskRepository.save(t))
    }

    @Transactional
    override fun delete(id: Long) {
        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        val t = taskRepository.findByIdAndDeletedFalse(id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId) throw AccessDeniedException()

        if (t.ownerId != employeeId) throw AccessDeniedException()

        taskRepository.trash(t.id!!)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): TaskResponse {
        val orgId = Context.orgId()

        val t = taskRepository.findByIdAndDeletedFalse(id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId) throw AccessDeniedException()

        return TaskResponse.toResponse(t)
    }

    @Transactional(readOnly = true)
    override fun getMyTasks(pageable: Pageable): Page<TaskResponse> {
        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        return taskRepository.findMyTasks(orgId, employeeId, pageable)
            .map { TaskResponse.toResponse(it) }
    }

    @Transactional
    override fun changeState(req: ChangeTaskStateRequest): TaskResponse {



        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        val task = taskRepository.findByIdAndDeletedFalse(req.taskId) ?: throw TaskNotFoundException()
        if (task.board.project.organizationId != orgId) throw AccessDeniedException()

        val now = Date()
        if (task.deadline != null && now.after(task.deadline)) throw DeadlineExpiredException()


        val newState = stateRepository.findByIdAndDeletedFalse(req.stateId) ?: throw StateNotFoundException()
        //yangi state workflowini tekshirish
        val newStateOrgId = newState.workflow.organizationId
        if (newStateOrgId != orgId) throw AccessDeniedException()

        // workflow mosligiga tekshirish
        val currentWorkflowId = task.state.workflow.id
        val newWorkflowId = newState.workflow.id
        if (currentWorkflowId != null && newWorkflowId != null && currentWorkflowId != newWorkflowId) {
            throw InvalidStateWorkflowException()
        }
        //kim otkazoladi
        when (newState.permission) {
            Permission.OWNER -> {
                if (task.ownerId != employeeId) throw AccessDeniedException()
            }
            Permission.ASSIGNEE -> {
                val exist = taskAssigneeRepository.existsByTaskIdAndEmployeeIdAndDeletedFalse(task.id!!, employeeId)
                if (!exist && task.ownerId != employeeId()) throw AccessDeniedException()
            }
        }

        task.state = newState
        return TaskResponse.toResponse(taskRepository.save(task))
    }

    @Transactional(readOnly = true)
    override fun getAllByBoardId(boardId: Long, pageable: Pageable): Page<TaskResponse> {
        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(boardId) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId) throw AccessDeniedException()

        return taskRepository.findAllByBoardIdAndDeletedFalse(boardId, pageable)
            .map { TaskResponse.toResponse(it) }
    }
}

