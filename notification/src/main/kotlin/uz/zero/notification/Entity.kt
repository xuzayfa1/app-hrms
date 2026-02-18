package uz.zero.notification

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.Date


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
class TelegramUser(
    @Column(nullable = false, unique = true)
    var userId: Long,

    @Column(nullable = false, unique = true)
    var chatId: Long,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false)
    var linkedAt: Instant = Instant.now(),

    @Column(columnDefinition = "TEXT")
    var lastError: String? = null
) : BaseEntity()


@Entity
class TelegramLinkToken(
    @Column(nullable = false)
    var userId: Long,

    @Column(nullable = false, unique = true, length = 128)
    var hashId: String,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var used: Boolean = false
) : BaseEntity()




@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_status_created", columnList = "status, createdDate")
    ]
)
class Notification(
    @Column(nullable = false)
    var employeeId: Long,

    @Column(nullable = false)
    var organizationName: String,

    @Column(nullable = false)
    var projectName: String,

    @Column(nullable = false)
    var ownerName: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var title: String,

    var oldStatus: String? = null,

    var newStatus: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    var error: String? = null,

    var sentAt: Date? = null,
) : BaseEntity()

