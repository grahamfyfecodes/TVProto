package com.example.tvproto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: Show)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Query("SELECT * FROM shows")
    suspend fun getAllShows(): List<Show>

    @Query("SELECT * FROM episodes WHERE showId = :showId ORDER BY season, number")
    suspend fun getEpisodesForShow(showId: Int): List<Episode>
}