package uz.zero.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.server.WebFilter

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.apply { } }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .oauth2ResourceServer { server ->
                server.jwt {
                    it.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }
            .authorizeExchange {
                it
                    .pathMatchers("/actuator/**", "/error").permitAll()
                    .pathMatchers("/user/api/v1/login/**").permitAll()
                    .pathMatchers("/user/api/v1/users/register/**").permitAll()
                    .pathMatchers("/file/api/v1/files/view/**").permitAll()
                    .pathMatchers("/api/v1/auth/oauth2/**").permitAll()
                    .anyExchange().access(monoPathManager())
            }.build()
    }


    @Suppress("ReactorTransformationOnMonoVoid")
    @Bean
    fun contextHeaderWebFilter(objectMapper: ObjectMapper): WebFilter {
        return WebFilter { exchange, chain ->
            return@WebFilter ReactiveSecurityContextHolder.getContext()
                .map { it.authentication }
                .flatMap { auth ->
                    val details = auth.details
                    if (details is Map<*, *> && details.keys.all { it is String }) {
                        val mutatedRequest = exchange.request.mutate()

                        // JSON formatida barcha ma'lumotlarni yuborish (siqilgan holda)
                        val detailsJson = objectMapper.writeValueAsString(details).compress()
                        mutatedRequest.header(USER_DETAILS_HEADER_KEY, detailsJson)

                        // Alohida headerlar sifatida chiqarish
                        mutatedRequest.header(USER_ID_HEADER_KEY, details[USER_ID_KEY]?.toString() ?: "")
                        mutatedRequest.header(USER_NAME_HEADER_KEY, details[USER_USERNAME_KEY]?.toString() ?: "")

                        // --- YANGI QISM: Org-Id ni qo'shamiz ---
                        val orgId = details[USER_ORG_ID_KEY]?.toString() ?: ""
                        mutatedRequest.header(USER_ORG_ID_HEADER_KEY, orgId)

                        // Exchange attributes (logging yoki boshqa filtrlar uchun)
//                        exchange.attributes[USER_ID_HEADER_KEY] = details[USER_ID_KEY]
//                        exchange.attributes[USER_NAME_HEADER_KEY] = details[USER_USERNAME_KEY]
//                        exchange.attributes[USER_ORG_ID_HEADER_KEY] = orgId

                        // Exchange attributes qismini shunday o'zgartiring:

                        details[USER_ID_KEY]?.let { exchange.attributes[USER_ID_HEADER_KEY] = it }
                        details[USER_USERNAME_KEY]?.let { exchange.attributes[USER_NAME_HEADER_KEY] = it }
                        if (orgId.isNotEmpty()) {
                            exchange.attributes[USER_ORG_ID_HEADER_KEY] = orgId
                        }
                        // ---------------------------------------

                        val newExchange = exchange.mutate().request(mutatedRequest.build()).build()
                        return@flatMap chain.filter(newExchange)
                    } else {
                        return@flatMap chain.filter(exchange)
                    }
                }.switchIfEmpty(chain.filter(exchange))
        }
    }


    private fun monoPathManager() = ReactiveAuthorizationManager<AuthorizationContext> { mono, context ->
        mono.map { auth ->
            try {
                val requestPath = context.exchange.request.path.value()
                val serviceName = requestPath.extractServiceName()
                val details = auth.details
                return@map AuthorizationDecision(true)

            } catch (e: Exception) {
                e.printStackTrace()
                return@map AuthorizationDecision(false)
            }
        }
    }
}