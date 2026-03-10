package com.example.tvproto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tvproto.data.local.model.UpcomingScheduleEntry
import com.example.tvproto.viewmodel.ShowViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(viewModel: ShowViewModel, onShowClick: (Int) -> Unit) {
    val upcoming by viewModel.upcomingEntries.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUpcoming()
    }

    val today = remember { LocalDate.now() }
    val tomorrow = remember { today.plusDays(1) }

    val grouped = remember(upcoming) {
        upcoming
            .sortedBy { it.date }
            .groupBy { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (upcoming.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nothing coming up. Try tracking more shows!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (date, entries) ->
                    item {
                        val label = try {
                            val parsed = LocalDate.parse(date)
                            when (parsed) {
                                today -> "Today"
                                tomorrow -> "Tomorrow"
                                else -> parsed.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                            }
                        } catch (_: Exception) {
                            date
                        }

                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(entries) { entry ->
                        UpcomingCard(
                            entry = entry,
                            onClick = { onShowClick(entry.show.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingCard(entry: UpcomingScheduleEntry, onClick: () -> Unit) {
    val channel = entry.show.networkName
        ?: entry.show.webChannelName
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.show.imageUrl != null) {
                AsyncImage(
                    model = entry.show.imageUrl,
                    contentDescription = entry.show.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(48.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.show.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (channel.isNotEmpty()) {
                    Text(
                        text = channel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = entry.time ?: "TBA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}