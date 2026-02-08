package uz.task

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody



@FeignClient(name = "media", url = "\${services.hosts.media}",configuration = [FeignOAuth2TokenConfig::class])
interface MediaFeignClient {


}

@FeignClient(name = "reaction", url = "\${services.hosts.reaction}",configuration = [FeignOAuth2TokenConfig::class])
interface ReactionFeignClient {


}


