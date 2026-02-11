package uz.zero.file

import java.security.Principal

const val SYSTEM_NAME = "OPEN_BANKING"
const val BASE_API_VERSION = "/api/v1"
const val USER_ID_KEY = "uid"

const val ORG_ID_KEY = "oid"
const val EMPLOYEE_ID_KEY = "eid"
const val EMPLOYEE_POS_KEY = "per"

const val USERNAME_KEY = "sub"
const val ROLE_KEY = "rol"
const val ROLE_LEVEL_KEY = "roll"
const val DEV_ROLE = "DEV"
const val USER_DETAILS_HEADER_KEY = "X-User-Details"
const val ACTIVE = "ACTIVE"
val PRINCIPAL_KEY: String = Principal::class.java.name