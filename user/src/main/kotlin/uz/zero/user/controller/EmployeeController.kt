package uz.zero.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.user.EmployeeCreateRequest
import uz.zero.user.EmployeeDetailResponse
import uz.zero.user.EmployeeResponse
import uz.zero.user.services.EmployeeService
import uz.zero.user.EmployeeUpdateRequest

@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @GetMapping("/my-organizations")
    fun getMyOrganizations(
        @RequestHeader("X-User-Id", required = false) currentUserId: String?
    ): ResponseEntity<List<EmployeeResponse>> {
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val employees = employeeService.getEmployeesByUser(currentUserId.toLong())
        return ResponseEntity.ok(employees)
    }

    @GetMapping("/organization/{organizationId}")
    fun getEmployeesByOrganization(
        @PathVariable organizationId: Long,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<List<EmployeeResponse>> {
        // Only ADMIN and MANAGER can see all employees
        if (userRole != "ADMIN" && userRole != "MANAGER") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val employees = employeeService.getEmployeesByOrganization(organizationId)
        return ResponseEntity.ok(employees)
    }

    @GetMapping("/{id}")
    fun getEmployeeById(@PathVariable id: Long): ResponseEntity<EmployeeDetailResponse> {
        val employee = employeeService.getEmployeeById(id)
        return ResponseEntity.ok(employee)
    }

    @PostMapping
    fun assignEmployee(
        @Valid @RequestBody request: EmployeeCreateRequest,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<EmployeeResponse> {
        // Only ADMIN can assign employees
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val employee = employeeService.assignEmployeeToOrganization(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(employee)
    }

    @PutMapping("/{id}")
    fun updateEmployee(
        @PathVariable id: Long,
        @Valid @RequestBody request: EmployeeUpdateRequest,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<EmployeeResponse> {
        // Only ADMIN can update employees
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val employee = employeeService.updateEmployee(id, request)
        return ResponseEntity.ok(employee)
    }

    @DeleteMapping("/{id}")
    fun removeEmployee(
        @PathVariable id: Long,
        @RequestHeader("X-User-Role", required = false) userRole: String?
    ): ResponseEntity<Void> {
        // Only ADMIN can remove employees
        if (userRole != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        employeeService.removeEmployee(id)
        return ResponseEntity.noContent().build()
    }
}