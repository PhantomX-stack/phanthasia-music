package com.phantasia.music

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.phantasia.music.ui.*

sealed class Route(val path: String) {
    object Home         : Route("home")
    object Search       : Route("search")
    object Stats        : Route("stats")        // NEW — between Home and Search
    object Library      : Route("library")
    object Settings     : Route("settings")
    object Accounts     : Route("accounts")
    object Queue        : Route("queue")
    object Downloads    : Route("downloads")
    object GoogleLogin  : Route("google_login")
    object SpotifyLogin : Route("spotify_login")
    object YtmLogin     : Route("ytm_login")
    object Player       : Route("player/{videoId}") {
        fun build(id: String) = "player/$id"
    }
    object ImportedPlaylist : Route("imported/{id}/{name}") {
        fun build(id: String, name: String) =
            "imported/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
    }
}

private data class NavItem(val route: Route, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation(innerPadding: PaddingValues) {
    // !! FIX for Issue 8: use a SINGLE rememberNavController at top level
    // and pass it to ALL screens. Never create NavController inside a screen.
    val nav = rememberNavController()

    val playerVm: PlayerViewModel = hiltViewModel()
    val playerState by playerVm.uiState.collectAsState()

    // Bottom nav tabs — Stats is between Home and Search (Issue 3)
    val tabs = listOf(
        NavItem(Route.Home,    "Home",    Icons.Filled.Home),
        NavItem(Route.Stats,   "Stats",   Icons.Filled.BarChart),   // NEW position
        NavItem(Route.Search,  "Search",  Icons.Filled.Search),
        NavItem(Route.Library, "Library", Icons.Filled.LibraryMusic),
        NavItem(Route.Settings,"Settings",Icons.Filled.Settings),
    )

    val backstackEntry by nav.currentBackStackEntryAsState()
    val currentRoute   = backstackEntry?.destination?.route

    // Show bottom bar only on tab screens — NOT on player, queue, settings sub-pages etc
    val tabPaths    = tabs.map { it.route.path }.toSet()
    val isOnTabScreen = tabPaths.contains(currentRoute)
    val isPlaying   = playerState is PlayerUiState.Playing

    Scaffold(
        modifier            = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
            ))
        ),
        containerColor      = PhantasiaColors.Midnight,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            // Only show bottom bar on tab screens
            if (isOnTabScreen) {
                Column {
                    // Mini player bar slides in above nav bar when Playing
                    AnimatedVisibility(
                        visible = isPlaying,
                        enter   = slideInVertically { it } + fadeIn(),
                        exit    = slideOutVertically { it } + fadeOut()
                    ) {
                        (playerState as? PlayerUiState.Playing)?.let { playing ->
                            MiniPlayerBar(
                                state   = playing,
                                onEvent = playerVm::onEvent,
                                onClick = {
                                    nav.navigate(Route.Player.build(playing.track.videoId)) {
                                        // Don't pop back stack when opening player
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = androidx.compose.ui.unit.Dp(0f)
                    ) {
                        tabs.forEach { item ->
                            val selected = backstackEntry?.destination
                                ?.hierarchy?.any { it.route == item.route.path } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick  = {
                                    // FIX Issue 8: always pop to start destination and restore state
                                    // This ensures search doesn't "lock" navigation
                                    nav.navigate(item.route.path) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                },
                                icon  = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label,
                                    style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor   = PhantasiaColors.Primary,
                                    selectedTextColor   = PhantasiaColors.Primary,
                                    unselectedIconColor = PhantasiaColors.OnDim,
                                    unselectedTextColor = PhantasiaColors.OnDim,
                                    indicatorColor      = PhantasiaColors.PrimaryDim.copy(alpha = 0.25f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController    = nav,
            startDestination = Route.Home.path,
            modifier         = Modifier.padding(scaffoldPadding),
            // Smooth transitions between screens
            enterTransition  = {
        fadeIn(animationSpec = tween(220)) +
        slideInHorizontally(animationSpec = tween(220)) { (it * 0.05f).toInt() }
    },
    exitTransition   = {
        fadeOut(animationSpec = tween(180)) +
        slideOutHorizontally(animationSpec = tween(180)) { -(it * 0.05f).toInt() }
    },
    popEnterTransition  = {
        fadeIn(animationSpec = tween(220)) +
        slideInHorizontally(animationSpec = tween(220)) { -(it * 0.05f).toInt() }
    },
    popExitTransition   = {
        fadeOut(animationSpec = tween(180)) +
        slideOutHorizontally(animationSpec = tween(180)) { (it * 0.05f).toInt() }
    }
        ) {
            composable(Route.Home.path)    { HomeScreen(nav) }
            composable(Route.Search.path)  { SearchScreen(nav) }
            composable(Route.Stats.path)   { StatsScreen(nav) }
            composable(Route.Library.path) { LibraryScreen(nav) }
            composable(Route.Settings.path){ SettingsScreen(nav) }
            composable(Route.Accounts.path){ AccountsScreen(nav) }
            composable(Route.Queue.path)   { QueueScreen(nav) }
            composable(Route.Downloads.path) { DownloadScreen(nav) }
            composable(Route.GoogleLogin.path)  { GoogleLoginScreen(nav) }
            composable(Route.SpotifyLogin.path) { SpotifyLoginScreen(nav) }
            composable(Route.YtmLogin.path)     { YtmLoginScreen(nav) }
            composable(
                route     = Route.Player.path,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
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
                val id   = back.arguments?.getString("id") ?: return@composable
                val name = java.net.URLDecoder.decode(
                    back.arguments?.getString("name") ?: "", "UTF-8")
                ImportedPlaylistScreen(id, name, nav)
            }
        }
    }
}
