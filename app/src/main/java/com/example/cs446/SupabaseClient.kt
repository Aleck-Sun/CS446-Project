package com.example.cs446

import com.example.cs446.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.serializer.MoshiSerializer
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
        install(io.github.jan.supabase.postgrest.Postgrest)
        //install(Storage)
        defaultSerializer = MoshiSerializer()
    }

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

}