package uz.task

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody



@FeignClient(name = "user", url = "\${services.hosts.user}",configuration = [FeignOAuth2TokenConfig::class])
interface EmployeeFeignClient {

    @GetMapping("/employees/{id}")
    fun getEmployee(@PathVariable("id") employeeId: Long): EmployeeDetailResponse
}

@FeignClient(name = "notification", url = "\${services.hosts.notification}",configuration = [FeignOAuth2TokenConfig::class])
interface NotifFeignClient {

    @PostMapping("/notifications")
    fun setNotif(@RequestBody task: TaskEvent)
}

@FeignClient(name = "file", url = "\${services.hosts.file}",configuration = [FeignOAuth2TokenConfig::class])
interface FileFeignClient {

    @GetMapping("/media/{id}")
    fun getByHashId(@PathVariable id: String):FileGetDto
}




