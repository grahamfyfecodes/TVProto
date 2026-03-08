package com.example.tvproto.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

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
    val lastUpdated: Long = System.currentTimeMillis()
)