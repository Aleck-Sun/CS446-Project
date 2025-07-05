package com.example.cs446.ui.components.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun IconWithText(
    icon: ImageVector,
    count: Int,
    tint: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClick: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.clickable(onClick = onClick)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$count",
            fontSize = fontSize
        )
    }
}

@Composable
fun IconWithText(
    icon: ImageVector,
    text: String,
    tint: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClick: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.clickable(onClick = onClick)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            fontSize = fontSize
        )
    }
}