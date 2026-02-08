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

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        projectService.delete(id)
    }
}



