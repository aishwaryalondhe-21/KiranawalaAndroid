package com.kiranawala.data.local.typeconverter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime

class DateTypeConverter {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
}