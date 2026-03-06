package com.example.tvproto.data.remote

data class ShowSearchResult(
    val score: Double,
    val show: TvMazeShow
)

data class TvMazeShow(
    val id: Int,
    val name: String,
    val image: TvMazeImage?,
    val status: String?
)

data class TvMazeImage(
    val medium: String?,
    val original: String?
)

data class TvMazeEpisode(
    val id: Int,
    val season: Int?,
    val number: Int?,
    val name: String,
    val airdate: String?
)