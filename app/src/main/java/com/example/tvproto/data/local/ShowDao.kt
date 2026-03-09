package com.example.tvproto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tvproto.data.local.model.Episode
import com.example.tvproto.data.local.model.Show
import com.example.tvproto.data.local.model.ShowWithEpisodes

@Dao
interface ShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: Show)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Query("SELECT * FROM shows WHERE tracked = 1")
    suspend fun getAllShows(): List<Show>

    @Query("SELECT * FROM shows WHERE id = :showId")
    suspend fun getShowById(showId: Int): Show?

    @Query("SELECT * FROM shows WHERE id = :showId and tracked = 1")
    suspend fun getShowWithEpisodes(showId: Int): ShowWithEpisodes

    @Query("SELECT * FROM shows WHERE status = 'Running' and tracked = 1")
    suspend fun getRunningShows(): List<Show>

    @Query("UPDATE episodes SET watched = :watched WHERE id = :episodeId")
    suspend fun setEpisodeWatched(episodeId: Int, watched: Boolean)

    @Query("UPDATE shows SET tracked = :tracked WHERE id = :showId")
    suspend fun setShowTracked(showId: Int, tracked: Boolean)
}