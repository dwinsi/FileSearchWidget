package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOrderDropdown(
    selectedOrder: SortOrder,
    onOrderSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier // ✅ Added modifier parameter
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // ✅ Use the passed-in modifier here
    ) {
        TextField(
            readOnly = true,
            value = selectedOrder.label(),
            onValueChange = {},
            label = { Text("Sort by") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(order.label()) },
                    onClick = {
                        onOrderSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun SortOrder.label(): String {
    return when (this) {
        SortOrder.NAME_ASC -> "Name (A-Z)"
        SortOrder.NEWEST_FIRST -> "Newest First"
        SortOrder.OLDEST_FIRST -> "Oldest First"
    }
}