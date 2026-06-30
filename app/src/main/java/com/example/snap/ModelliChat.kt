package com.example.snap

import kotlinx.serialization.Serializable

@Serializable
data class Profilo(
    val id: String,
    val Username: String,
    val ruolo: String? = null
)

@Serializable
data class Messaggio(
    val id: String? = null,
    val mittente_id: String,
    val destinatario_id: String,
    val testo: String,
    val letto: Boolean = false,
    val created_at: String? = null
)