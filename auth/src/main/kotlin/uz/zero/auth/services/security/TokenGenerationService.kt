package uz.zero.auth.services.security

import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import uz.zero.auth.constants.*
import uz.zero.auth.model.requests.GenerateTokenRequest
import uz.zero.auth.model.responses.GenerateTokenResponse
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class TokenGenerationService(
    private val jwtEncoder: JwtEncoder
) {
    fun generateToken(request: GenerateTokenRequest): GenerateTokenResponse {
        val now = Instant.now()
        val expiresAt = now.plus(15, ChronoUnit.MINUTES)
        
        val claims = JwtClaimsSet.builder()
            .issuer("http://localhost:8089")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(request.username)
            .claim(JWT_USER_ID_KEY, request.userId)
            .claim(JWT_ROLE_KEY, request.role)
            .claim(ORG_ID_KEY, request.organizationId)
            .claim(EMPLOYEE_ID_KEY, request.employeeId)
            .claim(EMPLOYEE_POS_KEY, request.employeeRole)
            .build()
        
        val jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims))
        
        return GenerateTokenResponse(
            accessToken = jwt.tokenValue,
            refreshToken = null,
            expiresIn = ChronoUnit.SECONDS.between(now, expiresAt)
        )
    }
}