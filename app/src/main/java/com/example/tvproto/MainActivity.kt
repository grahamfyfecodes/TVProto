package com.example.tvproto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.room.Room
import com.example.tvproto.data.local.AppDatabase
import com.example.tvproto.data.remote.RetrofitClient
import com.example.tvproto.data.repository.ShowRepository
import com.example.tvproto.ui.navigation.AppNavigation
import com.example.tvproto.ui.screens.SearchScreen
import com.example.tvproto.ui.theme.TVProtoTheme
import com.example.tvproto.viewmodel.ShowViewModel
import com.example.tvproto.viewmodel.ShowViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "showtrack-db").build()
    }

    private val repository by lazy {
        ShowRepository(database.showDao(), RetrofitClient.tvMazeService)
    }

    private val viewModel: ShowViewModel by viewModels {
        ShowViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TVProtoTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}