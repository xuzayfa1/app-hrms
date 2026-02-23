package uz.zero.notification


enum class ErrorCode(val code: Int) {

}

enum class NotificationStatus{
    PENDING,
    SENT,
    FAILED
}

enum class EmployeeRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}