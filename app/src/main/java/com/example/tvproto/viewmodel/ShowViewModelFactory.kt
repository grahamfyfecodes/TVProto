package com.example.tvproto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tvproto.data.repository.ShowRepository

class ShowViewModelFactory(
    private val repository: ShowRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShowViewModel(repository) as T
    }
}