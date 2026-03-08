package com.example.tvproto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tvproto.data.local.model.Episode
import com.example.tvproto.data.local.model.Show

@Database(entities = [Show::class, Episode::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
}