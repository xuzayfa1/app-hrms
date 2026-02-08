package uz.task

enum class ErrorCode(val code: Int) {
    PROJECT_NOT_FOUND(300),
    BOARD_NOT_FOUND(301),
    ACCESS_DENIED(302),

    FEIGN_ERROR(400),


}

enum class Status{
    ACTIVE,
    INACTIVE

}

