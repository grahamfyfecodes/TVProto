package com.example.tvproto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tvproto.ui.components.ShowImageSize.*

// Different image sizes used in different screens
enum class ShowImageSize(val width: Dp, val height: Dp, val iconSize: Dp) {
    Small(48.dp, 68.dp, 20.dp),
    Medium(56.dp, 80.dp, 20.dp),
    Large(100.dp, 140.dp, 36.dp)
}

// Shared Composable for show image icon
@Composable
fun ShowImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String,
    size: ShowImageSize = Medium
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .width(size.width)
                .height(size.height)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        Box(
            modifier = modifier
                .width(size.width)
                .height(size.height)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size.iconSize)
            )
        }
    }
}