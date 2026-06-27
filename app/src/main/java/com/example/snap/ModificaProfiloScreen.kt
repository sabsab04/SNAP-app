package com.example.snap

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificaProfiloScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Stati per gestire il caricamento e il salvataggio
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Conserviamo il profilo originale per avere l'ID
    var profiloAttuale by remember { mutableStateOf<UserProfile?>(null) }

    // Stati per i campi di testo (i dati che l'utente modificherà)
    var username by remember { mutableStateOf("") }
    var livelloAutismo by remember { mutableStateOf("") }
    var verbalita by remember { mutableStateOf("") }
    var biografia by remember { mutableStateOf("") }

    // Carica i dati attuali dal database
    LaunchedEffect(Unit) {
        try {
            val userId = supabase.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                val profilo = supabase.client.postgrest["profili"]
                    .select(columns = Columns.ALL) {
                        filter { eq("id", userId) }
                    }.decodeSingle<UserProfile>()

                profiloAttuale = profilo

                // Pre-compila i campi con i dati esistenti (se non sono nulli)
                username = profilo.Username
                livelloAutismo = profilo.Livelloautismo ?: ""
                verbalita = profilo.Verbalita ?: ""
                biografia = profilo.Biografia ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Errore nel caricamento dati", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Modifica Profilo", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 100.dp))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = livelloAutismo,
                        onValueChange = { livelloAutismo = it },
                        label = { Text("Livello Autismo DSM-5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = verbalita,
                        onValueChange = { verbalita = it },
                        label = { Text("Verbalità") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = biografia,
                        onValueChange = { biografia = it },
                        label = { Text("Biografia") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // TASTO SALVA
                    Button(
                        onClick = {
                            if (username.isBlank()) {
                                Toast.makeText(context, "L'username non può essere vuoto!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val userId = supabase.client.auth.currentUserOrNull()?.id
                                    if (userId != null && profiloAttuale != null) {

                                        // Copiamo il profilo attuale aggiornando solo i campi modificati
                                        val profiloAggiornato = profiloAttuale!!.copy(
                                            Username = username.trim(),
                                            Livelloautismo = livelloAutismo.trim().ifEmpty { null },
                                            Verbalita = verbalita.trim().ifEmpty { null },
                                            Biografia = biografia.trim().ifEmpty { null }
                                        )

                                        // Comando Supabase per aggiornare la riga
                                        supabase.client.postgrest["profili"].update(profiloAggiornato) {
                                            filter { eq("id", userId) }
                                        }

                                        Toast.makeText(context, "Profilo aggiornato!", Toast.LENGTH_SHORT).show()
                                        onNavigateBack() // Torna alla pagina profilo
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Errore durante il salvataggio", Toast.LENGTH_LONG).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B53A2))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Salva Modifiche", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}