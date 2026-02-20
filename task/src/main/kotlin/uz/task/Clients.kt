package uz.task

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody



@FeignClient(name = "user", url = "\${services.hosts.user}",configuration = [FeignOAuth2TokenConfig::class])
interface EmployeeFeignClient {

    @GetMapping("/{id}")
    fun getEmployee(@PathVariable("id") employeeId: Long): EmployeeDetailResponse


}




