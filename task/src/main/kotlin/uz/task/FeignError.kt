package uz.task

import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.stereotype.Component

@Component
class CustomFeignErrorDecoder(
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ErrorDecoder {

    override fun decode(methodKey: String, response: Response): Exception {

        val body = response.body()
            ?.asInputStream()
            ?.readBytes()
            ?.toString(Charsets.UTF_8)

        if (body.isNullOrBlank()) {
            return FeignException.errorStatus(methodKey, response)
        }

        return try {
            val baseMessage = objectMapper.readValue(body, BaseMessage::class.java)

            FeignException(
                code = baseMessage.code,
                messageValue = baseMessage.message
            )

        } catch (e: Exception) {
            FeignException.errorStatus(methodKey, response)
        }
    }
}