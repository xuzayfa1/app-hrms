package uz.task

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
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
        hasManager()
        val project = Project(
            name = req.name.trim(),
            organizationId = orgId(),
            status = Status.ACTIVE
        )
        project.createdBy = employeeId()

        return ProjectResponse.toResponse(projectRepository.save(project))
    }

    @Transactional
    override fun update(req: UpdateProjectRequest): ProjectResponse {
        hasManager()

        val project = projectRepository.findByIdAndDeletedFalse(req.id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId()) throw AccessDeniedException()

        req.name?.let { project.name = it.trim() }
        req.status?.let {
            val hasBoard = boardRepository.existsByProjectIdAndDeletedFalse(req.id)
            val hasTasks = taskRepository.existsOpenTasksByProjectId(req.id)
            if (hasTasks || hasBoard) throw ProjectNotEmptyException()
            project.status = it
        }

        project.lastModifiedBy = employeeId()

        return ProjectResponse.toResponse(projectRepository.save(project))
    }

    @Transactional
    override fun delete(id: Long) {
        hasManager()

        val project = projectRepository.findByIdAndDeletedFalse(id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId()) throw AccessDeniedException()


        val hasBoard = boardRepository.existsByProjectIdAndDeletedFalse(id)
        val hasTasks = taskRepository.existsOpenTasksByProjectId(id)
        if (hasTasks || hasBoard) throw ProjectNotEmptyException()

        projectRepository.trash(project.id!!)

        project.lastModifiedBy = employeeId()
        projectRepository.save(project)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): ProjectResponse {

        val project = projectRepository.findByIdAndDeletedFalse(id) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId()) throw AccessDeniedException()

        return ProjectResponse.toResponse(project)
    }

    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<ProjectResponse> {

        return projectRepository.findAllByOrganizationIdAndDeletedFalse(orgId(), pageable)
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

        hasManager()

        val project = projectRepository.findByIdAndDeletedFalse(req.projectId) ?: throw ProjectNotFoundException()

        if (project.status == Status.INACTIVE) throw ProjectArchivedException()

        if (project.organizationId != orgId()) throw AccessDeniedException()

        val board = Board(
            name = req.name.trim(),
            project = project,
            status = Status.ACTIVE
        )
        board.createdBy = employeeId()

        return BoardResponse.toResponse(boardRepository.save(board))
    }

    @Transactional
    override fun update(req: UpdateBoardRequest): BoardResponse {
        hasManager()

        val board = boardRepository.findByIdAndDeletedFalse(req.id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId()) throw AccessDeniedException()

        req.name?.let { board.name = it.trim() }
        req.status?.let {
            val hasTasks = taskRepository.existsOpenTasksByProjectId(req.id)
            if (hasTasks) throw BoardNotEmptyException()
            board.status = it
        }

        board.lastModifiedBy = employeeId()

        return BoardResponse.toResponse(boardRepository.save(board))
    }

    @Transactional
    override fun delete(id: Long) {
        hasManager()

        val board = boardRepository.findByIdAndDeletedFalse(id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId()) throw AccessDeniedException()

        val hasTasks = taskRepository.existsOpenTasksByProjectId(id)
        if (hasTasks) throw BoardNotEmptyException()

        boardRepository.trash(board.id!!)

        board.lastModifiedBy = employeeId()
        boardRepository.save(board)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): BoardResponse {

        val board = boardRepository.findByIdAndDeletedFalse(id) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId()) throw AccessDeniedException()

        return BoardResponse.toResponse(board)
    }

    @Transactional(readOnly = true)
    override fun getAllByProjectId(projectId: Long, pageable: Pageable): Page<BoardResponse> {

        val project = projectRepository.findByIdAndDeletedFalse(projectId) ?: throw ProjectNotFoundException()

        if (project.organizationId != orgId()) throw AccessDeniedException()

        return boardRepository.findAllByProjectIdAndDeletedFalse(projectId, pageable)
            .map { BoardResponse.toResponse(it) }
    }
}

interface WorkflowService {
    fun create(req: CreateWorkflowRequest): WorkflowResponse
    fun update(req: UpdateWorkflowRequest): WorkflowResponse
    fun delete(id: Long)
    fun getOne(id: Long): WorkflowResponse
    fun getAll(pageable: Pageable): Page<WorkflowResponse>
    fun getAllByBoardId(id: Long, pageable: Pageable): Page<WorkflowResponse>
}

