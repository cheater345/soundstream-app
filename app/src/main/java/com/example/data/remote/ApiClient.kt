package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PIPED_BASE_URL = "https://api.piped.private.coffee/"
    private const val PROXY_CONFIG_URL = "https://raw.githubusercontent.com/cheater345/soundstream-proxy/main/proxy-url.txt"
    private const val PROXY_BASE_URL = "https://2d363e8d8213ec2d-143-44-225-117.serveousercontent.com/"
    private const val TAG = "ApiClient"

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var proxyBaseUrl: String = PROXY_BASE_URL

    val pipedApi: PipedApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PIPED_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PipedApiService::class.java)
    }

    suspend fun refreshProxyUrl() {
        try {
            val request = Request.Builder()
                .url(PROXY_CONFIG_URL)
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val url = response.body?.string()?.trim()?.trimEnd('/')
                if (!url.isNullOrEmpty()) {
                    proxyBaseUrl = "$url/"
                    android.util.Log.d(TAG, "Proxy URL updated: $proxyBaseUrl")
                }
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Failed to fetch proxy URL config", e)
        }
    }

    suspend fun getStreamUrl(videoId: String): String {
        val request = Request.Builder()
            .url("${proxyBaseUrl}stream?videoId=$videoId")
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Proxy returned ${response.code}")
        }
        val body = response.body?.string() ?: throw Exception("Empty proxy response")
        if (body.contains("\"error\"")) {
            val msgStart = body.indexOf("\"error\"") + 8
            val msgValStart = body.indexOf('"', msgStart) + 1
            val msgValEnd = body.indexOf('"', msgValStart)
            throw Exception(body.substring(msgValStart, msgValEnd))
        }
        val urlKey = "\"url\""
        val urlStart = body.indexOf(urlKey)
        if (urlStart == -1) throw Exception("No URL in proxy response")
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
