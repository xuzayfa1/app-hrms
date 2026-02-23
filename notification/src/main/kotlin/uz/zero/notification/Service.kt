package uz.zero.notification

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.Date

interface TelegramUserService {
    fun connectChatId(userId: Long, chatId: Long)
}


@Service
class TelegramUserServiceImpl(private val telegramUserRepository: TelegramUserRepository) : TelegramUserService {
    override fun connectChatId(userId: Long, chatId: Long) {
        val user = telegramUserRepository.findByUserIdAndDeletedFalse(userId) ?: TelegramUser(userId = userId, chatId = chatId)

        user.apply {
            this.active = true
            telegramUserRepository.save(this)
        }
    }

}



interface TelegramLinkTokenService{
    fun generateToken(): String
    fun checkToken(token: String, chatId: Long): String
}


@Service
class TelegramLinkTokenServiceImpl(
    @param:Value("\${telegram.bot.username}") private val botUsername: String,
    private val telegramLinkTokenRepository: TelegramLinkTokenRepository,
    private val telegramUserService: TelegramUserService): TelegramLinkTokenService {

    override fun generateToken(): String {
        val hashId = generateUniqueHashId()
        val telegramLinkToken = TelegramLinkToken(
            userId = userId(),
            hashId = hashId,
            expiresAt = Instant.now().plusSeconds(15*60)
        )
        telegramLinkTokenRepository.save(telegramLinkToken)
        return "t.me/$botUsername?start=$hashId"
    }

    override fun checkToken(token: String, chatId: Long): String {
        telegramLinkTokenRepository.findByHashIdAndUsedFalse(token)?.let {
            if (it.expiresAt <= Instant.now()) {
                return "Something went wrong! \nTry again."
            }
            it.used = true
            telegramLinkTokenRepository.save(it)
            telegramUserService.connectChatId(it.userId, chatId)
            return "Successfully registration!"
        }
        return "Something went wrong! \nTry again."
    }


    private fun generateUniqueHashId(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        val random = SecureRandom()
        var hashId: String
        do {
            hashId = buildString(11) {
                repeat(11) { append(alphabet[random.nextInt(alphabet.length)]) }
            }
        } while (telegramLinkTokenRepository.existsByHashId(hashId))
        return hashId
    }
}



interface NotificationService{
    fun sendNotification()
    fun addNotification(event: TaskEvent)
}
@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val telegramUserRepository: TelegramUserRepository,
    private val telegramNotificationBot: TelegramNotificationBot,
    private val feignClient: EmployeeFeignClient
) : NotificationService {


    @Transactional
    override fun sendNotification() {
        val pending = notificationRepository.findTop30ByStatusOrderByCreatedDateAsc(NotificationStatus.PENDING)

        pending.forEach { notification ->
            try {
                val user = telegramUserRepository.findByUserIdAndActiveTrue(notification.userId)

                if (user == null) {
                    notification.status = NotificationStatus.FAILED
                    notification.error = "User not found or inactive"
                } else {
                    val message = notification.title
                    telegramNotificationBot.sendNotification(user.chatId, message)
                    notification.status = NotificationStatus.SENT
                }
            } catch (e: Exception) {
                notification.status = NotificationStatus.FAILED
                notification.error = e.message
            } finally {
                notificationRepository.save(notification)
            }
        }
    }

    override fun addNotification(event: TaskEvent) {

        //Owner notif
        run {
            val ownerEmp = feignClient.getEmployee(event.ownerEmployeeId)
            val text = buildMessage(event,event.ownerEmployeeId)

            notificationRepository.save(
                Notification(
                    userId = ownerEmp.user.id,
                    organizationName = event.orgName,
                    ownerName = event.ownerName,
                    title = text,
                    oldState = event.fromState,
                    newState = event.toState,
                    actionDate = event.createdDate
                )
            )
        }


        val assignees = event.assignees.distinct()
        if (assignees.isEmpty()) return

        assignees.forEach { assigneeEmployeeId ->
            val emp = feignClient.getEmployee(assigneeEmployeeId)
            val text = buildMessage(event,assigneeEmployeeId)

            notificationRepository.save(
                Notification(
                    userId = emp.user.id,
                    organizationName = event.orgName,
                    ownerName = event.ownerName,
                    title = text,
                    oldState = event.fromState,
                    newState = event.toState,
                    actionDate = event.createdDate
                )
            )
        }
    }


    private fun buildMessage(event: TaskEvent, receiverEmployeeId: Long): String {
        val lines = mutableListOf<String>()

        lines += "📌 Topshiriq yangilandi"
        lines += "🕒 ${event.createdDate ?: Date()}"
        lines += "🏢 Tashkilot: ${event.orgName}"
        lines += "📚 Loyiha: ${event.projectName}"
        lines += "👨‍💼 Harakat egasi: ${event.ownerName}"
        lines += "💾 Sarlavha: ${event.newTitle}"
        lines += "📶 Holat: ${stateText(event)}"

        val changes = mutableListOf<String>()

        if (!event.toState.isNullOrBlank()) {
            changes += "🔁 Holat o'zgartirildi"
        }

        if (event.newDeadline != null) {
            changes += "⏰ Deadline yangilandi: ${event.newDeadline}"
        }

        if (!event.newFileAttach.isNullOrEmpty()) {
            changes += "📎 Fayl biriktirildi: ${event.newFileAttach.size} ta"
            event.newFileAttach.forEach {
//                changes += """🔗 <a href="http://localhost:8080/api/v1/file/${it}">Faylni yuklash</a>"""
                changes += "🔗 Faylni yuklash: http://localhost:8080/api/v1/file/${it}"
            }
        }


        if (event.assigneeEmployeeId != null) {
            if (receiverEmployeeId == event.assigneeEmployeeId) {
                changes += "✅ Siz topshiriqqa biriktirildingiz"
            } else {
                val addedEmp = feignClient.getEmployee(event.assigneeEmployeeId)
                val addedName = addedEmp.user.firstName + " " + addedEmp.user.lastName
                changes += "👥 $addedName biriktirildi"
            }
        }

        if (changes.isNotEmpty()) {
            lines += ""
            lines += "O'zgarishlar:"
            lines += changes.joinToString("\n")
        }

        lines += ""
//        lines +=  """🔗 <a href="http://localhost:8080/api/v1/task/tasks/${event.taskId}">Topshiriqni ochish</a>"""
        lines += "\uD83D\uDD17 Topshiriqni ochish: http://localhost:8080/api/v1/task/tasks/${event.taskId}"
        return lines.joinToString("\n")
    }

    private fun stateText(event: TaskEvent): String {
        return if (event.toState.isNullOrBlank()) {
            event.fromState
        } else {
            "${event.fromState} → ${event.toState}"
        }
    }
}
