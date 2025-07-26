package com.example.cs446.ui.components.feed

import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun LocationButton(
    location: Location,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Loading location...") }
    
    LaunchedEffect(location) {
        locationText = getCityFromLocation(context, location)
    }
    
    Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFFcb85ed),
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = locationText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFcb85ed),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "📍 View on map",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFcb85ed)
            )
        }
    }
}

private suspend fun getCityFromLocation(context: android.content.Context, location: Location): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        continuation.resume(addresses)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                
                // formating
                val city = address.locality ?: address.subAdminArea
                val state = address.adminArea
                val country = address.countryName
                
                when {
                    city != null && state != null -> "$city, $state"
                    city != null && country != null -> "$city, $country"
                    city != null -> city
                    state != null -> state
                    country != null -> country
                    else -> formatCoordinates(location)
                }
            } else {
                formatCoordinates(location)
            }
        } catch (e: Exception) {
            formatCoordinates(location)
        }
    }
}

private fun formatCoordinates(location: Location): String {
    val latStr = String.format(Locale.US, "%.3f", location.latitude)
    val lngStr = String.format(Locale.US, "%.3f", location.longitude)
    return "$latStr, $lngStr"
} 