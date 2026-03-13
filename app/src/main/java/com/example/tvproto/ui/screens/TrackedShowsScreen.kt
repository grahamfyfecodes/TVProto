package com.example.tvproto.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tvproto.data.local.model.TrackedShowInfo
import com.example.tvproto.ui.components.ShowImage
import com.example.tvproto.ui.components.ShowImageSize
import com.example.tvproto.viewmodel.ShowViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedShowsScreen(
    viewModel: ShowViewModel,
    onShowClick: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val trackedShows by viewModel.trackedShows.collectAsState()
    val scope = rememberCoroutineScope()

    // Saves the sort mode preference + whether the sort mode is expanded
    var sortMode by remember { mutableStateOf("alpha") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val sortedShows = remember(trackedShows, sortMode) {
        when (sortMode) {
            // Least progress first
            "progress" -> trackedShows.sortedBy {
                if (it.totalCount > 0) it.watchedCount.toFloat() / it.totalCount else 0f
            }
            // Running Shows first
            "status" -> trackedShows.sortedBy { if (it.show.status == "Running") 0 else 1 }
            // Default Alphabetical
            else -> trackedShows.sortedBy { it.show.name.lowercase() }
        }
    }

    // Tracked shows loaded on screen open
    LaunchedEffect(Unit) {
        viewModel.loadTrackedShows()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shows") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    SortMenu(
                        expanded = sortMenuExpanded,
                        onExpandedChange = { sortMenuExpanded = it },
                        onSortSelected = { sortMode = it }
                    )
                })
        }
    ) { padding ->
        if (sortedShows.isEmpty()) {
            ShowNotTracked(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedShows) { info ->
                    TrackedShowCard(
                        info = info,
                        onClick = { onShowClick(info.show.id) },
                        onUntrack = {
                            viewModel.untrackShow(info.show.id)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${info.show.name} untracked",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.retrackShow(info.show.id)
                                }
                            }
                        },
                        onMarkAllWatched = { viewModel.markAllWatched(info.show.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowNotTracked(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No tracked shows yet.\nSearch and add some!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SortMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSortSelected: (String) -> Unit
) {
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(Icons.Default.FilterList, contentDescription = "Sort")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Alphabetical") },
                onClick = { onSortSelected("alpha"); onExpandedChange(false) }
            )
            DropdownMenuItem(
                text = { Text("Progress") },
                onClick = { onSortSelected("progress"); onExpandedChange(false) }
            )
            DropdownMenuItem(
                text = { Text("Running first") },
                onClick = { onSortSelected("status"); onExpandedChange(false) }
            )
        }
    }
}

@Composable
fun TrackedShowCard(
    info: TrackedShowInfo,
    onClick: () -> Unit,
    onUntrack: () -> Unit,
    onMarkAllWatched: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val channel = info.show.displayChannel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            ShowImage(
                imageUrl = info.show.imageUrl,
                contentDescription = info.show.name,
                size = ShowImageSize.Medium
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = info.show.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    ShowActions(
                        menuExpanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it },
                        onUntrack = onUntrack,
                        onMarkAllWatched = onMarkAllWatched
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${info.watchedCount}/${info.totalCount} watched",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    info.show.status?.let {
                        Text(
                            "· $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (channel.isNotEmpty()) {
                    Text(
                        text = channel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val progress = if (info.totalCount > 0)
                    info.watchedCount.toFloat() / info.totalCount else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun ShowActions(
    menuExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUntrack: () -> Unit,
    onMarkAllWatched: () -> Unit
) {
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Untrack") },
                onClick = { onExpandedChange(false); onUntrack() }
            )
            DropdownMenuItem(
                text = { Text("Mark all watched") },
                onClick = { onExpandedChange(false); onMarkAllWatched() }
            )
            DropdownMenuItem(
                text = { Text("Notifications") },
                enabled = false,
                onClick = { }
            )
        }
    }
}