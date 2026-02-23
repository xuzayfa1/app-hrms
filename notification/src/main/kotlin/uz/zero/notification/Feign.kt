package uz.zero.notification

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "user", url = "\${services.hosts.user}",configuration = [FeignOAuth2TokenConfig::class])
interface EmployeeFeignClient {

    @GetMapping("/employees/{id}")
    fun getEmployee(@PathVariable("id") employeeId: Long):EmployeeDetailResponse


}