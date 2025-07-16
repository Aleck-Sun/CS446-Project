package com.example.cs446.ui.components.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cs446.backend.data.model.ActivityLog


@Composable
fun TrendChart(activityLogs: List<ActivityLog>, modifier: Modifier) {
    val data = listOf(1)
    val fields = listOf("")
    val maxCount = data.maxBy { it.toLong() }

    Column {
        data.forEachIndexed { idx, value ->
            val text = fields[idx]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, modifier = Modifier.width(80.dp))
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width((value.toInt() * 200 / maxCount.toInt()).dp)
                        .background(Color.Blue)
                )
                Text(" $value")
            }
        }
    }
}
