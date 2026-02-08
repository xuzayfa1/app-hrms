package uz.task

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional



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

