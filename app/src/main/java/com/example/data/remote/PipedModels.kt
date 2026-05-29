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
data class PipedStreamResponse(
    @Json(name = "title") val title: String,
    @Json(name = "uploader") val uploader: String,
    @Json(name = "uploaderUrl") val uploaderUrl: String? = null,
    @Json(name = "uploaderAvatar") val uploaderAvatar: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "duration") val duration: Int,
    @Json(name = "audioStreams") val audioStreams: List<PipedAudioStream>,
    @Json(name = "videoStreams") val videoStreams: List<PipedVideoStream>? = null
)

@JsonClass(generateAdapter = true)
data class PipedAudioStream(
    @Json(name = "url") val url: String,
    @Json(name = "format") val format: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "codec") val codec: String? = null,
    @Json(name = "bitrate") val bitrate: Int? = null
)

@JsonClass(generateAdapter = true)
data class PipedVideoStream(
    @Json(name = "url") val url: String,
    @Json(name = "format") val format: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null
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
