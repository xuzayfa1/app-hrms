package uz.zero.auth.model.responses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfoResponse(
    val id: Long,
    val username: String,
    val role: String,
)