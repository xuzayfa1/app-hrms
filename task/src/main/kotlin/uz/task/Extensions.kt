package uz.task

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.util.Base64
import java.util.zip.GZIPInputStream
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.io.use


fun getHeader(headerKey: String): String? {
    return try {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        request.getHeader(headerKey)
    } catch (e: Exception) {
        null
    }
}


fun getUserJwtPrincipal(): Jwt? {
    val principal = SecurityContextHolder.getContext().authentication?.principal

    if (principal is Jwt) {
        return principal
    }

    return null
}


fun String.decompress(): String {
    val bytes = Base64.getDecoder().decode(this)
    return GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
}

fun username(): String {
    return getUserJwtPrincipal()?.claims?.get(USERNAME_KEY) as String
}

fun userId(): Long {
    return getUserJwtPrincipal()?.claims?.get(USER_ID_KEY) as Long
}
fun orgId1(): Long {
    return getUserJwtPrincipal()?.claims?.get(ORG_ID_KEY) as Long
}

fun employeeId1(): Long {
    return getUserJwtPrincipal()?.claims?.get(EMPLOYEE_ID_KEY) as Long
}

fun employeePosition1(): String {
    return getUserJwtPrincipal()?.claims?.get(EMPLOYEE_POS_KEY) as String
}

object Context {
    fun orgId(): Long = orgId1()
    fun employeeId(): Long = employeeId1()
    fun employeePos(): String = employeePosition1()
}
