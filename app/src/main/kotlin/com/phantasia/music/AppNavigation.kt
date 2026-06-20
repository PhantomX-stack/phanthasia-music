package com.phantasia.music

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phantasia.music.ui.HomeScreen
import com.phantasia.music.ui.LibraryScreen
import com.phantasia.music.ui.PlayerScreen
import com.phantasia.music.ui.SearchScreen

sealed class Route(val path: String) {
    object Home    : Route("home")
    object Search  : Route("search")
    object Library : Route("library")
    object Player  : Route("player/{itemId}") {
        fun create(itemId: String) = "player/$itemId"
    }
}

@Composable
fun AppNavigation(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) {
            HomeScreen(onNavigate = navController::navigate, padding = innerPadding)
        }
        composable(Route.Search.path) {
            SearchScreen(onNavigate = navController::navigate, padding = innerPadding)
        }
        composable(Route.Library.path) {
            LibraryScreen(onNavigate = navController::navigate, padding = innerPadding)
        }
        composable(
            route = Route.Player.path,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            PlayerScreen(itemId = itemId, padding = innerPadding)
        }
    }
}
