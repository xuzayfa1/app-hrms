package uz.zero.auth.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.auth.model.requests.GenerateTokenRequest
import uz.zero.auth.model.responses.BaseMessage
import uz.zero.auth.model.responses.GenerateTokenResponse
import uz.zero.auth.services.security.TokenGenerationService

@RestController
@RequestMapping("/internal/token")
class InternalTokenController(
    private val tokenGenerationService: TokenGenerationService
) {
    @PostMapping("/generate")
    fun generateToken(@RequestBody request: GenerateTokenRequest): ResponseEntity<GenerateTokenResponse> {
        val tokenResponse = tokenGenerationService.generateToken(request)
        return ResponseEntity.ok((tokenResponse))
    }
}