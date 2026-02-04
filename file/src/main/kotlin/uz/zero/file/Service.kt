package uz.zero.file

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter


interface FileService {
    fun upload(filePostDto: FilePostDto): String
    fun getByHashId(hashId: String): FileGetDto
    fun delete(hashId: String)
}

@Service
class FileServiceImpl(private val fileRepository: FileRepository) : FileService {

    private val storageRoot: Path = Paths.get(System.getProperty("user.dir"), "file", "file-storage")

    override fun upload(filePostDto: FilePostDto): String {

        filePostDto.run {

            val contentType =
                filePostDto.file.contentType ?: "unknown"

            val hashId = generateUniqueHashId()


            val folder = storageRoot.resolve(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")))
            Files.createDirectories(folder)
            val extension = filePostDto.file.originalFilename?.substringAfterLast('.') ?: "bin"
            val filename = "$hashId.$extension"
            val path = folder.resolve(filename)
            filePostDto.file.inputStream.use { Files.copy(it, path, StandardCopyOption.REPLACE_EXISTING) }


            val file = FileEntity(
                hashId = hashId,
                type = contentType,
                folderPath = folder.toString(),
                filename = filename,
                originalName = file.originalFilename ?: filename,
                size = file.size,
                orderNumber = orderNumber
            )
            fileRepository.save(file)
            return file.hashId
        }
    }

    override fun getByHashId(hashId: String): FileGetDto {
        val media = (fileRepository.findByHashIdAndDeletedFalse(hashId)
            ?: throw MediaNotFoundException())

        return FileGetDto.toResponse(media)
    }

    override fun delete(hashId: String) {
        val media = (fileRepository.findByHashIdAndDeletedFalse(hashId)
            ?: throw MediaNotFoundException())

        fileRepository.trash(media.id!!)
    }


    private fun generateUniqueHashId(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        val random = SecureRandom()
        var hashId: String
        do {
            hashId = buildString(11) {
                repeat(11) { append(alphabet[random.nextInt(alphabet.length)]) }
            }
        } while (fileRepository.existsByHashId(hashId))
        return hashId
    }



}

