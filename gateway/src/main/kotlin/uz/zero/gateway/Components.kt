package uz.zero.gateway

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

//@Component
//class JwtAuthenticationConverter(
//    private val authService: AuthService,
//) : Converter<Jwt, Mono<JwtAuthenticationToken>> {
//    override fun convert(source: Jwt): Mono<JwtAuthenticationToken> {
//        return authService.getUserInfo(source.tokenValue)
//            .flatMap { userInfo: Map<String, Any?> ->
//                val username = userInfo[USER_USERNAME_KEY] as String
//                val role = userInfo[USER_ROLE_KEY] as String
//                val authorities = listOf(SimpleGrantedAuthority(role))
//                val jwtToken = JwtAuthenticationToken(source, authorities, username)
//
//                jwtToken.details = userInfo
//
//                Mono.just(jwtToken)
//            }.onErrorResume { ex ->
//                val msg = "Failed to authenticate with token"
//                val cause = if (ex is WebClientResponseException)
//                    RuntimeException("${msg}: ${ex.statusCode}", ex)
//                else ex
//                Mono.error(BadCredentialsException(msg, cause))
//            }
//    }
//}


@Component
class JwtAuthenticationConverter : Converter<Jwt, Mono<JwtAuthenticationToken>> {

    override fun convert(source: Jwt): Mono<JwtAuthenticationToken> {
        return Mono.fromCallable {
            // Ma'lumotlarni bevosita JWT ichidan olamiz
            // source.claims - bu Map<String, Any>
            val username = source.getClaimAsString("preferred_username") ?: source.subject

            // Rollarni tokendagi nomiga qarab oling (masalan: "roles" yoki "role")
            val roles = source.getClaim<List<String>>("roles") ?: listOf("ROLE_USER")
            val authorities = roles.map { SimpleGrantedAuthority(it) }

            val jwtToken = JwtAuthenticationToken(source, authorities, username)

            // Barcha claim'larni details qismiga saqlab qo'yish mumkin
            jwtToken.details = source.claims

            jwtToken
        }.onErrorResume { ex ->
            Mono.error(BadCredentialsException("Token ichidan ma'lumotlarni o'qishda xatolik", ex))
        }
    }
}