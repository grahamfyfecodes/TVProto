package com.example.tvproto.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Entity(tableName = "shows")
data class Show(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String?,
    val status: String?,
    val networkName: String?,
    val webChannelName: String?,
    val scheduleTime: String?,
    val scheduleDays: List<DayOfWeek>?,
    val lastUpdated: Long = System.currentTimeMillis(),
    val tracked: Boolean = true
) {
    // Channel prioritises network -> web channel -> blank
    val displayChannel: String
        get() = networkName ?: webChannelName ?: ""
}