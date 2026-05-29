package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGray
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyLightGray
import com.example.ui.theme.ElegantPurple
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: MusicViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    var isPlayerExpanded by remember { mutableStateOf(false) }
    val currentSong by viewModel.currentSong.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpotifyBlack,
        bottomBar = {
            Column {
                // Mini Player docked right above Navigation
                if (currentSong != null) {
                    MiniPlayer(
                        viewModel = viewModel,
                        onExpandClick = { isPlayerExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 1.dp)
                    )
                }

                // Standard Premium Navigation Bar
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                NavigationBar(
                    containerColor = Color.Black.copy(alpha = 0.9f),
                    tonalElevation = 0.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantPurple,
                            selectedTextColor = ElegantPurple,
                            unselectedIconColor = SpotifyLightGray,
                            unselectedTextColor = SpotifyLightGray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "search",
                        onClick = {
                            if (currentRoute != "search") {
                                navController.navigate("search") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantPurple,
                            selectedTextColor = ElegantPurple,
                            unselectedIconColor = SpotifyLightGray,
                            unselectedTextColor = SpotifyLightGray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_search")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "downloads",
                        onClick = {
                            if (currentRoute != "downloads") {
                                navController.navigate("downloads") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.CloudDownload, contentDescription = "Downloads") },
                        label = { Text("Downloads") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantPurple,
                            selectedTextColor = ElegantPurple,
                            unselectedIconColor = SpotifyLightGray,
                            unselectedTextColor = SpotifyLightGray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_downloads")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "favorites",
                        onClick = {
                            if (currentRoute != "favorites") {
                                navController.navigate("favorites") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantPurple,
                            selectedTextColor = ElegantPurple,
                            unselectedIconColor = SpotifyLightGray,
                            unselectedTextColor = SpotifyLightGray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_favorites")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Screen Navigation Host
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("home") {
                    HomeScreen(viewModel = viewModel)
                }
                composable("search") {
                    SearchScreen(viewModel = viewModel)
                }
                composable("downloads") {
                    DownloadsScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { navController.navigate("search") }
                    )
                }
                composable("favorites") {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { navController.navigate("search") }
                    )
                }
            }

            // Explicited Full screen expand overlay player
            FullPlayerScreen(
                viewModel = viewModel,
                onCollapseClick = { isPlayerExpanded = false },
                visible = isPlayerExpanded
            )
        }
    }
}
