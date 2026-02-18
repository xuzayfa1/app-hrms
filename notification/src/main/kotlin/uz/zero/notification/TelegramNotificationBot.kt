package uz.zero.notification


import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramNotificationBot(
    @param:Value("\${telegram.bot.token}") private val botToken: String,
    @param:Value("\${telegram.bot.username}") private val botUsername: String,
    private val telegramLinkTokenService: TelegramLinkTokenService // Sizning backend servisiz
) : TelegramLongPollingBot(botToken) {

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            // /start buyrug'ini va tokenni tekshiramiz
            if (messageText.startsWith("/start")) {
                val parts = messageText.split(" ")

                if (parts.size > 1) {
                    val specialToken = parts[1] // Linkdagi o'sha token
                    handleNewUser(chatId, specialToken)
                }
//                else {
//                    sendNotification(chatId, "Salom! Botga xush kelibsiz.")
//                }
            }
        }
    }

    private fun handleNewUser(chatId: Long, token: String) {
        // Backend orqali userni topish va chatId ni saqlash
        val success = telegramLinkTokenService.checkToken(token, chatId)

        if (success) {
            sendNotification(chatId, "Siz muvaffaqiyatli ro'yxatdan o'tdingiz!")
        } else {
            sendNotification(chatId, "Xatolik: Token haqiqiy emas yoki muddati o'tgan.")
        }
    }

    fun sendNotification(chatId: Long, text: String) {
        val message = SendMessage(chatId.toString(), text)
        execute(message)
    }
}