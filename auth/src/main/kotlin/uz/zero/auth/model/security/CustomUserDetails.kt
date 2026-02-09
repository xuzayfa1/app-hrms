package uz.zero.auth.model.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val id: Long,
    private val username: String,
    private val role: String,
    private val enabled: Boolean,
    private val employeeId: Long,
    private val employeeRole: String,
    private val currentOrganizationId: Long
) : UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(role))

    override fun getPassword() = ""

    override fun getUsername() = username

    override fun isEnabled() = enabled
    fun getUserId() = id
    fun getRole(): String = role
    fun getEmployeeId() = employeeId
    fun getEmployeeRole() = employeeRole
    fun getCurrentOrganizationId() = currentOrganizationId
}