package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SongEntity
import com.example.ui.components.SongListItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.UiState
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val trendingState by viewModel.trendingSongsState.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progressMap by viewModel.downloadProgressMap.collectAsState()

    // Greeting based on hours
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Genre items
    val genres = listOf("All", "Electronic", "Hip Hop", "Rock", "Pop", "Classical")

    // Custom playlists list for carousel
    val featuredPlaylists = remember {
        listOf(
            FeaturedPlaylist("Chill Beats", "Relaxing modern beats for any mood", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=70", "Electronic"),
            FeaturedPlaylist("Acoustic Sessions", "Warm, direct acoustic vibes", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&q=70", "Rock"),
            FeaturedPlaylist("Fresh Dance", "High energy dance records", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=70", "Pop"),
            FeaturedPlaylist("Late Study", "Keep focus with ambient textures", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400&q=70", "Classical")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpotifyCardGray.copy(alpha = 0.5f),
                        SpotifyBlack
                    ),
                    endY = 500f
                )
            ),
        contentPadding = PaddingValues(bottom = 100.dp) // Avoid overlap with mini player
    ) {
        // Welcoming Header Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Row {
                    IconButton(onClick = { viewModel.loadTrendingSongs() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Horizontal Genre Filter Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    val isSelected = selectedGenre == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectGenre(genre) },
                        label = { Text(genre) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SpotifyCardGray,
                            labelColor = SpotifyLightGray,
                            selectedContainerColor = ElegantPurple,
                            selectedLabelColor = Color.White
                        ),
                        border = null,
                        shape = RoundedCornerShape(100.3.dp)
                    )
                }
            }
        }

        // Horizontal Scroll: Featured Playlists (Only on "All" genre to keep look clean)
        if (selectedGenre == "All") {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Editor's Picks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(featuredPlaylists) { playlist ->
                        PlaylistCarouselCard(
                            playlist = playlist,
                            onClick = {
                                viewModel.selectGenre(playlist.genreTag)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Header section for Trending
        item {
            Text(
                text = if (selectedGenre == "All") "Trending Streams" else "Trending - $selectedGenre",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Core dynamic Trending List
        when (val state = trendingState) {
            is UiState.Loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ElegantPurple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tuning into open networks...",
                            color = SpotifyLightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadTrendingSongs() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple)
                        ) {
                            Text("Retry Streams Connection", color = Color.White)
                        }
                    }
                }
            }
            is UiState.Success -> {
                val songs = state.data
                if (songs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tracks available in this genre.", color = SpotifyLightGray)
                        }
                    }
                } else {
                    items(songs) { song ->
                        SongListItem(
                            song = song,
                            isCurrent = currentSong?.id == song.id,
                            isPlaying = isPlaying,
                            downloadProgress = progressMap[song.id],
                            onPlayClick = { viewModel.playSong(song, songs) },
                            onFavoriteClick = { viewModel.toggleFavorite(song) },
                            onDownloadClick = {
                                if (song.isDownloaded) {
                                    viewModel.removeDownload(song)
                                } else {
                                    viewModel.downloadSong(song)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

data class FeaturedPlaylist(
    val title: String,
    val description: String,
    val imageUrl: String,
    val genreTag: String
)

@Composable
fun PlaylistCarouselCard(
    playlist: FeaturedPlaylist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(148.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SpotifyCardGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(playlist.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playlist.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = playlist.description,
                color = SpotifyLightGray,
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}
