package com.example.tvproto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvproto.data.local.model.Episode
import com.example.tvproto.data.local.model.Show
import com.example.tvproto.data.local.model.ShowWithEpisodes
import com.example.tvproto.data.local.model.UpcomingScheduleEntry
import com.example.tvproto.data.remote.TvMazeShow
import com.example.tvproto.data.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShowViewModel(private val repository: ShowRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<TvMazeShow>>(emptyList())
    val searchResults: StateFlow<List<TvMazeShow>> = _searchResults

    private val _trackedShows = MutableStateFlow<List<Show>>(emptyList())
    val trackedShows: StateFlow<List<Show>> = _trackedShows

    private val _upcomingEntries = MutableStateFlow<List<UpcomingScheduleEntry>>(emptyList())
    val upcomingEntries: StateFlow<List<UpcomingScheduleEntry>> = _upcomingEntries

    private val _selectedShow = MutableStateFlow<ShowWithEpisodes?>(null)
    val selectedShow: StateFlow<ShowWithEpisodes?> = _selectedShow

    init {
        viewModelScope.launch {
            _trackedShows.value = repository.getSavedShows()
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchShows(query)
        }
    }

    fun trackShow(show: TvMazeShow) {
        viewModelScope.launch {
            repository.trackShow(show)
            _trackedShows.value = repository.getSavedShows()
        }
    }

    fun loadShow(showId: Int) {
        viewModelScope.launch {
            _selectedShow.value = repository.getShowWithEpisodes(showId)
        }
    }

    fun toggleWatched(episode: Episode) {
        viewModelScope.launch {
            repository.setEpisodeWatched(episode.id, !episode.watched)
            _selectedShow.value?.let { loadShow(it.show.id) }
        }
    }

    fun loadUpcoming() {
        viewModelScope.launch {
            _upcomingEntries.value = repository.getScheduleBasedUpcoming()
        }
    }
}