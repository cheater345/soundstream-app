package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrackResponse(
    @Json(name = "data") val data: List<TrackDto>
)

@JsonClass(generateAdapter = true)
data class TrackDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "duration") val duration: Int, // duration in seconds
    @Json(name = "description") val description: String? = null,
    @Json(name = "artwork") val artwork: ArtworkDto? = null,
    @Json(name = "user") val user: UserDto? = null,
    @Json(name = "genre") val genre: String? = null
)

@JsonClass(generateAdapter = true)
data class ArtworkDto(
    @Json(name = "150x150") val size150: String? = null,
    @Json(name = "480x480") val size480: String? = null,
    @Json(name = "1000x1000") val size1000: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "name") val name: String,
    @Json(name = "handle") val handle: String,
    @Json(name = "profile_picture") val profilePicture: ProfilePictureDto? = null
)

@JsonClass(generateAdapter = true)
data class ProfilePictureDto(
    @Json(name = "150x150") val size150: String? = null
)
