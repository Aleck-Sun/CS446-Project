package com.example.cs446.ui.components.feed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun LocationPicker(
    selectedLocation: Location?,
    onLocationSelected: (Location?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isGettingLocation by remember { mutableStateOf(false) }
    
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        isGettingLocation = true
        
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && 
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                callback(null)
                isGettingLocation = false
                return
            }
            
            val lastKnownLocation = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: SecurityException) {
                null
            }
            
            if (lastKnownLocation != null) {
                callback(lastKnownLocation)
                isGettingLocation = false
            } else {
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        callback(location)
                        isGettingLocation = false
                        locationManager.removeUpdates(this)
                    }
                    
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {
                        callback(null)
                        isGettingLocation = false
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                
                try {
                    locationManager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        locationListener,
                        null
                    )
                } catch (_: SecurityException) {
                    callback(null)
                    isGettingLocation = false
                }
            }
        } catch (_: Exception) {
            callback(null)
            isGettingLocation = false
        }
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation { location: Location? ->
                onLocationSelected(location)
            }
        } else {
            isGettingLocation = false
        }
    }
    
    fun requestLocationPermission() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation { location: Location? ->
                onLocationSelected(location)
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (selectedLocation != null) {
                LocationDisplay(
                    location = selectedLocation,
                    onRemove = { onLocationSelected(null) }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { requestLocationPermission() },
                        enabled = !isGettingLocation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFcb85ed)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Current Location"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGettingLocation) "Getting..." else "Use Current"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationDisplay(
    location: Location,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFFcb85ed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Location Added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFcb85ed)
                )
                Text(
                    text = formatLocationText(location),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            OutlinedButton(
                onClick = onRemove,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Remove")
            }
        }
    }
}

private fun formatLocationText(location: Location): String {
    val latStr = String.format(Locale.US, "%.3f", location.latitude)
    val lngStr = String.format(Locale.US, "%.3f", location.longitude)
    return "$latStr, $lngStr"
} 