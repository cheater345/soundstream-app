package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PipedApiService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "videos"
    ): PipedSearchResponse
}
