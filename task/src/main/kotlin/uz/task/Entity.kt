package uz.task

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var lastModifiedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)



@Entity
class Project(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var organizationId: Long,
    @Enumerated(EnumType.STRING)var status: Status = Status.ACTIVE,
):BaseEntity()


@Entity
class Board(
    @Column(nullable = false) var name: String,
    @ManyToOne(fetch = FetchType.LAZY) var project: Project,
    @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
):BaseEntity()

@Entity
class Workflow(

    @Column(nullable = false) var name: String,

    @ManyToOne(fetch = FetchType.LAZY) var board: Board? = null,

    var organizationId: Long? = null

) : BaseEntity()


@Entity
class State(

    @Column(nullable = false) var name: String,
    @Column(nullable = false)var orderNumber: Long,
    @ManyToOne(fetch = FetchType.LAZY)var workflow: Workflow,
    @Enumerated(EnumType.STRING)var permission: Permission

):BaseEntity()



@Entity
class Task(
    @Column(nullable = false)var title: String,
    @Column(nullable = false)var description: String,
    @ManyToOne(fetch = FetchType.LAZY) var board: Board,
    @ManyToOne(fetch = FetchType.LAZY) var state: State,
    @Column(nullable = false) var ownerId:Long,
    @Temporal(TemporalType.TIMESTAMP) var deadline: Date? = null,
):BaseEntity()

@Entity
@Table(
    name = "task_assignee",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["task_id", "employee_id"])
    ]
)
class TaskAssignee(
    @ManyToOne(fetch = FetchType.LAZY) var task: Task,
    @Column(nullable = false) var employeeId: Long
) : BaseEntity()

@Entity
class TaskMedia(
    @ManyToOne(fetch = FetchType.LAZY) var task: Task,
    @Column(nullable = false)var hashId: String
):BaseEntity()

@Entity
class TaskAction(
    @ManyToOne(fetch = FetchType.LAZY) var task: Task,

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var type: TaskActionType,

    @Column(nullable = false)var employeeId: Long,
    @Column(nullable = false) var employeeName: String,
    var from: String? = null,
    var to: String? = null,
    var title: String? = null,
    @ManyToOne(fetch = FetchType.LAZY) var assignee: TaskAssignee? = null,
    @Column(columnDefinition = "text")var fileAttach: String? = null,
    var deadline: Date? = null,

//    @Column(columnDefinition = "text") var data: String? = null


):BaseEntity()