package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PipedSearchResponse(
    @Json(name = "items") val items: List<PipedSearchItem>,
    @Json(name = "nextpage") val nextpage: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedSearchItem(
    @Json(name = "url") val url: String,
    @Json(name = "title") val title: String,
    @Json(name = "uploaderName") val uploaderName: String,
    @Json(name = "uploaderUrl") val uploaderUrl: String? = null,
    @Json(name = "uploaderAvatar") val uploaderAvatar: String? = null,
    @Json(name = "thumbnail") val thumbnail: String,
    @Json(name = "duration") val duration: Int,
    @Json(name = "views") val views: Long = 0,
    @Json(name = "uploaded") val uploaded: Long? = null,
    @Json(name = "isShort") val isShort: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PipedTrendingResponse(
    @Json(name = "items") val items: List<PipedTrendingItem>
)

@JsonClass(generateAdapter = true)
data class PipedTrendingItem(
    @Json(name = "url") val url: String,
    @Json(name = "title") val title: String,
    @Json(name = "uploaderName") val uploaderName: String,
    @Json(name = "thumbnail") val thumbnail: String,
    @Json(name = "duration") val duration: Int,
    @Json(name = "views") val views: Long = 0,
    @Json(name = "uploaded") val uploaded: Long? = null,
    @Json(name = "isShort") val isShort: Boolean = false
)
