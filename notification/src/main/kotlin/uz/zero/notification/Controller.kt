package uz.zero.notification

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class TelegramLinkTokenController(private val telegramLinkTokenService: TelegramLinkTokenService) {

    @PostMapping("/token")
    fun generateToken() = telegramLinkTokenService.generateToken()


}

@RestController
@RequestMapping("/notifications")
class NotificationController(private val notificationService: NotificationService) {

    @PostMapping()
    fun addNotification(@RequestBody event: TaskEvent) = notificationService.addNotification(event)
}