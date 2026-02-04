package uz.zero.auth.entities

import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import uz.zero.auth.enums.Role
import uz.zero.auth.enums.UserStatus

//
@Entity(name = "users")
class User(
//    var fullName: String,
    @Column(length = 32, unique = true)
    var username: String,
    @Column(length = 255)
    var password: String,
    var role: Role,
    @Enumerated(STRING)
    @Column(length = 32)
    var status: UserStatus = UserStatus.ACTIVE
) : BaseEntity()