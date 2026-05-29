package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PIPED_BASE_URL = "https://api.piped.private.coffee/"
    private const val PROXY_BASE_URL = "https://f4fb27c8ecc6bb.lhr.life/"
    private const val TAG = "ApiClient"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
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
        val request = Request.Builder()
            .url("${PROXY_BASE_URL}stream?videoId=$videoId")
            .build()
        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty proxy response")
        val json = moshi.adapter(Map::class.java).fromJson(body) as? Map<*, *>
        val url = json?.get("url") as? String
            ?: throw Exception("No URL in proxy response: ${json?.get("error")}")
        return url
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
