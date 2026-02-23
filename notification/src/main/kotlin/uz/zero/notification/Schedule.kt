package uz.zero.notification

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class NotificationScheduler(
    private val notificationService: NotificationService
) {

    @Scheduled(fixedDelay = 1000, initialDelay = 2000)
    fun sync() {
        notificationService.sendNotification()
    }
}