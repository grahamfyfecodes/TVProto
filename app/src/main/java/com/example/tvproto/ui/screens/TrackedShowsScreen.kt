package com.example.tvproto.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tvproto.data.local.model.TrackedShowInfo
import com.example.tvproto.viewmodel.ShowViewModel
import kotlinx.coroutines.launch

@Composable
fun TrackedShowsScreen(
    viewModel: ShowViewModel,
    onShowClick: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val trackedShows by viewModel.trackedShows.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadTrackedShows()
    }

    if (trackedShows.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No tracked shows yet.\nSearch and add some!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trackedShows) { info ->
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
                    }
                )
            }
        }
    }
}

@Composable
fun TrackedShowCard(info: TrackedShowInfo, onClick: () -> Unit, onUntrack: () -> Unit) {
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
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.show.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${info.watchedCount}/${info.totalCount} watched",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    info.show.status?.let {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = it,
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
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Untrack") },
                        onClick = {
                            menuExpanded = false
                            onUntrack()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Mark all watched") },
                        enabled = false,
                        onClick = { }
                    )
                    DropdownMenuItem(
                        text = { Text("Notifications") },
                        enabled = false,
                        onClick = { }
                    )
                }
            }
        }
    }
}