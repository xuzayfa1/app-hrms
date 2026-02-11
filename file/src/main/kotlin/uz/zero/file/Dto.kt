package uz.zero.file

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.multipart.MultipartFile


data class BaseMessage(val code: Int? = null, val message: String? = null){
    companion object{
        var OK = BaseMessage(0,"OK")
    }
}


data class FilePostDto(
    var orderNumber: Long,
    var file: MultipartFile,
)

data class FileGetDto(
    var id: Long,
    var hashId: String,
    var folderPath: String,
    var filename: String,
    var type: String,
    var originalName: String,
    var size: Long,
    var orderNumber: Long
){
    companion object{
        fun toResponse(files: FileEntity) = FileGetDto(
            id = files.id!!,
            hashId = files.hashId,
            folderPath = files.folderPath,
            filename = files.filename,
            type = files.type,
            originalName = files.originalName,
            size = files.size,
            orderNumber = files.orderNumber
        )
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoResponse(
    @JsonProperty("uid")
    val id: Long,

    @JsonProperty("sub")
    val username: String,

    @JsonProperty("rol")
    val role: String,

    val enabled: Boolean = true,

    @JsonProperty("eid")
    val employeeId: Long,

    @JsonProperty("per")
    val employeeRole: String,

    @JsonProperty("oid")
    val currentOrganizationId: Long
)
