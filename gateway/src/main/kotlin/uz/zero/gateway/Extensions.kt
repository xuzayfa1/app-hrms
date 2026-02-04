package uz.zero.gateway

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPOutputStream

fun ObjectMapper.parseMap(data: String?): Map<String, Any?> {
    try {
        data ?: return emptyMap()
        return this.readValue(data, object : TypeReference<MutableMap<String, Any?>>() {})
    } catch (ex: Exception) {
        throw IllegalArgumentException(ex.message, ex)
    }
}

fun String.extractServiceName(): String? {
    val regex = Regex("^/api/v\\d+/([^/]+)")
    val match = regex.find(this)
    return match?.groupValues?.get(1)
}


fun <T> Class<T>.getIsRoutedKey(): String {
    return "${this.name}.entered"
}

fun String.compress(): String {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(this) }
    return Base64.getEncoder().encodeToString(bos.toByteArray())
}
