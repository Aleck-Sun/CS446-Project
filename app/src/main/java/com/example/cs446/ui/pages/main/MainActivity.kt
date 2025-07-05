package com.example.cs446.ui.pages.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.cs446.ui.pages.login.LoginActivity
import com.example.cs446.view.pets.PetsViewModel
import com.example.cs446.view.social.FeedViewModel
import com.example.cs446.view.pets.PermissionsViewModel
import com.example.cs446.view.social.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.UUID

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val petsViewModel = PetsViewModel()
        val feedViewModel = FeedViewModel()
        val profileViewModel = ProfileViewModel()
        val permissionsViewModel = PermissionsViewModel()

        val shareContent = intent.getBooleanExtra("share_post", false)
        val sharedText = intent.getStringExtra("shared_text")
        val sharedImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("shared_image_uri", Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("shared_image_uri")
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

        setContent {
            MainNavigator(
                petsViewModel = petsViewModel,
                feedViewModel = feedViewModel,
                profileViewModel = profileViewModel,
                onLogout = onLogout,
                permissionsViewModel = permissionsViewModel,
                onShare = ::onShare,
                shareContent = shareContent,
                sharedText = sharedText,
                sharedImageUri = sharedImageUri
            )
        }
    }
}
