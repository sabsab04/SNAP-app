package com.example.snap

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    destinatarioId: String,
    destinatarioNome: String,
    onNavigateBack: () -> Unit
) {
    var messaggioText by remember { mutableStateOf("") }
    var messaggi by remember { mutableStateOf<List<Messaggio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val mioId = supabase.client.auth.currentUserOrNull()?.id ?: ""
    val backgroundColor = Color(0xFFE5ECEF)

    // 1. CARICA I MESSAGGI ALL'AVVIO
    fun caricaMessaggi() {
        coroutineScope.launch {
            try {
                // Grazie alla RLS di Supabase, scarichiamo solo i messaggi che ci riguardano
                val tuttiIMieiMessaggi = supabase.client.postgrest["messaggi"]
                    .select()
                    .decodeList<Messaggio>()

                // Filtriamo solo quelli scambiati con QUESTO specifico destinatario
                messaggi = tuttiIMieiMessaggi.filter {
                    it.mittente_id == destinatarioId || it.destinatario_id == destinatarioId
                }.sortedBy { it.created_at } // Li ordiniamo per data

            } catch (e: Exception) {
                Log.e("CHAT", "Errore caricamento messaggi: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Carica appena apri la pagina
    LaunchedEffect(Unit) {
        caricaMessaggi()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF005684)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(destinatarioNome, color = Color(0xFF005684), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color(0xFF005684))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
            ) {
                OutlinedTextField(
                    value = messaggioText,
                    onValueChange = { messaggioText = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = { Icon(Icons.Outlined.Mood, contentDescription = "Emoji") },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (messaggioText.isNotBlank()) {
                                // 2. INVIA IL MESSAGGIO A SUPABASE
                                val nuovoMessaggio = Messaggio(
                                    mittente_id = mioId,
                                    destinatario_id = destinatarioId,
                                    testo = messaggioText.trim()
                                )
                                // Svuoto subito la casella per reattività
                                messaggioText = ""

                                coroutineScope.launch {
                                    try {
                                        supabase.client.postgrest["messaggi"].insert(nuovoMessaggio)
                                        // Ricarica la lista per far apparire il nuovo messaggio
                                        caricaMessaggi()
                                    } catch (e: Exception) {
                                        Log.e("CHAT", "Errore invio: ${e.message}")
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia", tint = Color.Black)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent, containerColor = Color.White
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (messaggi.isEmpty()) {
                Text("Nessun messaggio. Scrivi per iniziare!", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(messaggi) { msg ->
                        // Capiamo se la bolla è nostra o dello psicologo
                        val isMio = msg.mittente_id == mioId
                        BollaMessaggio(testo = msg.testo, isMio = isMio)
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun BollaMessaggio(testo: String, isMio: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMio) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMio) 16.dp else 0.dp,
                        bottomEnd = if (isMio) 0.dp else 16.dp
                    )
                )
                .background(if (isMio) Color(0xFF00A2FF) else Color.White)
                .padding(16.dp)
        ) {
            Text(testo, color = if (isMio) Color.White else Color.Black, fontSize = 16.sp)
        }
    }
}