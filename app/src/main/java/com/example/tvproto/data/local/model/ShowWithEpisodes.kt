package com.example.tvproto.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

data class ShowWithEpisodes (
    @Embedded val show: Show,
    @Relation(
        parentColumn = "id",
        entityColumn = "showId"
    )
    val episodes: List<Episode>
)