@Service
class WorkflowServiceImpl(
    private val workflowRepository: WorkflowRepository,
    private val boardRepository: BoardRepository,
    private val stateRepository: StateRepository
) : WorkflowService {

    @Transactional
    override fun create(req: CreateWorkflowRequest): WorkflowResponse {

        val board = boardRepository.findByIdAndDeletedFalse(req.boardId) ?: throw BoardNotFoundException()

        if (board.project.organizationId != orgId()) throw AccessDeniedException()

        val w = Workflow(
            name = req.name.trim(),
            organizationId = orgId(),
            board = board
        )
        w.createdBy = employeeId()
        return WorkflowResponse.toResponse(workflowRepository.save(w))
    }

    @Transactional
    override fun update(req: UpdateWorkflowRequest): WorkflowResponse {
        val w = workflowRepository.findByIdAndDeletedFalse(req.id) ?: throw WorkflowNotFoundException()

        // system default readonly
        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId()) throw AccessDeniedException()

        req.name.let { w.name = it.trim() }

        w.lastModifiedBy = employeeId()
        return WorkflowResponse.toResponse(workflowRepository.save(w))
    }

    @Transactional
    override fun delete(id: Long) {
        val w = workflowRepository.findByIdAndDeletedFalse(id) ?: throw WorkflowNotFoundException()

        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId()) throw AccessDeniedException()

        if (stateRepository.existsByWorkflowIdAndDeletedFalse(id)) throw WorkflowNotEmptyException()

        workflowRepository.trash(w.id!!)

        w.lastModifiedBy = employeeId()
        workflowRepository.save(w)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): WorkflowResponse {
        val w = workflowRepository.findByIdAndDeletedFalse(id) ?: throw WorkflowNotFoundException()

        if (w.organizationId == null) return WorkflowResponse.toResponse(w)
        if (w.organizationId != null && w.organizationId != orgId()) throw AccessDeniedException()


        return WorkflowResponse.toResponse(w)
    }


    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<WorkflowResponse> {
        val orgId = orgId()
        return workflowRepository.findAllByOrgId(orgId, pageable)
            .map { WorkflowResponse.toResponse(it) }
    }

    override fun getAllByBoardId(id: Long, pageable: Pageable): Page<WorkflowResponse> {
        val board = boardRepository.findByIdAndDeletedFalse(id) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId()) throw AccessDeniedException()

        return workflowRepository.findAllByBoardId(id, pageable)
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

    private fun orderValidation(workflowId: Long, orderNumber: Long) {
        if (orderNumber < 1) throw InvalidStateOrderNumberException()

        val existingStates = stateRepository.findAllByWorkflowId(workflowId)

        if (existingStates.any { it.orderNumber == orderNumber }) throw InvalidStateOrderException()


        if (existingStates.isNotEmpty()) {
            val orderNumbers = existingStates.map { it.orderNumber }.toMutableList()
            orderNumbers.add(orderNumber)
            orderNumbers.sort()

            // orderlar farqini tek qilish
            for (i in 1 until orderNumbers.size - 1) {
                val n = orderNumbers[i + 1] - orderNumbers[i]
                if (n != 1L) throw InvalidStateOrderException()
            }
        } else {
            //agar orderlar bolmasa 1dan boshlanishi kerak
            if (orderNumber != 1L) throw InvalidStateOrderNumberException()
        }
    }

    @Transactional
    override fun create(req: CreateStateRequest): StateResponse {

        val workflow = workflowRepository.findByIdAndDeletedFalse(req.workflowId) ?: throw WorkflowNotFoundException()

        if (workflow.organizationId != orgId()) throw AccessDeniedException()


        // Validation order number
        orderValidation(req.workflowId, req.orderNumber)


        val state = State(
            name = req.name.trim(),
            orderNumber = req.orderNumber,
            workflow = workflow,
            permission = req.permission
        )

        state.createdBy = employeeId()

        return StateResponse.toResponse(stateRepository.save(state))
    }

    @Transactional
    override fun update(req: UpdateStateRequest): StateResponse {
        val s = stateRepository.findByIdAndDeletedFalse(req.id) ?: throw StateNotFoundException()

        val w = s.workflow
        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId()) throw AccessDeniedException()

        req.name?.let { s.name = it.trim() }
        req.orderNumber?.let {
            orderValidation(s.workflow.id!!, req.orderNumber)
            s.orderNumber = it
        }
        req.permission?.let { s.permission = it }

        s.lastModifiedBy = employeeId()

        return StateResponse.toResponse(stateRepository.save(s))
    }

    @Transactional
    override fun delete(id: Long) {
        val s = stateRepository.findByIdAndDeletedFalse(id) ?: throw StateNotFoundException()

        val w = s.workflow
        if (w.organizationId == null) throw SystemWorkflowReadonlyException()
        if (w.organizationId != orgId() && employeeId() != s.createdBy) throw AccessDeniedException()

        if (workflowRepository.existsByWorkflowId(s.workflow.id!!)) throw StateInUseException()

        stateRepository.trash(s.id!!)

        s.lastModifiedBy = employeeId()
        stateRepository.save(s)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): StateResponse {
        val s = stateRepository.findByIdAndDeletedFalse(id) ?: throw StateNotFoundException()

        // system state ham korinadi
        val wOrg = s.workflow.organizationId
        if (wOrg != null && wOrg != orgId()) throw AccessDeniedException()

        return StateResponse.toResponse(s)
    }


    @Transactional(readOnly = true)
    override fun getAllByWorkflowId(workflowId: Long, pageable: Pageable): Page<StateResponse> {

        val workflow = workflowRepository.findByIdAndDeletedFalse(workflowId) ?: throw WorkflowNotFoundException()
        if (workflow.organizationId != null) {
            if (workflow.organizationId != orgId()) throw AccessDeniedException()
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
    fun assignTask(req: AssignTaskRequest)
    fun removeAssignee(req: RemoveAssigneeRequest)
}

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
    private val stateRepository: StateRepository,
    private val taskAssigneeRepository: TaskAssigneeRepository,
    private val taskMediaRepository: TaskMediaRepository,
    private val employeeFeignClient: EmployeeFeignClient,
    private val taskActionRepository: TaskActionRepository,
    private val objectMapper: ObjectMapper,
//    private val taskKafkaProducer: TaskKafkaProducer,
    private val notifFeignClient: NotifFeignClient
) : TaskService {

    private fun saveAction(
        task: Task,
        type: TaskActionType,
        from: String? = null,
        to: String? = null,
        title: String? = null,
        assignee: TaskAssignee? = null,
        fileAttach: Map<String, List<String>?>? = null,
        deadline: Date? = null,
        onlySave: Boolean
    ) {

        val employee = employeeFeignClient.getEmployee(employeeId())

        val action = TaskAction(
            task = task,
            employeeId = employeeId(),
            employeeName = employee.user.firstName+" "+employee.user.lastName,
            type = type,
            fromState = from,
            toState = to,
            title = title,
            assignee = assignee,
            fileAttach = fileAttach?.let { objectMapper.writeValueAsString(it) },
            deadline = deadline
        )
        action.createdBy = employeeId()
        taskActionRepository.save(action)

        if (type == TaskActionType.CREATED || onlySave) return

        val assignees = taskAssigneeRepository.findAssigneeIds(task.id!!)

        val files: List<String>? = fileAttach?.get("fileHashId")

        val event = TaskEvent(
            orgName = employee.organization.name,
            taskId = task.id!!,

            ownerEmployeeId = task.ownerId,
            ownerName = employee.user.firstName+" "+employee.user.lastName,

            assignees = assignees,

            projectName = task.board.project.name,
            fromState = from,
            toState = to,

            newTitle = task.title,

            assigneeEmployeeId = assignee?.employeeId,
            newFileAttach = files,
            newDeadline = deadline,

            createdDate = action.createdDate,
        )

        // Transaction commit bolgandan keyin notifga yuborish
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    notifFeignClient.setNotif(event)
                }
            })
        } else {
            notifFeignClient.setNotif(event)
        }



    }


    @Transactional
    override fun create(req: CreateTaskRequest): TaskResponseMedia {

        val board = boardRepository.findByIdAndDeletedFalse(req.boardId) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId()) throw AccessDeniedException()
        if (board.status == Status.INACTIVE) throw BoardArchivedException()

        val state = stateRepository.findByIdAndDeletedFalse(req.stateId) ?: throw StateNotFoundException()

        val workflowOrgId = state.workflow.organizationId
        if (workflowOrgId != null) {
            if (workflowOrgId != orgId()) throw AccessDeniedException()
        }

        val now = Date()
        if (req.deadline != null && req.deadline.before(now)) throw DeadlineInPastException()


        val task = Task(
            title = req.title.trim(),
            description = req.description.trim(),
            board = board,
            state = state,
            ownerId = employeeId(),
            deadline = req.deadline
        )

        task.createdBy = employeeId()
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

        saveAction(
            task = task,
            type = TaskActionType.CREATED,
            title = task.title,
            from = state.name,
            deadline = task.deadline,
            fileAttach = mapOf(
                "fileHashId" to req.medias
            ),
            onlySave = true
        )



        return TaskResponseMedia.toResponse(saved, req.medias ?: emptyList())
    }

    @Transactional
    override fun update(req: UpdateTaskRequest): TaskResponse {

        val t = taskRepository.findByIdAndDeletedFalse(req.id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId()) throw AccessDeniedException()

        if (t.ownerId != employeeId()) throw AccessDeniedException()

        val oldTitle = t.title
        val oldDeadline = t.deadline

        req.title?.let { t.title = it.trim() }
        req.description?.let { t.description = it.trim() }
        req.deadline?.let {
            val now = Date()
            if (req.deadline.before(now)) throw DeadlineInPastException()
            t.deadline = it
        }
        req.medias?.let {
            req.medias.forEach {
                taskMediaRepository.save(
                    TaskMedia(
                        task = t,
                        hashId = it
                    )
                )
            }
        }
        t.lastModifiedBy = employeeId()
        val saved = taskRepository.save(t)

        var changedDeadline: Date? = null
        var changedFiles: List<String>? = null

        if (req.title != null && saved.title != oldTitle) {
            saveAction(
                task = saved,
                type = TaskActionType.TITLE_CHANGED,
                title = saved.title,
                from = saved.state.name,
                onlySave = true
            )
        }

        if (req.deadline != null && saved.deadline != oldDeadline) {
            changedDeadline = saved.deadline
            saveAction(
                task = saved,
                type = TaskActionType.DEADLINE_CHANGED,
                deadline = req.deadline,
                from = saved.state.name,
                onlySave = true

            )
        }

        if (req.medias != null) {
            changedFiles = req.medias
            saveAction(
                task = saved,
                type = TaskActionType.FILE_ATTACHED,
                fileAttach = mapOf(
                    "fileHashId" to req.medias
                ),
                from = saved.state.name,
                onlySave = true
            )
        }


        val employee = employeeFeignClient.getEmployee(employeeId())


        val assignees = taskAssigneeRepository.findAssigneeIds(saved.id!!)

        val event = TaskEvent(
            orgName = employee.organization.name,
            taskId = saved.id!!,
            ownerEmployeeId = saved.ownerId,
            ownerName = employee.user.firstName+" "+employee.user.lastName,
            assignees = assignees,
            fromState = saved.state.name,
            toState = null,
            projectName = saved.board.project.name,
            newTitle = saved.title,
            assigneeEmployeeId = null,
            newFileAttach = changedFiles,
            newDeadline = changedDeadline,
            createdDate = Date()
        )


        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    notifFeignClient.setNotif(event)
                }
            })
        } else {
            notifFeignClient.setNotif(event)
        }


        return TaskResponse.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long) {

        val t = taskRepository.findByIdAndDeletedFalse(id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId()) throw AccessDeniedException()

        if (t.ownerId != employeeId()) throw AccessDeniedException()

        taskRepository.trash(t.id!!)

        t.lastModifiedBy = employeeId()
        taskRepository.save(t)
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): TaskResponseMedia {

        val t = taskRepository.findByIdAndDeletedFalse(id) ?: throw TaskNotFoundException()
        if (t.board.project.organizationId != orgId()) throw AccessDeniedException()

        val media = taskMediaRepository.findAllByTask(t).map {
            it.hashId
        }

        return TaskResponseMedia.toResponse(t, media)
    }

    @Transactional(readOnly = true)
    override fun getMyTasks(pageable: Pageable): Page<TaskResponseMedia> {

        return taskRepository.findMyTasks(orgId(), employeeId(), pageable)
            .map {
                val media = taskMediaRepository.findAllByTask(it).map { media ->
                    media.hashId
                }
                TaskResponseMedia.toResponse(it, media)
            }
    }

    @Transactional
    override fun changeState(req: ChangeTaskStateRequest): TaskResponse {


        val task = taskRepository.findByIdAndDeletedFalse(req.taskId) ?: throw TaskNotFoundException()
        if (task.board.project.organizationId != orgId()) throw AccessDeniedException()

        // deadline check
        val now = Date()
        if (task.deadline != null && now.after(task.deadline)) throw DeadlineExpiredException()

        val newState = stateRepository.findByIdAndDeletedFalse(req.stateId) ?: throw StateNotFoundException()

        val newStateOrgId = newState.workflow.organizationId
        if (newStateOrgId != null) {
            if (newStateOrgId != orgId()) throw AccessDeniedException()
        }

        val currentWorkflowId = task.state.workflow.id
        val newWorkflowId = newState.workflow.id
        if (currentWorkflowId != null && newWorkflowId != null && currentWorkflowId != newWorkflowId) {
            throw InvalidStateWorkflowException()
        }

        val currentState = task.state
        val isOwner = task.ownerId == employeeId()
        val isAssignee = taskAssigneeRepository.existsByTaskIdAndEmployeeIdAndDeletedFalse(task.id!!, employeeId())

        // Owner bolsa hohlaganiga otkazadi
        if (isOwner) {
            task.state = newState
            task.lastModifiedBy = employeeId()
            val saved = taskRepository.save(task)
            saveAction(
                task = saved,
                type = TaskActionType.STATE_CHANGED,
                from = currentState.name,
                to = newState.name,
                onlySave = false
            )

            return TaskResponse.toResponse(saved)
        }

        // Assignee
        if (isAssignee) {
            val currentPer = currentState.permission
            val targetPer = newState.permission

            if (currentPer == Permission.ASSIGNEE && targetPer == Permission.ASSIGNEE) {
                val step = kotlin.math.abs(newState.orderNumber - currentState.orderNumber)
                if (step == 1L) {
                    task.state = newState
                    task.lastModifiedBy = employeeId()
                    val saved = taskRepository.save(task)
                    saveAction(
                        task = saved,
                        type = TaskActionType.STATE_CHANGED,
                        from = currentState.name,
                        to = newState.name,
                        onlySave = false
                    )

                    return TaskResponse.toResponse(saved)

                }
            }
        }

        throw AccessDeniedException()
    }

    @Transactional(readOnly = true)
    override fun getAllByBoardId(boardId: Long, pageable: Pageable): Page<TaskResponseMedia> {

        val board = boardRepository.findByIdAndDeletedFalse(boardId) ?: throw BoardNotFoundException()
        if (board.project.organizationId != orgId()) throw AccessDeniedException()

        return taskRepository.findAllByBoardIdAndDeletedFalse(boardId, pageable)
            .map {
                val media = taskMediaRepository.findAllByTask(it).map { media ->
                    media.hashId
                }
                TaskResponseMedia.toResponse(it, media)
            }
    }

    @Transactional
    override fun assignTask(req: AssignTaskRequest) {

        val task = taskRepository.findByIdAndDeletedFalse(req.taskId) ?: throw TaskNotFoundException()
        if (task.board.project.organizationId != orgId()) throw AccessDeniedException()

        if (task.ownerId != employeeId()) throw AccessDeniedException()

        val alreadyAssigned =
            taskAssigneeRepository.existsByTaskIdAndEmployeeIdAndDeletedFalse(req.taskId, req.employeeId)
        if (alreadyAssigned) throw EmployeeAlreadyAssignedException()

        val emp = employeeFeignClient.getEmployee(req.employeeId)

        if (emp.organization.id != orgId()) throw EmployeeNotInOrganizationException()
        if (!emp.isActive) throw AccessDeniedException()

        val taskAssignee = TaskAssignee(
            task = task,
            employeeId = req.employeeId
        )
        taskAssignee.createdBy = employeeId()
        taskAssigneeRepository.save(taskAssignee)

        saveAction(
            task = task,
            type = TaskActionType.ASSIGNEE_ADDED,
            assignee = taskAssignee,
            from = task.state.name,
            onlySave = false
        )

    }

    @Transactional
    override fun removeAssignee(req: RemoveAssigneeRequest) {

        val task = taskRepository.findByIdAndDeletedFalse(req.taskId) ?: throw TaskNotFoundException()
        if (task.board.project.organizationId != orgId()) throw AccessDeniedException()

        if (task.ownerId != employeeId()) throw AccessDeniedException()

        val assignee = taskAssigneeRepository.findByTaskIdAndEmployeeIdAndDeletedFalse(req.taskId, req.employeeId)
            ?: throw AssigneeNotFoundException()

        taskAssigneeRepository.trash(assignee.id!!)
    }


}


interface ActionService{
    fun getAllByTaskId(id:Long,pageable: Pageable): Page<ActionResponse>
}

@Service
class ActionServiceImpl(
    private val actionRepository: TaskActionRepository
): ActionService{
    override fun getAllByTaskId(id: Long, pageable: Pageable): Page<ActionResponse> {
        return actionRepository.findAllByTaskId(id,pageable).map {
            ActionResponse.toResponse(it)
        }
    }

}



