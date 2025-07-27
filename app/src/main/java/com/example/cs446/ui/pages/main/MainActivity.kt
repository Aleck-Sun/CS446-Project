package com.example.cs446.ui.pages.main

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.cs446.common.badges.BadgeComponent
import com.example.cs446.ui.pages.login.LoginActivity
import com.example.cs446.view.pets.PetsViewModel
import com.example.cs446.view.social.FeedViewModel
import com.example.cs446.view.pets.PermissionsViewModel
import com.example.cs446.view.pets.RemindersViewModel
import com.example.cs446.view.social.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.TimeZone
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        TimeZone.setDefault(TimeZone.getTimeZone("America/Toronto"))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val petsViewModel = PetsViewModel()
        val feedViewModel = FeedViewModel()
        val profileViewModel = ProfileViewModel()
        val permissionsViewModel = PermissionsViewModel()
        val remindersViewModel = RemindersViewModel()
        val badgeComponent = BadgeComponent()

        requestNotificationPermissions()
        remindersViewModel.scheduleAllUpcomingReminders(applicationContext)

        val shareContent = intent.getBooleanExtra("share_post", false)
        val sharedText = intent.getStringExtra("shared_text")
        val sharedImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("shared_image_uri", Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("shared_image_uri")
        }
        if (shareContent) {
            feedViewModel.setSharedData(sharedText, sharedImageUri)
        }

        val onLogout = {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        suspend fun downloadImageToCache(
            context: Context,
            imageUrl: String
        ): Uri = withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
            URL(imageUrl).openStream().use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }

        fun onShare(context: Context, caption: String, imageUrl: String) {
            lifecycleScope.launch {
                val imageUri = downloadImageToCache(context, imageUrl)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
            }
        }

        lifecycleScope.launch {
            badgeComponent.startObserving(scope = CoroutineScope(SupervisorJob() + Dispatchers.Default))
        }

        setContent {
            MainNavigator(
                petsViewModel = petsViewModel,
                feedViewModel = feedViewModel,
                profileViewModel = profileViewModel,
                remindersViewModel = remindersViewModel,
                onLogout = onLogout,
                permissionsViewModel = permissionsViewModel,
                onShare = ::onShare,
            )
        }
    }

    private fun requestNotificationPermissions() {
        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }

        // Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
