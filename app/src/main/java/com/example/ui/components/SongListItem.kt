package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SongEntity
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: SongEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    downloadProgress: Float?,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Album Cover with overlay
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SpotifyCardGray)
        ) {
            if (!song.artworkUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.artworkUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "No Artwork",
                        tint = SpotifyLightGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Pulsing shade overlay if currently playing
            if (isCurrent && isPlaying) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ElegantPurple.copy(alpha = alpha)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Artist Column
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = song.title,
                color = if (isCurrent) ElegantPurple else Color.White,
                fontSize = 15.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (isCurrent) Modifier.basicMarquee() else Modifier
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = song.artist,
                color = SpotifyLightGray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Download status / action button
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                downloadProgress != null -> {
                    // Downloading State
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        color = ElegantPurple,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(18.dp)
                    )
                }
                song.isDownloaded -> {
                    // Downloaded State
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.testTag("download_indicator_loaded_${song.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Downloaded Offline",
                            tint = ElegantPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                else -> {
                    // Regular Downloadable Action
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.testTag("download_button_${song.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleDown,
                            contentDescription = "Download track",
                            tint = SpotifyLightGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Favorite Button
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .size(36.dp)
                .testTag("favorite_button_${song.id}")
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (song.isFavorite) "Remove Favorite" else "Add Favorite",
                tint = if (song.isFavorite) ElegantPurple else SpotifyLightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
