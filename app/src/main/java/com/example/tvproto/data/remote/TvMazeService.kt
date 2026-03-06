package com.example.tvproto.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TvMazeService {

    @GET("search/shows")
    suspend fun searchShows(@Query("q") query: String): List<ShowSearchResult>

    @GET("shows/{id}/episodes")
    suspend fun getEpisodes(@Path("id") showId: Int): List<TvMazeEpisode>
}