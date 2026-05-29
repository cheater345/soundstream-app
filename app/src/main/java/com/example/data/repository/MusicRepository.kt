package com.example.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.local.SongDao
import com.example.data.local.SongEntity
import com.example.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MusicRepository(
    private val context: Context,
    private val songDao: SongDao
) {
    private val pipedApi = ApiClient.pipedApi
    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val favoriteSongs: Flow<List<SongEntity>> = songDao.getFavoriteSongsFlow()
    val downloadedSongs: Flow<List<SongEntity>> = songDao.getDownloadedSongsFlow()
    val allSavedSongs: Flow<List<SongEntity>> = songDao.getAllSongsFlow()

    private fun mapPipedItem(id: String, title: String, uploader: String, duration: Int, thumbnail: String): SongEntity {
        return SongEntity(
            id = id,
            title = title,
            artist = uploader,
            duration = duration,
            artworkUrl = thumbnail,
            isFavorite = false,
            isDownloaded = false,
            localPath = null,
            downloadedTime = 0L
        )
    }

    suspend fun getTrendingSongs(genre: String? = null): List<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val query = if (genre != null) "trending $genre music" else "trending music"
            val response = pipedApi.search(query = query, filter = "videos")
            response.items
                .filter { !it.isShort }
                .take(30)
                .map { item ->
                    val id = ApiClient.extractVideoId(item.url)
                    mapPipedItem(id, item.title, item.uploaderName, item.duration, item.thumbnail)
                }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error loading trending songs", e)
            val cached = songDao.getAllSongsFlow().firstOrNull() ?: emptyList()
            cached
        }
    }

    suspend fun searchSongs(query: String): List<SongEntity> = withContext(Dispatchers.IO) {
        if (query.trim().isEmpty()) return@withContext emptyList()
        val response = pipedApi.search(query = query, filter = "videos")
        response.items
            .filter { !it.isShort }
            .take(40)
            .map { item ->
                val id = ApiClient.extractVideoId(item.url)
                mapPipedItem(id, item.title, item.uploaderName, item.duration, item.thumbnail)
            }
    }

    suspend fun toggleFavorite(song: SongEntity) = withContext(Dispatchers.IO) {
        val existing = songDao.getSongById(song.id)
        val updatedSong = if (existing != null) {
            existing.copy(isFavorite = !existing.isFavorite)
        } else {
            song.copy(isFavorite = true)
        }

        if (!updatedSong.isFavorite && !updatedSong.isDownloaded) {
            songDao.deleteSong(updatedSong)
        } else {
            songDao.insertOrUpdateSong(updatedSong)
        }
    }

    suspend fun deleteSong(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.deleteSong(song)
    }

    suspend fun downloadSong(
        song: SongEntity,
        onProgress: (Float) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val streamUrl = ApiClient.getStreamUrl(song.id)
            val request = Request.Builder().url(streamUrl).build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Failed to download file: $response")
            }

            val body = response.body ?: throw IOException("Empty response body")
            val contentType = body.contentType()?.toString() ?: ""
            Log.d("MusicRepository", "Downloading: content provider type is $contentType")

            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
            val downloadsFolder = File(storageDir, "SoundStreamDownloads")
            if (!downloadsFolder.exists()) {
                downloadsFolder.mkdirs()
            }

            val targetFile = File(downloadsFolder, "track_${song.id}.mp3")

            val totalBytes = body.contentLength()
            var bytesWritten = 0L

            body.byteStream().use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        if (totalBytes > 0) {
                            val progress = bytesWritten.toFloat() / totalBytes.toFloat()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            val existing = songDao.getSongById(song.id)
            val updated = if (existing != null) {
                existing.copy(
                    isDownloaded = true,
                    localPath = targetFile.absolutePath,
                    downloadedTime = System.currentTimeMillis()
                )
            } else {
                song.copy(
                    isDownloaded = true,
                    localPath = targetFile.absolutePath,
                    downloadedTime = System.currentTimeMillis()
                )
            }
            songDao.insertOrUpdateSong(updated)

            withContext(Dispatchers.Main) {
                onSuccess(targetFile.absolutePath)
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Download failed for song ${song.id}", e)
            withContext(Dispatchers.Main) {
                onFailure(e)
            }
        }
    }

    suspend fun removeDownload(song: SongEntity) = withContext(Dispatchers.IO) {
        song.localPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }

        val existing = songDao.getSongById(song.id)
        if (existing != null) {
            val updated = existing.copy(
                isDownloaded = false,
                localPath = null,
                downloadedTime = 0L
            )
            if (!updated.isFavorite) {
                songDao.deleteSong(updated)
            } else {
                songDao.insertOrUpdateSong(updated)
            }
        }
    }
}
