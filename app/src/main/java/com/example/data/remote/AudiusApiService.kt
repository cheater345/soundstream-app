package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AudiusApiService {

    @GET("v1/tracks/trending")
    suspend fun getTrendingTracks(
        @Query("app_name") appName: String = "SoundStream",
        @Query("genre") genre: String? = null,
        @Query("limit") limit: Int = 30
    ): TrackResponse

    @GET("v1/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "SoundStream",
        @Query("limit") limit: Int = 40
    ): TrackResponse
}
