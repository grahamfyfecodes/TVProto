package com.example.tvproto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows")
data class Show(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String?,
    val status: String?
)