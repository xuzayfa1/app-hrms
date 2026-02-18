package uz.zero.notification

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class TelegramLinkTokenController(private val telegramLinkTokenService: TelegramLinkTokenService) {

    @PostMapping("/token")
    fun generateToken() = telegramLinkTokenService.generateToken()


}