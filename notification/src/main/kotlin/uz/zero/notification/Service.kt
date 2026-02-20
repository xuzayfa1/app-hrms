package uz.zero.notification

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant

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
                return "Token is expired"
            }
            it.used = true
            telegramLinkTokenRepository.save(it)
            telegramUserService.connectChatId(it.userId, chatId)
            return "Hello World"
        }
        return "Token is invalid"
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
}
@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val telegramUserRepository: TelegramUserRepository,
    private val telegramNotificationBot: TelegramNotificationBot
) : NotificationService {

    @Transactional
    override fun sendNotification() {
        val pendingNotifications = notificationRepository.findTop30ByStatusOrderByCreatedDateAsc(NotificationStatus.PENDING)

        pendingNotifications.forEach { notification ->
            try {
                val user = telegramUserRepository.findByUserIdAndActiveTrue(notification.userId)

                if (user != null) {
                    val message = """
                        Topshiriqning holati o'zgartirildi
                        🕒 ${notification.getFormattedDate()}
                        🏢 Tashkilot nomi: ${notification.organizationName}
                        📚 Loyiha nomi: ${notification.projectName}
                        👨‍💼 Harakat egasi: ${notification.ownerName}
                        💾 Sarlavha:
                        ${notification.title}
                        📶 Holat: ${notification.oldState} >> ${notification.newState}
                        🔗 Topshiriqni ochish
                    """.trimIndent()

                    telegramNotificationBot.sendNotification(user.chatId, message)
                    notification.status = NotificationStatus.SENT
                } else {
                    notification.status = NotificationStatus.FAILED
                    notification.error = "User not found or inactive"
                }
            } catch (e: Exception) {
                notification.status = NotificationStatus.FAILED
                notification.error = e.message
            } finally {
                notificationRepository.save(notification)
            }
        }
    }
}
