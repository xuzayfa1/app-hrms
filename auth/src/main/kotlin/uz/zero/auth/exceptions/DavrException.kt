package uz.zero.auth.exceptions

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import uz.zero.auth.model.responses.BaseMessage
import uz.zero.auth.enums.ErrorCode

sealed class DavrException : RuntimeException() {

    abstract fun errorCode(): ErrorCode

    open fun getErrorMessageArguments(): Array<Any?>? = null

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        val errorMessage = try {
            errorMessageSource.getMessage(errorCode().name, getErrorMessageArguments(), LocaleContextHolder.getLocale())
        } catch (e: Exception) {
            e.message
        }
        return BaseMessage(errorCode().code, errorMessage)
    }
}

class UsernameAlreadyTakenException :  DavrException() {
    override fun errorCode() = ErrorCode.USERNAME_ALREADY_TAKEN
}