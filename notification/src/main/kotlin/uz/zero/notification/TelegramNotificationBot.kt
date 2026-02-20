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
    private val telegramLinkTokenService: TelegramLinkTokenService,
    private val telegramUserRepository: TelegramUserRepository
) : TelegramLongPollingBot(botToken) {

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId


            if (messageText.startsWith("/start")) {
                val parts = messageText.split(" ")

                if (parts.size > 1) {
                    val specialToken = parts[1]
                    handleNewUser(chatId, specialToken)
                }
                else{
                    if (!telegramUserRepository.existsByChatIdAndDeletedFalse(chatId)){
                        sendNotification(chatId, "Botdan foydalanish uchun sayt orqali registratsiya qiling!")
                    }
                }
            }
        }
    }

    private fun handleNewUser(chatId: Long, token: String) {

        val message = telegramLinkTokenService.checkToken(token, chatId)

        sendNotification(chatId, message)
    }

    fun sendNotification(chatId: Long, text: String) {
        val message = SendMessage(chatId.toString(), text)
        execute(message)
    }
}