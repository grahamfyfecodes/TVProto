package com.example.tvproto.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey val id: Int,
    val showId: Int,
    val season: Int,
    val number: Int,
    val name: String,
    val airdate: String?,
    val airtime: String?,
    val watched: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)