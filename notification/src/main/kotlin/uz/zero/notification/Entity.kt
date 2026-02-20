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
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
    @Column(nullable = false)
    var userId: Long,

    @Column(nullable = false)
    var chatId: Long,

    @Column(nullable = false)
    var active: Boolean = true,

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
class Notification(
    @Column(nullable = false)
    var userId: Long,

    @Column(nullable = false)
    var organizationName: String,

    @Column(nullable = false)
    var projectName: String,

    @Column(nullable = false)
    var ownerName: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var title: String,

    @Column(nullable = false)
    var oldState: String,

    var newState: String? = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    var error: String? = null,

    var actionDate: Date? = null,



) : BaseEntity(){
    fun getFormattedDate(): String {
        if (actionDate == null) return "Unknown"
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return formatter.format(actionDate)
    }
}

