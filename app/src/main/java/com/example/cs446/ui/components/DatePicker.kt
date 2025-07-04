package com.example.cs446.ui.components

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
    val colorScheme = MaterialTheme.colorScheme

    val datePickerDialog = remember(colorScheme) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setOnShowListener {
                    val buttonPositive = getButton(DatePickerDialog.BUTTON_POSITIVE)
                    val buttonNegative = getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    
                    buttonPositive?.setTextColor(colorScheme.primary.toArgb())
                    buttonNegative?.setTextColor(colorScheme.primary.toArgb())
                    
                    val datePicker = datePicker
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            val headerViewId = context.resources.getIdentifier("date_picker_header", "id", "android")
                            val headerView = datePicker.findViewById<android.view.View>(headerViewId)
                            headerView?.setBackgroundColor(colorScheme.primary.toArgb())
                        } catch (e: Exception) {
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val colorStateList = ColorStateList.valueOf(colorScheme.primary.toArgb())
                                
                                val dayPickerViewId = context.resources.getIdentifier("day_picker_view_pager", "id", "android")
                                val dayPickerView = datePicker.findViewById<android.view.View>(dayPickerViewId)
                                val monthViewId = context.resources.getIdentifier("month_view", "id", "android")
                                val monthView = datePicker.findViewById<android.view.View>(monthViewId)
                                
                                dayPickerView?.backgroundTintList = colorStateList
                                monthView?.backgroundTintList = colorStateList
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            }
        }
    }

    OutlinedTextField(
        value = date.format(formatter),
        onValueChange = {},
        label = { Text(label) },
        enabled = false,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange, 
                contentDescription = "Select date",
                tint = colorScheme.onSurfaceVariant
            )
        },
        // Note: for date picker dialog to show text field needs to be disabled
        // so we override disabled colors
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = colorScheme.onSurface,
            disabledBorderColor = colorScheme.outline,
            disabledLabelColor = colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                datePickerDialog.updateDate(date.year, date.monthValue - 1, date.dayOfMonth)
                datePickerDialog.show()
            }
    )
}

