package com.example.cs446

import com.example.cs446.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.serializer.MoshiSerializer

const val supabaseUrl = BuildConfig.SUPABASE_URL
const val supabaseKey = BuildConfig.SUPABASE_KEY

object SupabaseClient {
    val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        //install(Auth)
        install(io.github.jan.supabase.postgrest.Postgrest)
        //install(Storage)
        defaultSerializer = MoshiSerializer()
    }
}