package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PIPED_BASE_URL = "https://api.piped.private.coffee/"
    private const val PROXY_BASE_URL = "https://f4fb27c8ecc6bb.lhr.life/"
    private const val TAG = "ApiClient"

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val pipedApi: PipedApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PIPED_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PipedApiService::class.java)
    }

    suspend fun getStreamUrl(videoId: String): String {
        val request = Request.Builder()
            .url("${PROXY_BASE_URL}stream?videoId=$videoId")
            .build()
        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty proxy response")
        val urlKey = "\"url\""
        val urlStart = body.indexOf(urlKey)
        if (urlStart == -1) throw Exception("No URL in proxy response: $body")
        val valueStart = body.indexOf('"', urlStart + urlKey.length + 1) + 1
        val valueEnd = body.indexOf('"', valueStart)
        return body.substring(valueStart, valueEnd)
    }

    fun extractVideoId(url: String): String {
        return if (url.startsWith("/watch?v=")) {
            url.removePrefix("/watch?v=")
        } else if (url.contains("v=")) {
            url.substringAfter("v=").substringBefore("&").take(11)
        } else {
            url.take(11)
        }
    }
}
