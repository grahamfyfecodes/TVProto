package com.example.tvproto.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek

class Converters {
    @TypeConverter
    fun fromDayList(value: List<DayOfWeek>?): String? =
        value?.joinToString(",") { it.name }

    @TypeConverter
    fun toDayList(value: String?): List<DayOfWeek>? =
        value?.takeIf { it.isNotEmpty() }?.split(",")?.map { DayOfWeek.valueOf(it) }
}