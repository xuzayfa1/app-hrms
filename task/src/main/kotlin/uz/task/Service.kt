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
    private val projectRepository: ProjectRepository,
    private val boardRepository: BoardRepository,
    private val taskRepository: TaskRepository
) : ProjectService {



    @Transactional
    override fun create(req: CreateProjectRequest): ProjectResponse {

        Utils.checkPosition()

        val orgId = Context.orgId() // id contexdan

        val project = Project(
            name = req.name.trim(),
            organizationId = orgId,
            status = Status.ACTIVE
        )

        return ProjectResponse.toResponse(projectRepository.save(project))
    }

    @Transactional
    override fun update(req: UpdateProjectRequest): ProjectResponse {
        Utils.checkPosition()

        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(req.id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        req.name?.let { project.name = it.trim() }
        //TODO shu yerda agar tugamagan task yoki boshmas board bor bolsa tekshirishni yozish kerak-FIXED!
        req.status?.let {
            val hasBoard = boardRepository.existsByProjectIdAndDeletedFalse(req.id)
            val hasTasks = taskRepository.existsOpenTasksByProjectId(req.id)
            if (hasTasks || hasBoard) throw ProjectNotEmptyException()
            project.status = it
        }

        return ProjectResponse.toResponse(projectRepository.save(project))
    }

    @Transactional
    override fun delete(id: Long) {
        Utils.checkPosition()

        val orgId = Context.orgId()

        val project = projectRepository.findByIdAndDeletedFalse(id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId) throw AccessDeniedException()

        val hasBoard = boardRepository.existsByProjectIdAndDeletedFalse(id)
        val hasTasks = taskRepository.existsOpenTasksByProjectId(id)
        if (hasTasks || hasBoard) throw ProjectNotEmptyException()

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
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : BoardService {

    @Transactional
    override fun create(req: CreateBoardRequest): BoardResponse {

        Utils.checkPosition()

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
        Utils.checkPosition()

        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(req.id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId) throw AccessDeniedException()

        req.name?.let { board.name = it.trim() }
        req.status?.let {
            val hasTasks = taskRepository.existsOpenTasksByProjectId(req.id)
            if (hasTasks) throw BoardNotEmptyException()
            board.status = it
        }

        return BoardResponse.toResponse(boardRepository.save(board))
    }

    @Transactional
    override fun delete(id: Long) {
        Utils.checkPosition()

        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId) throw AccessDeniedException()

        val hasTasks = taskRepository.existsOpenTasksByProjectId(id)
        if (hasTasks) throw BoardNotEmptyException()

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

        if(workflowRepository.existsByWorkflowId(s.workflow.id!!)) throw StateInUseException()

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
        if(workflow.organizationId != null) {
            if (workflow.organizationId != orgId) throw AccessDeniedException()
        }

        return stateRepository.findAllByWorkflowIdAndDeletedFalse(workflowId, pageable)
            .map { StateResponse.toResponse(it) }
    }
}

interface TaskService {
    fun create(req: CreateTaskRequest): TaskResponseMedia
    fun update(req: UpdateTaskRequest): TaskResponse
    fun delete(id: Long)
    fun getOne(id: Long): TaskResponseMedia
    fun getAllByBoardId(boardId: Long, pageable: Pageable): Page<TaskResponseMedia>
    fun getMyTasks(pageable: Pageable): Page<TaskResponseMedia>
    fun changeState(req: ChangeTaskStateRequest): TaskResponse
}

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
    private val stateRepository: StateRepository,
    private val taskAssigneeRepository: TaskAssigneeRepository,
    private val taskMediaRepository: TaskMediaRepository,
) : TaskService {

    @Transactional
    override fun create(req: CreateTaskRequest): TaskResponseMedia {
        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        val board = boardRepository.findByIdAndDeletedFalse(req.boardId) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId) throw AccessDeniedException()

        val state = stateRepository.findByIdAndDeletedFalse(req.stateId) ?: throw StateNotFoundException()

        val workflowOrgId = state.workflow.organizationId ?: throw AccessDeniedException()
        if (workflowOrgId != orgId) throw AccessDeniedException()

        val now = Date()
        if (req.deadline != null && req.deadline.before(now)) throw DeadlineInPastException()



        val task = Task(
            title = req.title.trim(),
            description = req.description.trim(),
            board = board,
            state = state,
            ownerId = employeeId,
            deadline = req.deadline
        )
       val saved = taskRepository.save(task)

        req.medias?.let {
            req.medias.forEach {
                taskMediaRepository.save(
                    TaskMedia(
                        task = task,
                        hashId = it
                    )
                )
            }
        }



        return TaskResponseMedia.toResponse(saved,req.medias?:emptyList())
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
        req.deadline?.let {
            val now = Date()
            if (req.deadline.before(now)) throw DeadlineInPastException()
            t.deadline = it
        }

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
    override fun getOne(id: Long): TaskResponseMedia {
        val orgId = Context.orgId()

        val t = taskRepository.findByIdAndDeletedFalse(id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId) throw AccessDeniedException()

        val media = taskMediaRepository.findAllByTask(t).map {
            it.hashId
        }

        return TaskResponseMedia.toResponse(t,media)
    }

    @Transactional(readOnly = true)
    override fun getMyTasks(pageable: Pageable): Page<TaskResponseMedia> {
        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        return taskRepository.findMyTasks(orgId, employeeId, pageable)
            .map {
                val media = taskMediaRepository.findAllByTask(it).map {media->
                    media.hashId
                }
                TaskResponseMedia.toResponse(it,media)
            }
    }


    @Transactional
    override fun changeState(req: ChangeTaskStateRequest): TaskResponse {

        val orgId = Context.orgId()
        val employeeId = Context.employeeId()

        val task = taskRepository.findByIdAndDeletedFalse(req.taskId) ?: throw TaskNotFoundException()
        if (task.board.project.organizationId != orgId) throw AccessDeniedException()

        // deadline check
        val now = Date()
        if (task.deadline != null && now.after(task.deadline)) throw DeadlineExpiredException()

        val newState = stateRepository.findByIdAndDeletedFalse(req.stateId) ?: throw StateNotFoundException()

        val newStateOrgId = newState.workflow.organizationId
        if (newStateOrgId != orgId) throw AccessDeniedException()

        val currentWorkflowId = task.state.workflow.id
        val newWorkflowId = newState.workflow.id
        if (currentWorkflowId != null && newWorkflowId != null && currentWorkflowId != newWorkflowId) {
            throw InvalidStateWorkflowException()
        }

        val currentState = task.state

        val isOwner = task.ownerId == employeeId
        val isAssignee = taskAssigneeRepository.existsByTaskIdAndEmployeeIdAndDeletedFalse(task.id!!, employeeId)

        //owner hohlagan statega otkazadi agar ozini ozi biriktirgan bolsa
        if (isOwner && isAssignee) {
            task.state = newState
            return TaskResponse.toResponse(taskRepository.save(task))
        }


        val currentPer = currentState.permission
        val targetPer = newState.permission

        // assignee lar faqat step-by-step otadi qaytadi
        fun requireStep() {
            val step = kotlin.math.abs(newState.orderNumber - currentState.orderNumber)
            if (step != 1L) throw AccessDeniedException()
        }

        //assignee
        if (targetPer == Permission.ASSIGNEE) {
            if (isAssignee) {
                //check step
                requireStep()
                task.state = newState
                return TaskResponse.toResponse(taskRepository.save(task))
            }

            if (isOwner) {
                //admin faqat owner dan qaytaroladi
                if (currentPer != Permission.OWNER) throw AccessDeniedException()
                // order boyicha qaytarganda kichik bolishi kerak
                if (newState.orderNumber >= currentState.orderNumber) throw AccessDeniedException()
                task.state = newState
                return TaskResponse.toResponse(taskRepository.save(task))
            }

            throw AccessDeniedException()
        }

        //owner
        if (targetPer == Permission.OWNER) {
            if (!isOwner) throw AccessDeniedException()

            if (currentPer != Permission.OWNER) throw AccessDeniedException()


            task.state = newState
            return TaskResponse.toResponse(taskRepository.save(task))
        }

        throw AccessDeniedException()
    }

    @Transactional(readOnly = true)
    override fun getAllByBoardId(boardId: Long, pageable: Pageable): Page<TaskResponseMedia> {
        val orgId = Context.orgId()

        val board = boardRepository.findByIdAndDeletedFalse(boardId) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId) throw AccessDeniedException()

        return taskRepository.findAllByBoardIdAndDeletedFalse(boardId, pageable)
            .map {
                val media = taskMediaRepository.findAllByTask(it).map {media->
                    media.hashId
                }
                TaskResponseMedia.toResponse(it,media)
            }
    }
}

