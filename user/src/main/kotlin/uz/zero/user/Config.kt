package uz.zero.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component

@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val objectMapper: ObjectMapper,
){

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt {
                    it.jwtAuthenticationConverter(JwtAuthenticationConverter())
                }
            }
            .build()
    }


//    fun jwtAuthenticationConverter(): Converter<Jwt, JwtAuthenticationToken> {
//        return Converter<Jwt, JwtAuthenticationToken> { source ->
//            source
//            val userDetailsJson = getHeader(USER_DETAILS_HEADER_KEY)?.decompress()
//            println("++++++++++++++++++++++++++++++++++++++++++++++++")
//            println(userDetailsJson)
//            println("++++++++++++++++++++++++++++++++++++++++++++++++")
//            val userDetails = userDetailsJson?.run { objectMapper.readValue(this, UserInfoResponse::class.java) }
//            val username = userDetails?.username ?: username()
//            val authorities = mutableListOf<SimpleGrantedAuthority>()
//            if (userDetails != null) {
//                authorities.add(SimpleGrantedAuthority("ROLE_${userDetails.role}"))
//            }
//            JwtAuthenticationToken(source, authorities, username).apply {
//                details = userDetails
//            }
//        }
//    }


    @Component
    class JwtAuthenticationConverter : Converter<Jwt, JwtAuthenticationToken> {
        override fun convert(source: Jwt): JwtAuthenticationToken {
            // JWT ichidagi claimlarni to'g'ridan-to'g'ri o'qiymiz
            val claims = source.claims

            // Authorities (Rollar)
            val role = claims["rol"]?.toString() ?: "USER"
            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

            // Username
            val username = source.subject ?: "unknown"

            return JwtAuthenticationToken(source, authorities, username).apply {
                // MUHIM: details'ga Map yuklaymiz, shunda filtr uni o'qiy oladi
                details = claims
            }
        }
    }
}



