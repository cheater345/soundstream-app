package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchResultsState.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progressMap by viewModel.downloadProgressMap.collectAsState()

    val browseCategories = remember {
        listOf(
            BrowseCategory("Electronic / Dance", Brush.linearGradient(listOf(Color(0xFF8C1DFF), Color(0xFF1D5BFF)))),
            BrowseCategory("Hip Hop / Rap", Brush.linearGradient(listOf(Color(0xFFFF9F1C), Color(0xFFFF401C)))),
            BrowseCategory("Rock & Metal", Brush.linearGradient(listOf(Color(0xFFE01A4F), Color(0xFF50007A)))),
            BrowseCategory("Pop Essentials", Brush.linearGradient(listOf(Color(0xFF33FFA9), Color(0xFF27995C)))),
            BrowseCategory("Jazz & Blues", Brush.linearGradient(listOf(Color(0xFF8B4513), Color(0xFFCD853F)))),
            BrowseCategory("Classical Masterpieces", Brush.linearGradient(listOf(Color(0xFF5C5C5C), Color(0xFF2B2B2B)))),
            BrowseCategory("Fresh Chill out", Brush.linearGradient(listOf(Color(0xFFDF5A14), Color(0xFFE9B815)))),
            BrowseCategory("Country Spirit", Brush.linearGradient(listOf(Color(0xFF2E6B15), Color(0xFF114212))))
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        // App Sticky Heading
        Text(
            text = "Search",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Custom Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchSongs(it) },
            placeholder = { Text("What do you want to listen to?", color = SpotifyLightGray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SpotifyLightGray) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchSongs("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SpotifyCardGray,
                unfocusedContainerColor = SpotifyCardGray,
                focusedBorderColor = ElegantPurple,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_text_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic State Content
        if (query.trim().isEmpty()) {
            // Display Browse Grid
            Text(
                text = "Browse all",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(browseCategories) { category ->
                    BrowseGridCard(category) {
                        viewModel.searchSongs(category.title.split(" ")[0])
                    }
                }
            }
        } else {
            // Display search queries outputs
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = searchState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ElegantPurple)
                        }
                    }
                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Search failed: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is UiState.Success -> {
                        val results = state.data
                        if (results.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No results found for \"$query\"",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Make sure words are spelled correctly or search different music artists.",
                                    color = SpotifyLightGray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(results) { song ->
                                    SongListItem(
                                        song = song,
                                        isCurrent = currentSong?.id == song.id,
                                        isPlaying = isPlaying,
                                        downloadProgress = progressMap[song.id],
                                        onPlayClick = { viewModel.playSong(song, results) },
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
        }
    }
}

data class BrowseCategory(
    val title: String,
    val backgroundBrush: Brush
)

@Composable
fun BrowseGridCard(
    category: BrowseCategory,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(category.backgroundBrush)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = category.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
