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
@Table(name = "projects")
class Project(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var organizationId: Long,
    @Enumerated(EnumType.STRING)var status: Status = Status.ACTIVE,
):BaseEntity()

@Entity
@Table(name = "boards")
class Board(
    @Column(nullable = false) var name: String,
    @ManyToOne(fetch = FetchType.LAZY) @Column(nullable = false)var project: Project,
    @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
):BaseEntity()

@Entity
@Table(name = "workflows")
class Workflow(

    @Column(nullable = false) var name: String,

    var organizationId: Long? = null

) : BaseEntity()


@Entity
@Table(name = "states")
class State(

    @Column(nullable = false) var name: String,

    @Column(nullable = false) var terminal: Boolean = false

    //todo order bolishi kerak , workflowga ulanishi kerak , kim otkazoladi enum permission bolishi kerak

):BaseEntity()

//@Entity
//@Table(
//    name = "state_transitions",
//    uniqueConstraints = [
//        UniqueConstraint(columnNames = ["from_state_id", "to_state_id"])
//    ]
//)
//class StateTransition(
//
//    @ManyToOne(fetch = FetchType.LAZY) var fromState: State,
//
//    @ManyToOne(fetch = FetchType.LAZY) var toState: State
//
//) :BaseEntity()



@Entity
@Table(name = "tasks")
class Task(
    var title: String,
    var description: String,
    @ManyToOne(fetch = FetchType.LAZY) var board: Board,
    @ManyToOne(fetch = FetchType.LAZY) var state: State,
    @Column(nullable = false) var ownerId:Long
):BaseEntity()

@Entity
@Table(
    name = "task_assignees",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["task_id", "employee_id"])
    ]
)
class TaskAssignee(

    @ManyToOne(fetch = FetchType.LAZY) var task: Task,

    @Column(nullable = false) var employeeId: Long

) : BaseEntity()
