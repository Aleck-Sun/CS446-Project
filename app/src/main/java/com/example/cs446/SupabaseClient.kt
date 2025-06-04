package com.example.cs446

import com.example.cs446.BuildConfig
import com.example.cs446.data.repository.UUIDAdapter
import com.example.cs446.data.repository.UserRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.serializer.MoshiSerializer
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val supabaseUrl = BuildConfig.SUPABASE_URL
const val supabaseKey = BuildConfig.SUPABASE_KEY



object SupabaseClient {
    val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Auth)
        install(Postgrest)
        //install(Storage)

        val moshi = Moshi.Builder()
            .add(UUIDAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        defaultSerializer = MoshiSerializer(moshi)
    }
    val userRepository = UserRepository()

    fun loginWithSupabase(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Login failed")
                }
            }
        }
    }

    fun signUpWithSupabase(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("Signup process started")
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = supabase.auth.currentSessionOrNull()?.user?.id
                if (userId != null) {
                    try {
                        userRepository.createNewUser(userId, email)
                    } catch (e: Exception) {
                        print(e.message)
                        throw Exception(
                            "Could not add user. Please try again."
                        )
                    }

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                println("Error: $e.toString()")
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Sign Up failed")
                }
            }
        }
    }
}