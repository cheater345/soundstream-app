package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SongEntity
import com.example.data.repository.MusicRepository
import com.example.playback.PlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MusicRepository(application, database.songDao())

    // UI state flows
    private val _trendingSongsState = MutableStateFlow<UiState<List<SongEntity>>>(UiState.Loading)
    val trendingSongsState: StateFlow<UiState<List<SongEntity>>> = _trendingSongsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultsState = MutableStateFlow<UiState<List<SongEntity>>>(UiState.Success(emptyList()))
    val searchResultsState: StateFlow<UiState<List<SongEntity>>> = _searchResultsState.asStateFlow()

    private val _selectedGenre = MutableStateFlow("All")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    // Download progress tracking: Song ID -> Progress (0.0 to 1.0)
    private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, Float>> = _downloadProgressMap.asStateFlow()

    // Playback state proxies (Synced from standard PlaybackManager single source of truth)
    val currentSong: StateFlow<SongEntity?> = PlaybackManager.currentSong
    val isPlaying: StateFlow<Boolean> = PlaybackManager.isPlaying
    val playlist: StateFlow<List<SongEntity>> = PlaybackManager.playlist
    val currentPosition: StateFlow<Long> = PlaybackManager.currentPosition
    val duration: StateFlow<Long> = PlaybackManager.duration
    val isLoadingAudio: StateFlow<Boolean> = PlaybackManager.isLoading

    // Offline collections observed continuously from Room DB
    val favoriteSongs: StateFlow<List<SongEntity>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<SongEntity>> = repository.downloadedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTrendingSongs()
    }

    fun selectGenre(genre: String) {
        _selectedGenre.value = genre
        loadTrendingSongs()
    }

    fun loadTrendingSongs() {
        viewModelScope.launch {
            _trendingSongsState.value = UiState.Loading
            try {
                val genreQuery = if (_selectedGenre.value == "All") null else _selectedGenre.value.lowercase()
                val songs = repository.getTrendingSongs(genreQuery)
                _trendingSongsState.value = UiState.Success(songs)
            } catch (e: Exception) {
                _trendingSongsState.value = UiState.Error(e.localizedMessage ?: "Failed to load trending music")
            }
        }
    }

    fun searchSongs(query: String) {
        _searchQuery.value = query
        if (query.trim().isEmpty()) {
            _searchResultsState.value = UiState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _searchResultsState.value = UiState.Loading
            try {
                val results = repository.searchSongs(query)
                _searchResultsState.value = UiState.Success(results)
            } catch (e: Exception) {
                _searchResultsState.value = UiState.Error(e.localizedMessage ?: "Find tracks failed")
            }
        }
    }

    // Playback events delegated directly to PlaybackManager
    fun playSong(song: SongEntity, tracklist: List<SongEntity>) {
        PlaybackManager.playSong(getApplication(), song, tracklist)
    }

    fun togglePlayPause() {
        PlaybackManager.togglePlayPause(getApplication())
    }

    fun seekTo(position: Long) {
        PlaybackManager.seekTo(position)
    }

    fun playNext() {
        PlaybackManager.playNext(getApplication())
    }

    fun playPrevious() {
        PlaybackManager.playPrevious(getApplication())
    }

    // Favorites modification
    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
            // Trigger refresh on trending/search lists to sync favorite icon state
            if (_trendingSongsState.value is UiState.Success) {
                val list = (_trendingSongsState.value as UiState.Success<List<SongEntity>>).data
                val updated = list.map { 
                    if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it 
                }
                _trendingSongsState.value = UiState.Success(updated)
            }
            if (_searchResultsState.value is UiState.Success) {
                val list = (_searchResultsState.value as UiState.Success<List<SongEntity>>).data
                val updated = list.map { 
                    if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it 
                }
                _searchResultsState.value = UiState.Success(updated)
            }
        }
    }

    // Downloads management
    fun downloadSong(song: SongEntity) {
        if (_downloadProgressMap.value.containsKey(song.id)) {
            Toast.makeText(getApplication(), "Already downloading track", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgressMap.update { it + (song.id to 0.0f) }
            repository.downloadSong(
                song = song,
                onProgress = { progress ->
                    _downloadProgressMap.update { it + (song.id to progress) }
                },
                onSuccess = { path ->
                    _downloadProgressMap.update { it - song.id }
                    // Update lists local states to show green download checkmark
                    updateDownloadStateInLists(song.id, isDownloaded = true, path = path)
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Downloaded ${song.title}", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { err ->
                    _downloadProgressMap.update { it - song.id }
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Download failed: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    fun removeDownload(song: SongEntity) {
        viewModelScope.launch {
            repository.removeDownload(song)
            updateDownloadStateInLists(song.id, isDownloaded = false, path = null)
            Toast.makeText(getApplication(), "Removed download", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDownloadStateInLists(songId: String, isDownloaded: Boolean, path: String?) {
        viewModelScope.launch {
            if (_trendingSongsState.value is UiState.Success) {
                val list = (_trendingSongsState.value as UiState.Success<List<SongEntity>>).data
                val updated = list.map { 
                    if (it.id == songId) it.copy(isDownloaded = isDownloaded, localPath = path) else it 
                }
                _trendingSongsState.value = UiState.Success(updated)
            }
            if (_searchResultsState.value is UiState.Success) {
                val list = (_searchResultsState.value as UiState.Success<List<SongEntity>>).data
                val updated = list.map { 
                    if (it.id == songId) it.copy(isDownloaded = isDownloaded, localPath = path) else it 
                }
                _searchResultsState.value = UiState.Success(updated)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Note: keeping state ongoing for background play, so release player selectively
    }
}
