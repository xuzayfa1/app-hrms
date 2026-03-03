package uz.zero.file

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping
class FileController(private val fileService: FileService) {

    @PostMapping("/add")
    fun addOrUpdate(@ModelAttribute filePostDto: FilePostDto) = fileService.upload(filePostDto)

    @GetMapping("/media/{id}")
    fun getByHashId(@PathVariable id: String) = fileService.getByHashId(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = fileService.delete(id)
}