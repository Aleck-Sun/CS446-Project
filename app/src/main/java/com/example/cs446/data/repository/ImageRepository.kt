package com.example.cs446.data.repository

import android.content.Context
import android.net.Uri
import com.example.cs446.backend.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID

class ImageRepository {
    private val storage = SupabaseClient.supabase.storage
    private val bucketName = "images"

    suspend fun uploadPetImage(context: Context, imageUri: Uri, petId: UUID): String? {
        return try {
            val fileName = "pets/${petId}_${System.currentTimeMillis()}.jpg"
            
            // get input stream from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            
            if (imageBytes != null) {
                // upload to supabase 
                storage.from(bucketName).upload(fileName, imageBytes)
                
                // return public url
                storage.from(bucketName).publicUrl(fileName)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun deletePetImage(imageUrl: String): Boolean {
        return try {
            // extract filename from URL
            val fileName = imageUrl.substringAfterLast("/")
            storage.from(bucketName).delete(fileName)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 