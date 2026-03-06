package com.example.tvproto.data.repository

import com.example.tvproto.data.local.Episode
import com.example.tvproto.data.local.Show
import com.example.tvproto.data.local.ShowDao
import com.example.tvproto.data.remote.TvMazeShow
import com.example.tvproto.data.remote.TvMazeService

class ShowRepository(
    private val dao: ShowDao,
    private val api: TvMazeService
) {

    suspend fun searchShows(query: String): List<TvMazeShow> {
        val results = api.searchShows(query)
        return results.map { it.show }
    }

    suspend fun trackShow(show: TvMazeShow) {
        val localShow = Show(
            id = show.id,
            name = show.name,
            imageUrl = show.image?.medium,
            status = show.status
        )
        dao.insertShow(localShow)
        fetchAndSaveEpisodes(show.id)
    }

    suspend fun fetchAndSaveEpisodes(showId: Int): List<Episode> {
        val results = api.getEpisodes(showId)
        val episodes = results.map { ep ->
            Episode(
                id = ep.id,
                showId = showId,
                season = ep.season ?: 0,
                number = ep.number ?: 0,
                name = ep.name,
                airdate = ep.airdate
            )
        }
        dao.insertEpisodes(episodes)
        return episodes
    }

    suspend fun getSavedShows(): List<Show> = dao.getAllShows()

    suspend fun getEpisodesForShow(showId: Int): List<Episode> = dao.getEpisodesForShow(showId)
}