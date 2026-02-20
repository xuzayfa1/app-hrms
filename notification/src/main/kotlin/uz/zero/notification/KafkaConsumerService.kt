package uz.zero.notification


import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class TaskEventConsumer(
    private val taskEventLogRepository: TaskEventLogRepository
) {

    @KafkaListener(topics = ["task-events"], groupId = "task-event-consumer-group")
    fun consume(event: TaskEvent) {
        val log = TaskEventLog(
            taskId = event.taskId,
            action = event.action,
            timestamp = event.timestamp
        )
        taskEventLogRepository.save(log)
    }
}
