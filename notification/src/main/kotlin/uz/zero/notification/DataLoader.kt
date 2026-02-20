package uz.zero.notification

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*

@Component
class NotificationDataLoader(
    private val notificationRepository: NotificationRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (notificationRepository.count() == 0L) {
            val allNotifications = mutableListOf<Notification>()

            // 1. Tasklar ro'yxati
            val tasks = listOf(
                TaskTemplate("Fintech Bank", "Mobile App", "To'lov tizimini integratsiya qilish"),
                TaskTemplate("E-Commerce", "Web Site", "Savatcha funksiyasini tekshirish"),
                TaskTemplate("Logistics System", "Dashboard", "Yangi haydovchini biriktirish")
            )

            // 2. Userlar va ularning ismlari
            val userNames = mapOf(
                1L to "Alisher Navoiy",
                2L to "Zahiriddin Bobur",
                3L to "Amir Temur"
            )

            // 3. Jami 9 ta row yaratish
            tasks.forEach { task ->
                userNames.forEach { (id, name) ->
                    allNotifications.add(
                        Notification(
                            userId = id,
                            organizationName = task.org,
                            projectName = task.project,
                            ownerName = name, // Endi ism static emas, dynamic
                            title = task.title,
                            oldState = "TODO",
                            newState = "IN_PROGRESS",
                            status = NotificationStatus.PENDING,
                            actionDate = Date()
                        )
                    )
                }
            }

            notificationRepository.saveAll(allNotifications)
            println("✅ Bazaga 9 ta realistik notification yuklandi.")
        }
    }

    private data class TaskTemplate(val org: String, val project: String, val title: String)
}