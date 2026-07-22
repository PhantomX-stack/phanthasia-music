package com.phantasia.music

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phantasia.music.ui.*

sealed class Route(val path: String) {
    object Home    : Route("home")
    object Search  : Route("search")
    object Library : Route("library")
    object Player  : Route("player/{videoId}") {
        fun build(id: String) = "player/$id"
    }
}

data class NavItem(val route: Route, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation(innerPadding: PaddingValues) {
    val nav = rememberNavController()
    val navItems = listOf(
        NavItem(Route.Home,    "Home",    Icons.Filled.Home),
        NavItem(Route.Search,  "Search",  Icons.Filled.Search),
        NavItem(Route.Library, "Library", Icons.Filled.LibraryMusic),
    )
    val backstackEntry by nav.currentBackStackEntryAsState()
    val currentDest   = backstackEntry?.destination
    val showBottomBar = navItems.any { it.route.path == currentDest?.route }

    Scaffold(
        modifier      = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp(0f)
                ) {
                    navItems.forEach { item ->
                        val selected = currentDest?.hierarchy?.any { it.route == item.route.path } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                nav.navigate(item.route.path) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon     = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label    = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors   = NavigationBarItemDefaults.colors(
                                selectedIconColor   = MaterialTheme.colorScheme.primary,
                                selectedTextColor   = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor      = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController    = nav,
            startDestination = Route.Home.path,
            modifier         = Modifier.padding(scaffoldPadding)
        ) {
            composable(Route.Home.path)    { HomeScreen(nav) }
            composable(Route.Search.path)  { SearchScreen(nav) }
            composable(Route.Library.path) { LibraryScreen(nav) }
            composable(
                route     = Route.Player.path,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) { back ->
                val videoId = back.arguments?.getString("videoId") ?: return@composable
                PlayerScreen(videoId, nav)
            }
        }
    }
}
