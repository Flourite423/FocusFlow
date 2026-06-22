package com.focusflow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriorityChip(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (priority.lowercase()) {
        "urgent" -> Color(0xFFFFEBEE) to Color(0xFFC62828)      // Red
        "high" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)        // Orange
        "medium" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)      // Green
        "low" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)         // Blue
        else -> androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant to androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.uppercase(),
            fontSize = 10.sp,
            color = textColor,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}