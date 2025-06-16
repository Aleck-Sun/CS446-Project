package com.example.cs446.data.repository

import android.content.Context
import android.net.Uri
import com.example.cs446.backend.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import java.util.UUID
import kotlin.time.Duration

class PostRepository {
    private val storage = SupabaseClient.supabase.storage
    private val auth = SupabaseClient.supabase.auth
    private val bucketName = "posts"

    suspend fun uploadPostImages(
        context: Context,
        imageUris: List<Uri>,
        petId: UUID
    ): List<String> {
        // TODO - will test image uploading later when post backend is made
        val imageUrls = mutableListOf<String>()
        try {
            imageUris.forEach {
                imageUri -> val fileName = "pets/${petId}_${System.currentTimeMillis()}.jpg"

                // get input stream from URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()

                if (imageBytes != null) {
                    // upload to supabase
                    storage.from(bucketName).upload(fileName, imageBytes)

                    // return public url
                    storage.from(bucketName).publicUrl(fileName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageUrls
    }
    
    suspend fun getSignedImageUrl(imageUrl: String): String {
        return try {
            storage.from(bucketName).createSignedUrl(
                // TODO - this should be the URL of the poster, not the user
                path = "${auth.currentUserOrNull()!!.id}/$imageUrl",
                expiresIn = Duration.parse("1h")  // expires in 1 hour
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
} 