//package uz.task
//
//
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.stereotype.Service
//
//@Service
//class TaskKafkaProducer(
//    private val kafkaTemplate: KafkaTemplate<String, TaskEvent>
//) {
//    fun send(taskEvent: TaskEvent) {
//        kafkaTemplate.send("task-events", taskEvent)
//    }
//}
