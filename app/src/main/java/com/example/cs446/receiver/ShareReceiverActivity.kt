package com.example.cs446.receiver

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.cs446.ui.pages.main.MainActivity

class ShareReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sharedImageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        // Pass the data to your MainActivity or target Compose screen
        val forwardIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("share_post", true)
            putExtra("shared_text", sharedText)
            putExtra("shared_image_uri", sharedImageUri)
        }

        startActivity(forwardIntent)
        finish()
    }
}
