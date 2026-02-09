package uz.zero.user

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,

    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,

    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var updatedDate: Date? = null,

    @CreatedBy var createdBy: Long? = null,

    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,

    @Column(nullable = false) var isActive: Boolean = true

)


@Entity
class User(
    var username: String,
    var email: String,
    var firstName: String,
    var lastName: String,
    var authUserId: Long? = null,
    var currentOrgId: Long? = null
) : BaseEntity()

@Entity
class Organization(
    var name: String,
    var description: String? = null,
    @OneToMany(mappedBy = "organization")
    var employees: MutableList<Employee> = mutableListOf()
) : BaseEntity()

@Entity
class Employee(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    var organization: Organization,

    @Enumerated(EnumType.STRING)
    var role: EmployeeRole = EmployeeRole.EMPLOYEE,

    @Column(nullable = false, updatable = false)
    var joinedAt: LocalDateTime = LocalDateTime.now()
): BaseEntity()