package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PIPED_BASE_URL = "https://pipedapi.kavin.rocks/"
    private const val TAG = "ApiClient"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val pipedApi: PipedApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PIPED_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PipedApiService::class.java)
    }

    suspend fun getStreamUrl(videoId: String): String {
        val response = pipedApi.getStreams(videoId)
        return response.audioStreams
            .maxByOrNull { it.bitrate ?: 0 }
            ?.url ?: throw Exception("No audio streams found for $videoId")
    }

    fun extractVideoId(url: String): String {
        return if (url.startsWith("/watch?v=")) {
            url.removePrefix("/watch?v=")
        } else if (url.startsWith("https://")) {
            url.substringAfter("v=").substringBefore("&").take(11)
        } else {
            url.take(11)
        }
    }
}
