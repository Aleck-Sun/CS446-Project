package com.example.cs446.ui.components

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerTextField(
    date: LocalDate,
    label: String,
    formatter: DateTimeFormatter,
    onDateChange: (LocalDate) -> Unit,
) {
    val context = LocalContext.current

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )

    OutlinedTextField(
        value = date.format(formatter),
        onValueChange = {},
        label = { Text(label) },
        enabled = false,
        trailingIcon = {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select date")
        },
        // Note: for date picker dialog to show text field needs to be disabled
        // so we override disabled colors
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = LocalContentColor.current,
            disabledBorderColor = LocalContentColor.current.copy(alpha = 0.5f),
            disabledLabelColor = LocalContentColor.current.copy(alpha = 0.7f),
            disabledLeadingIconColor = LocalContentColor.current.copy(alpha = 0.7f),
            disabledTrailingIconColor = LocalContentColor.current.copy(alpha = 0.7f),
            disabledPlaceholderColor = LocalContentColor.current.copy(alpha = 0.7f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                datePickerDialog.show()
            }
    )
}

