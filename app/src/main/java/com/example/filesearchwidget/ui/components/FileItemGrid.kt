package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.filesearchwidget.R
import com.example.filesearchwidget.model.MediaFile
import androidx.compose.ui.res.painterResource
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FileItemGrid(
    file: MediaFile,
    onClick: (MediaFile) -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    fun formatFileSize(sizeBytes: Long?): String {
        if (sizeBytes == null || sizeBytes <= 0) return ""
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$sizeBytes bytes"
        }
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(file) }
    ) {
        AsyncImage(
            model = file.thumbnailUri ?: file.uri,
            contentDescription = file.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.ic_file_placeholder),
            error = painterResource(R.drawable.ic_file_placeholder)
        )

        // Date at top-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = file.createdDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Unknown Date",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Size at bottom-start
        val sizeText = formatFileSize(file.sizeBytes)
        if (sizeText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = sizeText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}