package com.example.cs446.backend

import com.example.cs446.BuildConfig
import com.example.cs446.backend.data.repository.InstantAdapter
import com.example.cs446.backend.data.repository.JavaTimeInstantAdapter
import com.example.cs446.backend.data.repository.UUIDAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.MoshiSerializer
import io.github.jan.supabase.storage.Storage

const val supabaseUrl = BuildConfig.SUPABASE_URL
const val supabaseKey = BuildConfig.SUPABASE_KEY

object SupabaseClient {
    val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)

        val moshi = Moshi.Builder()
            .add(UUIDAdapter())
            .add(KotlinJsonAdapterFactory())
            .add(InstantAdapter())
            .add(JavaTimeInstantAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        defaultSerializer = MoshiSerializer(moshi)
    }
}
