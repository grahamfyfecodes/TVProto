package com.example.tvproto.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
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
import com.example.tvproto.ui.screens.ShowDetailScreen
import com.example.tvproto.ui.screens.TrackedShowsScreen
import com.example.tvproto.ui.screens.UpcomingScreen
import com.example.tvproto.util.NetworkMonitor
import com.example.tvproto.viewmodel.ShowViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Tracked : Screen("tracked", "My Shows", Icons.AutoMirrored.Filled.List)
    object Upcoming : Screen("upcoming", "Upcoming", Icons.Default.DateRange)

    object ShowDetail : Screen("show/{showId}", "Detail", Icons.AutoMirrored.Filled.List) {
        fun createRoute(showId: Int) = "show/$showId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: ShowViewModel, networkMonitor: NetworkMonitor) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Dashboard, Screen.Search, Screen.Tracked, Screen.Upcoming)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isOnline by networkMonitor.isOnline.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                    saveState = false
                                }
                                launchSingleTop = true
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
                SearchScreen(viewModel = viewModel, isOnline = isOnline)
            }
            composable(Screen.Tracked.route) {
                TrackedShowsScreen(
                    viewModel = viewModel,
                    onShowClick = { showId ->
                        navController.navigate(Screen.ShowDetail.createRoute(showId))
                    },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(Screen.Upcoming.route) {
                UpcomingScreen(viewModel = viewModel)
            }
            composable("show/{showId}") { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId")?.toIntOrNull()
                showId?.let {
                    ShowDetailScreen(
                        viewModel = viewModel,
                        showId = it,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}