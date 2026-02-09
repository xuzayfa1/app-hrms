package uz.task




import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/task/projects")
class ProjectController(
    private val projectService: ProjectService
) {

    @PostMapping
    fun create(@RequestBody req: CreateProjectRequest): ProjectResponse {
        return projectService.create(req)
    }

    @PutMapping
    fun update(@RequestBody req: UpdateProjectRequest): ProjectResponse {
        return projectService.update(req)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): ProjectResponse {
        return projectService.getOne(id)
    }

    @GetMapping
    fun getAll(pageable: Pageable): Page<ProjectResponse> {
        return projectService.getAll(pageable)
    }

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Long) {
        projectService.delete(id)
    }
}


@RestController
@RequestMapping("/api/task/boards")
class BoardController(
    private val boardService: BoardService
) {

    @PostMapping
    fun create(@RequestBody req: CreateBoardRequest): BoardResponse {
        return boardService.create(req)
    }

    @PutMapping
    fun update(@RequestBody req: UpdateBoardRequest): BoardResponse {
        return boardService.update(req)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): BoardResponse {
        return boardService.getOne(id)
    }

    @GetMapping
    fun getAllByProjectId(@RequestParam projectId: Long, pageable: Pageable): Page<BoardResponse> {
        return boardService.getAllByProjectId(projectId, pageable)
    }

    @PostMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        boardService.delete(id)
    }
}

@RestController
@RequestMapping("/api/task/workflows")
class WorkflowController(
    private val workflowService: WorkflowService
) {

    @PostMapping
    fun create(@RequestBody req: CreateWorkflowRequest): WorkflowResponse {
        return workflowService.create(req)
    }
    @PutMapping
    fun update(@RequestBody req: UpdateWorkflowRequest) = workflowService.update(req)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = workflowService.getOne(id)

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Long) = workflowService.delete(id)

    @GetMapping
    fun getAll(pageable: Pageable): Page<WorkflowResponse> {
        return workflowService.getAll(pageable)
    }
}

@RestController
@RequestMapping("/api/task/states")
class StateController(
    private val stateService: StateService
) {

    @PostMapping
    fun create(@RequestBody req: CreateStateRequest): StateResponse {
        return stateService.create(req)
    }

    @PutMapping
    fun update(@RequestBody req: UpdateStateRequest) = stateService.update(req)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = stateService.getOne(id)

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Long) = stateService.delete(id)

    @GetMapping
    fun getAllByWorkflowId(@RequestParam workflowId: Long, pageable: Pageable): Page<StateResponse> {
        return stateService.getAllByWorkflowId(workflowId, pageable)
    }
}

@RestController
@RequestMapping("/api/task/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun create(@RequestBody req: CreateTaskRequest): TaskResponse {
        return taskService.create(req)
    }

    @PutMapping fun update(@RequestBody req: UpdateTaskRequest) = taskService.update(req)

    @GetMapping("/{id}") fun getOne(@PathVariable id: Long) = taskService.getOne(id)

    @GetMapping("/my")
    fun myTasks(pageable: Pageable) = taskService.getMyTasks(pageable)

    @PostMapping("/delete/{id}") fun delete(@PathVariable id: Long) = taskService.delete(id)

    @GetMapping
    fun getAllByBoardId(@RequestParam boardId: Long, pageable: Pageable): Page<TaskResponse> {
        return taskService.getAllByBoardId(boardId, pageable)
    }

    @PutMapping("/state")
    fun changeState(@RequestBody req: ChangeTaskStateRequest): TaskResponse {
        return taskService.changeState(req)
    }
}



