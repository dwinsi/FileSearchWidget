package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.example.filesearchwidget.R
import com.example.filesearchwidget.model.MediaFile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FileItem(
    file: MediaFile,
    onClick: (MediaFile) -> Unit,
    compact: Boolean = false // <- New parameter
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    fun formatFileSize(sizeBytes: Long?): String {
        if (sizeBytes == null || sizeBytes <= 0) return "Unknown size"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$sizeBytes bytes"
        }
    }

    val imageSize = if (compact) 96.dp else 64.dp
    val padding = if (compact) 4.dp else 8.dp

    Card(
        modifier = Modifier
            .padding(padding)
            .clickable { onClick(file) }
            .then(
                if (compact) Modifier.fillMaxWidth() else Modifier.fillMaxWidth()
            ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        if (compact) {
            // Grid mode - vertical layout
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = file.thumbnailUri ?: file.uri,
                    contentDescription = file.displayName,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .align(alignment = androidx.compose.ui.Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_file_placeholder),
                    error = painterResource(R.drawable.ic_file_placeholder)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = file.displayName ?: "Unnamed File",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // List mode - horizontal layout
            Row(modifier = Modifier.padding(16.dp)) {
                AsyncImage(
                    model = file.thumbnailUri ?: file.uri,
                    contentDescription = file.displayName,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_file_placeholder),
                    error = painterResource(R.drawable.ic_file_placeholder)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.displayName ?: "Unnamed File",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = file.mimeType ?: "Unknown Type",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = formatFileSize(file.sizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = file.createdDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Unknown date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}