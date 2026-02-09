package uz.task

import java.security.Principal
import kotlin.jvm.java


const val USER_ID_KEY = "uid"
const val USERNAME_KEY = "sub"

const val ORG_ID_KEY = "oid"
const val EMPLOYEE_ID_KEY = "eid"
const val EMPLOYEE_POS_KEY = "per"

const val ROLE_KEY = "rol"
const val ROLE_LEVEL_KEY = "roll"
const val DEV_ROLE = "DEV"
const val USER_DETAILS_HEADER_KEY = "X-User-Details"
const val ACTIVE = "ACTIVE"
val PRINCIPAL_KEY: String = Principal::class.java.name