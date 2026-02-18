package uz.zero.notification

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@Configuration
class BotInitializer(
    private val telegramBot: TelegramNotificationBot
) {

    @PostConstruct
    fun init() {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(telegramBot)
    }
}