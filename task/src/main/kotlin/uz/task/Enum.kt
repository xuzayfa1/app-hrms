package uz.task

enum class ErrorCode(val code: Int) {
    PROJECT_NOT_FOUND(300),
    BOARD_NOT_FOUND(301),
    ACCESS_DENIED(302),

    WORKFLOW_NOT_FOUND(303),
    STATE_NOT_FOUND(304),
    TASK_NOT_FOUND(305),

    INVALID_STATE_WORKFLOW(306),
    SYSTEM_WORKFLOW_EXCEPTION(307),
    DEADLINE_EXPIRED(308),

    FEIGN_ERROR(400),


}

enum class Status{
    ACTIVE,
    INACTIVE
}

enum class Permission{
    OWNER,
    ASSIGNEE
}

enum class EmployeeRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}

