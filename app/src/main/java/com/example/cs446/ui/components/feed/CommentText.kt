package com.example.cs446.ui.components.feed

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CommentText(author: String, comment: String) {
    val styledText = buildAnnotatedString {
        // Bold author name
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(author)
        pop()
        append(": $comment")
    }

    Text(text = styledText, style= MaterialTheme.typography.bodyMedium)
}
