//package uz.zero.notification
//
//import org.springframework.kafka.annotation.KafkaListener
//import org.springframework.stereotype.Service
//
//@Service
//class TaskEventConsumer(
//    private val employeeFeignClient: EmployeeFeignClient,
//    private val notificationRepository: NotificationRepository
//) {
//
//    @KafkaListener(topics = ["task-events"], groupId = "task-event-consumer-group")
//    fun consume(event: TaskEvent) {
//
//        event.ownerEmployeeId.let {
//            val employee = employeeFeignClient.getEmployee(it)
//            val msg = Notification(
//                userId = employee.user.id,
//                organizationName = event.orgName,
//                ownerName = event.ownerName,
//                title = event.newTitle,
//                oldState = event.fromState,
//                newState = event.toState,
//                actionDate = event.createdDate
//            )
//            notificationRepository.save(msg)
//        }
//
//        event.assignees.forEach {
//            val employee = employeeFeignClient.getEmployee(it)
//            val msg = Notification(
//                userId = employee.user.id,
//                organizationName = event.orgName,
//                ownerName = event.ownerName,
//                title = event.newTitle,
//                oldState = event.fromState,
//                newState = event.toState,
//                actionDate = event.createdDate
//            )
//            notificationRepository.save(msg)
//        }
//
//
//    }
//}