package com.example.snap

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class supabase : Application() {

    companion object {
        // Questa variabile conterrà il client e la userai in tutta l'app
        lateinit var client: SupabaseClient
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Inizializzazione di Supabase
        client = createSupabaseClient(
            supabaseUrl = "https://jxhrauzkdmotwdntoolz.supabase.co",
            supabaseKey = "sb_publishable_0SZ0kF438MNDFmHQX1fGOg_svDm1UkD"
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }
}