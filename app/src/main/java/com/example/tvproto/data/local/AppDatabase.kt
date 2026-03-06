package com.example.tvproto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Show::class, Episode::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
}