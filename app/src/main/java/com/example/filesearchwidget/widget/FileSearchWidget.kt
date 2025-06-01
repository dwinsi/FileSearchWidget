package com.example.filesearchwidget.widget

import android.content.Context
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider
import com.example.filesearchwidget.SearchActivity


object FileSearchWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(modifier = GlanceModifier.padding(30.dp)) {
//                Text(
//                    text = "Search Files",
//                    style = TextStyle(fontSize = 18.sp)
//                )
                Spacer(modifier = GlanceModifier.size(8.dp))
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .cornerRadius(12.dp)
                        .background(Color(0xFFF1F3F4)) // Subtle material-style light background
                        .clickable(onClick = actionStartActivity<SearchActivity>())
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row {
                        Text(
                            text = "🔍",
                            style = TextStyle(fontSize = 18.sp),
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Search your files...",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(Color(0xFF5F6368)) // Muted Google-like gray
                            )
                        )
                    }
                }
            }
        }
    }
}