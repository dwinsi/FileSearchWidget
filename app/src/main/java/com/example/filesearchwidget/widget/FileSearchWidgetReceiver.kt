package com.example.filesearchwidget.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class FileSearchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: FileSearchWidget = FileSearchWidget
}