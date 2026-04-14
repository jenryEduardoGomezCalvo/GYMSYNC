package com.AppexSolutions.gymsync.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentIsoTimestamp(): String {
    return ZonedDateTime.now(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatToIsoTimestamp(dateString: String): String? {
    if (dateString.isBlank()) return null
    return try {
        val localDate = java.time.LocalDate.parse(dateString)
        localDate.atStartOfDay(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
    } catch (e: Exception) {
        null
    }
}
