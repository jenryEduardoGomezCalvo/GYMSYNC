package com.AppexSolutions.gymsync.core.datastore

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAnnouncementType(type: AnnouncementType): String = type.name

    @TypeConverter
    fun toAnnouncementType(value: String): AnnouncementType =
        runCatching { AnnouncementType.valueOf(value) }.getOrDefault(AnnouncementType.GENERAL)
}
