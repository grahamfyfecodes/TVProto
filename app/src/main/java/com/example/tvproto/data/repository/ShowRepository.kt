package com.example.tvproto.data.repository

import com.example.tvproto.util.Constants.UPCOMING_DAYS_AHEAD
import com.example.tvproto.data.local.model.Episode
import com.example.tvproto.data.local.model.Show
import com.example.tvproto.data.local.ShowDao
import com.example.tvproto.data.local.model.ShowWithEpisodes
import com.example.tvproto.data.local.model.TrackedShowInfo
import com.example.tvproto.data.local.model.UpcomingScheduleEntry
import com.example.tvproto.data.remote.TvMazeShow
import com.example.tvproto.data.remote.TvMazeService
import java.time.DayOfWeek
import java.time.LocalDate.now

class ShowRepository(
    private val dao: ShowDao,
    private val api: TvMazeService
) {

    suspend fun searchShows(query: String): List<TvMazeShow> {
        val results = api.searchShows(query)
        return results.map { it.show }
    }

    suspend fun trackShow(show: TvMazeShow) {
        // Check for soft deleted shows to avoid overwrite
        val existing = dao.getShowById(show.id)
        if (existing != null) {
            dao.setShowTracked(show.id, true)
            return
        }

        val localShow = Show(
            id = show.id,
            name = show.name,
            imageUrl = show.image?.medium,
            status = show.status,
            networkName = show.network?.name,
            webChannelName = show.webChannel?.name,
            scheduleTime = show.schedule?.time,
            scheduleDays = show.schedule?.days
                ?.filter { it.isNotEmpty() }
                ?.map { DayOfWeek.valueOf(it.uppercase()) }
                ?.ifEmpty { null }
        )
        dao.insertShow(localShow)
        fetchAndSaveEpisodes(show.id)
    }

    suspend fun fetchAndSaveEpisodes(showId: Int): List<Episode> {
        val results = api.getEpisodes(showId)
        val episodes = results
            .filter { it.season != null && it.number != null }
            .map { ep ->
                Episode(
                    id = ep.id,
                    showId = showId,
                    season = ep.season ?: 0,
                    number = ep.number ?: 0,
                    name = ep.name,
                    airdate = ep.airdate,
                    airtime = ep.airtime
                )
            }
        dao.insertEpisodes(episodes)
        return episodes
    }

    suspend fun getShowWithEpisodes(showId: Int): ShowWithEpisodes = dao.getShowWithEpisodes(showId)

    suspend fun setEpisodeWatched(episodeId: Int, watched: Boolean) =
        dao.setEpisodeWatched(episodeId, watched)

    suspend fun getScheduleBasedUpcoming(): List<UpcomingScheduleEntry> {
        val today = now()
        val dates = (0L..UPCOMING_DAYS_AHEAD.toLong()).map { today.plusDays(it) }

        fun getUpcomingForShow(show: Show): List<UpcomingScheduleEntry> =
            dates
                .filter { date -> show.scheduleDays?.contains(date.dayOfWeek) == true }
                .map { date ->
                UpcomingScheduleEntry(
                    show = show,
                    date = date.toString(),
                    time = show.scheduleTime
                )
            }

        return dao.getRunningShows()
            .flatMap { show ->
                getUpcomingForShow(show)
            }
            .sortedWith(compareBy({ it.date }, { it.time }))
    }

    suspend fun untrackShow(showId: Int) {
        dao.setShowTracked(showId, false)
    }

    suspend fun retrackShow(showId: Int) {
        dao.setShowTracked(showId, true)
    }

    suspend fun getTrackedShowsWithProgress(): List<TrackedShowInfo> {
        return dao.getAllShows().map { show ->
            TrackedShowInfo(
                show = show,
                watchedCount = dao.getWatchedCount(show.id),
                totalCount = dao.getEpisodeCount(show.id)
            )
        }
    }
}