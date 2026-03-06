package com.example.tvproto.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tvproto.ui.screens.SearchScreen
import com.example.tvproto.ui.screens.TrackedShowsScreen
import com.example.tvproto.viewmodel.ShowViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Tracked : Screen("tracked", "My Shows", Icons.Default.List)
    object Upcoming : Screen("upcoming", "Upcoming", Icons.Default.DateRange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: ShowViewModel) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Dashboard, Screen.Search, Screen.Tracked, Screen.Upcoming)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                Text("Dashboard - Coming Soon")
            }
            composable(Screen.Search.route) {
                SearchScreen(viewModel = viewModel)
            }
            composable(Screen.Tracked.route) {
                TrackedShowsScreen(viewModel = viewModel)
            }
            composable(Screen.Upcoming.route) {
                Text("Upcoming - Coming Soon")
            }
        }
    }
}