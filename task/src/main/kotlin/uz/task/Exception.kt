package uz.task

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.Locale

@ControllerAdvice
class ExceptionHandler(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(Throwable::class)
    fun handleOtherExceptions(exception: Throwable): ResponseEntity<Any> {
        when (exception) {
            is FeignException -> {
                return ResponseEntity
                    .badRequest()
                    .body(exception.toBaseMessage())
            }

            is TaskException-> {

                return ResponseEntity
                    .badRequest()
                    .body(exception.getErrorMessage(errorMessageSource))
            }

            else -> {
                exception.printStackTrace()
                return ResponseEntity
                    .badRequest().body(
                        BaseMessage(100,
                            "Iltimos support bilan bog'laning")
                    )
            }
        }
    }

}



sealed class TaskException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null
    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments(),
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}

class ProjectNotFoundException : TaskException() {
    override fun errorType() = ErrorCode.PROJECT_NOT_FOUND
}

class BoardNotFoundException : TaskException() {
    override fun errorType() = ErrorCode.BOARD_NOT_FOUND
}

class AccessDeniedException : TaskException() {
    override fun errorType() = ErrorCode.ACCESS_DENIED
}

class WorkflowNotFoundException : TaskException() {
    override fun errorType() = ErrorCode.WORKFLOW_NOT_FOUND
}

class StateNotFoundException : TaskException() {
    override fun errorType() = ErrorCode.STATE_NOT_FOUND
}

class TaskNotFoundException : TaskException() {
    override fun errorType() = ErrorCode.TASK_NOT_FOUND
}

class InvalidStateWorkflowException : TaskException() {
    override fun errorType() = ErrorCode.INVALID_STATE_WORKFLOW
}

class SystemWorkflowReadonlyException : TaskException() {
    override fun errorType() = ErrorCode.SYSTEM_WORKFLOW_EXCEPTION
}

class DeadlineExpiredException : TaskException() {
    override fun errorType() = ErrorCode.DEADLINE_EXPIRED
}

class ProjectNotEmptyException : TaskException() {
    override fun errorType() = ErrorCode.PROJECT_NOT_EMPTY
}

class BoardNotEmptyException : TaskException() {
    override fun errorType() = ErrorCode.BOARD_NOT_EMPTY
}

class DeadlineInPastException : TaskException() {
    override fun errorType() = ErrorCode.DEADLINE_IN_PAST
}

class StateInUseException : TaskException() {
    override fun errorType() = ErrorCode.STATE_IN_USE
}



class FeignException(
    private val code: Int?,
    private val messageValue: String?
) : TaskException() {

    override fun errorType() = ErrorCode.FEIGN_ERROR

    fun toBaseMessage() = BaseMessage(code, messageValue)

}


