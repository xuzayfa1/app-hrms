package uz.task


import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional



@Component
class DefaultStateLoader(
    private val workflowRepository: WorkflowRepository,
    private val stateRepository: StateRepository,

) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(this::class.java)


    @Transactional
    override fun run(vararg args: String?) {
        createSystemDefault()
    }


    private fun createSystemDefault() {
        try {

            if (workflowRepository.count() == 0L) {

                logger.info("Creating system default")
                val workflow = Workflow(
                    name = "System default"
                )
                workflowRepository.save(workflow)

                val stateList = mutableListOf<State>()

                val stateToDo = State(
                    name = "TODO",
                    workflow = workflow,
                    orderNumber = 1,
                    permission = Permission.OWNER
                )
                stateList.add(stateToDo)

                val stateProg = State(
                    name = "IN_PROGRESS",
                    workflow = workflow,
                    orderNumber = 2,
                    permission = Permission.ASSIGNEE
                )
                stateList.add(stateProg)

                val stateRev = State(
                    name = "REVIEW",
                    workflow = workflow,
                    orderNumber = 3,
                    permission = Permission.ASSIGNEE
                )
                stateList.add(stateRev)

                val stateCom = State(
                    name = "COMPLETE",
                    workflow = workflow,
                    orderNumber = 4,
                    permission = Permission.OWNER
                )
                stateList.add(stateCom)

                stateRepository.saveAll(stateList)

                logger.info("System default states successfully loaded")
            }
        } catch (e: Exception) {
            logger.warn("Couldn't create default states. Stacktrace: ${e.stackTraceToString()}")
        }
    }

}