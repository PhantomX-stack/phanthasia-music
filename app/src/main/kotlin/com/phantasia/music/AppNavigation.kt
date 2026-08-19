package com.phantasia.music

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.phantasia.music.ui.*
import androidx.compose.ui.graphics.graphicsLayer

// ── All routes ────────────────────────────────────────────────────────────────
sealed class Route(val path: String) {
    object Home            : Route("home")
    object Search          : Route("search?q={q}") {
        const val BASE = "search"
        fun build(query: String = "") =
            if (query.isBlank()) "search?q="
            else "search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
    }
    object Library         : Route("library")
    object Settings        : Route("settings")
    // Sub-pages — NOT in bottom nav
    object Stats           : Route("stats")
    object Queue           : Route("queue")
    object Accounts        : Route("accounts")
    object Downloads       : Route("downloads")
    object GoogleLogin     : Route("google_login")
    object SpotifyLogin    : Route("spotify_login")
    object YtmLogin        : Route("ytm_login")
    object Player          : Route("player/{videoId}") {
        fun build(id: String) = "player/$id"
    }
    object ImportedPlaylist: Route("imported/{id}/{name}") {
        fun build(id: String, name: String) =
            "imported/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
    }
}

private data class TabItem(val route: Route, val label: String, val icon: ImageVector)

// All 5 bottom tabs
private val TABS = listOf(
    TabItem(Route.Home,     "Home",     Icons.Filled.Home),
    TabItem(Route.Search,   "Search",   Icons.Filled.Search),
    TabItem(Route.Stats,    "Stats",    Icons.Filled.BarChart),
    TabItem(Route.Library,  "Library",  Icons.Filled.LibraryMusic),
    TabItem(Route.Settings, "Settings", Icons.Filled.Settings),
)

// Paths where bottom nav is visible
private val TAB_PREFIXES = listOf("home", "search", "stats", "library", "settings")

@Composable
fun AppNavigation(innerPadding: PaddingValues) {
    val nav         = rememberNavController()
    val playerVm: PlayerViewModel = hiltViewModel()
    val playerState by playerVm.uiState.collectAsState()

    val backstackEntry by nav.currentBackStackEntryAsState()
    val currentRoute   = backstackEntry?.destination?.route
    val showBottomBar  = TAB_PREFIXES.any { currentRoute?.startsWith(it) == true }
    val isPlaying      = playerState is PlayerUiState.Playing

    Scaffold(
        modifier            = Modifier.fillMaxSize(),
        containerColor      = PhantasiaColors.Midnight,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Mini player — Samsung Now Bar style floating above nav bar
                    AnimatedVisibility(
                        visible = isPlaying,
                        enter   = slideInVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) { it } + fadeIn(tween(250)),
                        exit    = slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { it } + fadeOut(tween(200))
                    ) {
                        (playerState as? PlayerUiState.Playing)?.let { playing ->
                            MiniPlayerBar(
                                state   = playing,
                                onEvent = playerVm::onEvent,
                                onClick = {
                                    nav.navigate(Route.Player.build(playing.track.videoId)) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    // Bottom nav bar
                    NavigationBar(
                        containerColor = Color(0xF50B0E17),
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TABS.forEach { tab ->
                            val selected = when (tab.route) {
                                Route.Search -> currentRoute?.startsWith("search") == true
                                else -> backstackEntry?.destination?.hierarchy?.any { it.route == tab.route.path } == true
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick  = {
                                    val destination = if (tab.route == Route.Search) Route.Search.build("") else tab.route.path
                                    nav.navigate(destination) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                },
                                icon  = {
                                    val scale by animateFloatAsState(
                                        targetValue   = if (selected) 1.15f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness    = Spring.StiffnessLow
                                        ),
                                        label = "nav_scale"
                                    )
                                    Icon(tab.icon, tab.label,
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = scale; scaleY = scale
                                        })
                                },
                                label  = {
                                    Text(tab.label, style = MaterialTheme.typography.labelSmall)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor   = PhantasiaColors.Primary,
                                    selectedTextColor   = PhantasiaColors.Primary,
                                    unselectedIconColor = PhantasiaColors.OnDim,
                                    unselectedTextColor = PhantasiaColors.OnDim,
                                    indicatorColor      = PhantasiaColors.PrimaryDim.copy(alpha = 0.22f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController       = nav,
            startDestination    = Route.Home.path,
            modifier            = Modifier.padding(scaffoldPadding),
            enterTransition     = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { (it * 0.04f).toInt() } },
            exitTransition      = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -(it * 0.04f).toInt() } },
            popEnterTransition  = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -(it * 0.04f).toInt() } },
            popExitTransition   = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { (it * 0.04f).toInt() } },
        ) {
            composable(Route.Home.path)         { HomeScreen(nav) }
            composable(
                route     = Route.Search.path,
                arguments = listOf(navArgument("q") { type = NavType.StringType; defaultValue = "" })
            ) { SearchScreen(nav) }
            composable(Route.Search.BASE)       { SearchScreen(nav) }
            composable(Route.Library.path)       { LibraryScreen(nav) }
            composable(Route.Settings.path)      { SettingsScreen(nav) }
            composable(Route.Stats.path)         { StatsScreen(nav) }
            composable(Route.Queue.path)         { QueueScreen(nav) }
            composable(Route.Accounts.path)      { AccountsScreen(nav) }
            composable(Route.Downloads.path)     { DownloadScreen(nav) }
            composable(Route.GoogleLogin.path)   { GoogleLoginScreen(nav) }
            composable(Route.SpotifyLogin.path)  { SpotifyLoginScreen(nav) }
            composable(Route.YtmLogin.path)      { YtmLoginScreen(nav) }
            composable(
                route     = Route.Player.path,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(250))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(200))
                },
                popEnterTransition = {
                    fadeIn(tween(200))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(220))
                }
            ) { back ->
                val videoId = back.arguments?.getString("videoId") ?: return@composable
                PlayerScreen(videoId, nav)
            }
            composable(
                route     = Route.ImportedPlaylist.path,
                arguments = listOf(
                    navArgument("id")   { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { back ->
                val id   = back.arguments?.getString("id")   ?: return@composable
                val name = java.net.URLDecoder.decode(
                    back.arguments?.getString("name") ?: "", "UTF-8")
                ImportedPlaylistScreen(id, name, nav)
            }
        }
    }
}
