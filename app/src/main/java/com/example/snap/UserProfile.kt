package com.example.snap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserProfile(
    val id: String,
    val Email: String,
    val Ruolo: String,
    val Username: String,
    val Biografia: String? = null,


    // CORREZIONE: Forza il nome esatto della colonna su Supabase
    @SerialName("Livelloautismo") // <-- Metti qui il nome esatto che vedi su Supabase (es. "livello autismo" o "livello_autismo")
    val Livelloautismo: String? = null,

    val Verbalita: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    // Facciamo lo stesso anche per i campi dello psicologo per sicurezza
    @SerialName("NomeCognome")
    val NomeCognome: String? = null,

    val Mansione: String? = null,

    @SerialName("Titolodistudio")
    val Titolodistudio: String? = null
)