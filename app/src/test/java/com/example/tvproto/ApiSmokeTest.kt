package com.example.tvproto

import com.example.tvproto.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ApiSmokeTest {

    private val api = RetrofitClient.tvMazeService

    @Test
    fun searchShows() = runBlocking {
        val results = api.searchShows("tracker")
        println("=== SEARCH RESULTS ===")
        results.forEach { result ->
            println("${result.show.name} (id: ${result.show.id})")
            println("  status: ${result.show.status}")
            println("  image: ${result.show.image?.medium}")
            println()
        }
    }

    @Test
    fun fetchEpisodes() = runBlocking {
        val episodes = api.getEpisodes(73565)
        println("=== EPISODES (first 5) ===")
        episodes.take(5).forEach { ep ->
            println("S${ep.season}E${ep.number} - ${ep.name}")
            println("  airdate: ${ep.airdate}")
            println()
        }
    }

    @Test
    fun fetchShowWithNextEpisode() = runBlocking {
        // This one we can't test yet - we haven't added
        // the nextepisode embed endpoint to our service.
        // Placeholder for when we do.
        println("=== TODO: nextepisode embed ===")
    }
}