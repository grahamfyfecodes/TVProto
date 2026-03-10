package com.example.tvproto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tvproto.data.local.model.TrackedShowInfo
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
    var sortMode by remember { mutableStateOf("alpha") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val sortedShows = remember(trackedShows, sortMode) {
        when (sortMode) {
            "progress" -> trackedShows.sortedBy {
                if (it.totalCount > 0) it.watchedCount.toFloat() / it.totalCount else 0f
            }
            "status" -> trackedShows.sortedBy { if (it.show.status == "Running") 0 else 1 }
            else -> trackedShows.sortedBy { it.show.name.lowercase() }
        }
    }

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
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Alphabetical") },
                                onClick = { sortMode = "alpha"; sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Progress") },
                                onClick = { sortMode = "progress"; sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Running first") },
                                onClick = { sortMode = "status"; sortMenuExpanded = false }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (sortedShows.isEmpty()) {
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
fun TrackedShowCard(
    info: TrackedShowInfo,
    onClick: () -> Unit,
    onUntrack: () -> Unit,
    onMarkAllWatched: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val channel = info.show.networkName
        ?: info.show.webChannelName
        ?: ""

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
            if (info.show.imageUrl != null) {
                AsyncImage(
                    model = info.show.imageUrl,
                    contentDescription = info.show.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(56.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = info.show.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Untrack") },
                                onClick = { menuExpanded = false; onUntrack() }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark all watched") },
                                onClick = { menuExpanded = false; onMarkAllWatched() }
                            )
                            DropdownMenuItem(
                                text = { Text("Notifications") },
                                enabled = false,
                                onClick = { }
                            )
                        }
                    }
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