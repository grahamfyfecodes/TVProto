package com.example.tvproto.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tvproto.data.local.model.UpcomingScheduleEntry
import com.example.tvproto.ui.components.ShowImage
import com.example.tvproto.ui.components.ShowImageSize.Small
import com.example.tvproto.viewmodel.ShowViewModel
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(viewModel: ShowViewModel, onShowClick: (Int) -> Unit) {
    val upcoming by viewModel.upcomingEntries.collectAsState()

    // Load upcoming shows everytime screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadUpcoming()
    }

    val today = remember { now() }
    val tomorrow = today.plusDays(1)

    // Sorted by date
    // Grouped by day
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
                        // Parse the date to use as label
                        // Default to using direct string if the parse fails
                        val label = try {
                            when (val parsed = LocalDate.parse(date)) {
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
    val channel = entry.show.displayChannel

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
            ShowImage(
                imageUrl = entry.show.imageUrl,
                contentDescription = entry.show.name,
                size = Small
            )
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