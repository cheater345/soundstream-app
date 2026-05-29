package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SongEntity
import com.example.ui.components.SongListItem
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGray
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyLightGray
import com.example.ui.theme.ElegantPurple
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun FavoritesScreen(
    viewModel: MusicViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progressMap by viewModel.downloadProgressMap.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Grand Dynamic Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF450AF5), // Sophisticated deep indigo purple gradient
                                SpotifyBlack
                            )
                        )
                    )
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ElegantPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Liked Songs",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${favoriteSongs.size} tracks",
                        fontSize = 13.sp,
                        color = SpotifyLightGray
                    )
                }
            }
        }

        // Action Play Row
        if (favoriteSongs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Playlist",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Large Round Refresh / Play Bar
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = {
                                if (favoriteSongs.isNotEmpty()) {
                                    val shuffled = favoriteSongs.shuffled()
                                    viewModel.playSong(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier.testTag("shuffle_play_favorites")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                 tint = ElegantPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (favoriteSongs.isNotEmpty()) {
                                    viewModel.playSong(favoriteSongs.first(), favoriteSongs)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("play_all_favorites")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play all",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Item listings
        if (favoriteSongs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Songs you like will appear here",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Save songs from trending streams or searches by tapping the heart icon.",
                        color = SpotifyLightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("discover_favorite_redirect")
                    ) {
                        Text("Discover Music", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(favoriteSongs) { song ->
                SongListItem(
                    song = song,
                    isCurrent = currentSong?.id == song.id,
                    isPlaying = isPlaying,
                    downloadProgress = progressMap[song.id],
                    onPlayClick = { viewModel.playSong(song, favoriteSongs) },
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
