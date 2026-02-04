package uz.zero.file

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
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
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

// todo
/**
 * file meta data saqlash,
 * folder path saqla
 * hashId generatsitya
 * hash bilan malumot olish,
 *
 *
 * */

@Entity
class FileEntity(
    @Column(nullable = false, unique = true, length = 11)
    var hashId: String,

    @Column(nullable = false)
    var folderPath: String,

    @Column(nullable = false)
    var filename: String,

    @Column(nullable = false)
    var type: String,

    @Column(nullable = false)
    var originalName: String,

    @Column(nullable = false)
    var size: Long,

    @Column(nullable = false)
    var orderNumber: Long
): BaseEntity()
