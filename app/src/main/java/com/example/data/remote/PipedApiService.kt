package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PipedApiService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "music_songs"
    ): PipedSearchResponse

    @GET("streams/{videoId}")
    suspend fun getStreams(
        @Path("videoId") videoId: String
    ): PipedStreamResponse

    @GET("feed/trending")
    suspend fun getTrending(
        @Query("region") region: String = "US"
    ): PipedTrendingResponse
}
