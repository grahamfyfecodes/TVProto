package com.example.tvproto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvproto.data.local.Show
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
}