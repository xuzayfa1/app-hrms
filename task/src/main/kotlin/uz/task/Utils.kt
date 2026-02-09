package uz.task


class Utils() {
    companion object {
        fun checkPosition() {
            val permission = Context.employeePos()
            if (permission != EmployeeRole.MANAGER.name) throw AccessDeniedException()
        }
    }
}