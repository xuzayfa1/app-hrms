package uz.zero.file


enum class ErrorCode(val code: Int) {
    FILE_TOO_LARGE(300),
    FILE_TYPE_NOT_ALLOWED(301),
    MEDIA_NOT_FOUND(302)
}

enum class Type{
    VIDEO,
    IMAGE,
}

enum class EmployeeRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}