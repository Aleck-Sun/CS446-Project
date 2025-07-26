package com.example.cs446.ui.components.feed

import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapDialog(
    location: Location,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    fun openInExternalMaps() {
        val geoUri = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Post Location)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        context.startActivity(intent)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = ::openInExternalMaps) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in Maps")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open in Maps")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text("Post Location")
        },
        text = {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(400.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                        
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            
                            val mapController = controller
                            val startPoint = GeoPoint(location.latitude, location.longitude)
                            mapController.setZoom(15.0)
                            mapController.setCenter(startPoint)
                            
                            // add marker
                            val marker = Marker(this)
                            marker.position = startPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Post Location"
                            marker.snippet = "This is where the post was made"
                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(16.dp)
    )
} 