package uz.zero.notification

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant

interface TelegramUserService {

}


@Service
class TelegramUserServiceImpl : TelegramUserService {

}



interface TelegramLinkTokenService{
    fun generateToken(): String
    fun checkToken(token: String, chatId: Long): Boolean
}


@Service
class TelegramLinkTokenServiceImpl(
    @param:Value("\${telegram.bot.username}") private val botUsername: String,
    private val telegramLinkTokenRepository: TelegramLinkTokenRepository): TelegramLinkTokenService {

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

    override fun checkToken(token: String, chatId: Long): Boolean {
        telegramLinkTokenRepository.findByHashIdAndUsedFalse(token)?.let {
            if (it.expiresAt <= Instant.now() || it.used) {
                return false
            }
            it.used = true
            telegramLinkTokenRepository.save(it)
            return true
        }
        return false
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

}

@Service
class NotificationServiceImpl: NotificationService{

}

