package uz.zero.user

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (userRepository.existsByUsername("admin")) {
            logger.info("Admin user already exists, skipping data loader")
            return
        }

        logger.info("Creating default admin user...")

        val admin = userRepository.save(
            User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                email = "admin@hrms.uz",
                firstName = "Admin",
                lastName = "System"
            )
        )

        val org = organizationRepository.save(
            Organization(
                name = "Default Organization",
                description = "Tizim tomonidan yaratilgan standart tashkilot"
            ).apply { createdBy = admin.id }
        )

        employeeRepository.save(
            Employee(
                user = admin,
                organization = org,
                role = EmployeeRole.ADMIN
            )
        )

        admin.currentOrgId = org.id
        userRepository.save(admin)

        logger.info("Admin user created: username=admin, password=admin123")
    }
}
