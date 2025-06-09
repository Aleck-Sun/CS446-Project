package com.example.cs446.backend

import com.example.cs446.BuildConfig
import com.example.cs446.backend.data.repository.InstantAdapter
import com.example.cs446.backend.data.repository.UUIDAdapter
import com.example.cs446.backend.data.repository.UserRepository
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
            .add(InstantAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        defaultSerializer = MoshiSerializer(moshi)
    }
}
