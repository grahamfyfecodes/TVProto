package com.example.tvproto.data.remote

import com.example.tvproto.Constants
import com.example.tvproto.Constants.TVMAZE_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(TVMAZE_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val tvMazeService: TvMazeService = retrofit.create(TvMazeService::class.java)
}