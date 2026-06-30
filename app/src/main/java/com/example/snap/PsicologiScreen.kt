package com.example.snap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun PsicologiScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- NUOVO: Stato per contare le chat presenti ---
    var numeroChatPresenti by remember { mutableIntStateOf(0) }
    val mioId = supabase.client.auth.currentUserOrNull()?.id ?: ""

    val backgroundColor = Color(0xFFE5ECEF)

    // Ricalcola il numero di chat ogni volta che cambi tab
    // (così se invii un messaggio e torni indietro, si aggiorna subito)
    LaunchedEffect(selectedTab) {
        try {
            val mieiMessaggi = supabase.client.postgrest["messaggi"]
                .select()
                .decodeList<Messaggio>()

            // Conta gli ID unici delle persone con cui hai scambiato messaggi
            val interlocutoriIds = mieiMessaggi.map { msg ->
                if (msg.mittente_id == mioId) msg.destinatario_id else msg.mittente_id
            }.distinct()

            numeroChatPresenti = interlocutoriIds.size
        } catch (e: Exception) {
            android.util.Log.e("BADGE_CHAT", "Errore conteggio chat: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // --- CUSTOM TAB ROW ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = backgroundColor,
            contentColor = Color(0xFF005684),
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF005684) // Colore linea blu
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Psicologi", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // --- NUOVO: Mostra il badge solo se c'è almeno 1 chat ---
                        if (numeroChatPresenti > 0) {
                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                Text(numeroChatPresenti.toString())
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("Chat", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

        // --- CONTENUTO DELLE TAB ---
        if (selectedTab == 0) {
            ListaPsicologi(navController)
        } else {
            ListaChat(navController)
        }
    }
}

// ---------------------------------------------------------
// TAB 1: LISTA PSICOLOGI
// ---------------------------------------------------------
@Composable
fun ListaPsicologi(navController: NavController) {
    var psicologi by remember { mutableStateOf<List<Profilo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Scarica tutti gli utenti con ruolo 'psicologo'
            psicologi = supabase.client.postgrest["profili"]
                .select { filter { eq("Ruolo", "psicologo") } }
                .decodeList<Profilo>()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(psicologi) { psicologo ->
                CardPsicologo(
                    nome = psicologo.Username,
                    onChiamaClick = { /* Da implementare */ },
                    onMessaggiaClick = {
                        // Naviga alla chat passando l'ID e il nome dello psicologo
                        navController.navigate("chat_detail/${psicologo.id}/${psicologo.Username}")
                    }
                )
            }
        }
    }
}

@Composable
fun CardPsicologo(nome: String, onChiamaClick: () -> Unit, onMessaggiaClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFF005684)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = nome, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005684))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.padding(start = 76.dp)) {
            OutlinedButton(
                onClick = onChiamaClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF005684))
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chiama")
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(
                onClick = onMessaggiaClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF005684))
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}


@Composable
fun ListaChat(navController: NavController) {
    var chatUtenti by remember { mutableStateOf<List<Profilo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val mioId = supabase.client.auth.currentUserOrNull()?.id ?: ""

    LaunchedEffect(Unit) {
        try {
            // 1. Scarica tutti i messaggi dell'utente
            val mieiMessaggi = supabase.client.postgrest["messaggi"]
                .select()
                .decodeList<Messaggio>()

            // 2. Estrai gli ID delle persone con cui hai chattato (escludendo te stesso)
            val interlocutoriIds = mieiMessaggi.map { msg ->
                if (msg.mittente_id == mioId) msg.destinatario_id else msg.mittente_id
            }.distinct()

            // 3. Scarica i profili solo di quelle persone
            if (interlocutoriIds.isNotEmpty()) {
                chatUtenti = supabase.client.postgrest["profili"]
                    .select { filter { isIn("id", interlocutoriIds) } }
                    .decodeList<Profilo>()
            }
        } catch (e: Exception) {
            android.util.Log.e("LISTA_CHAT", "Errore: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (chatUtenti.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessuna chat attiva. Vai in Psicologi per iniziare!", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chatUtenti) { utente ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Naviga alla chat passando i dati veri
                            navController.navigate("chat_detail/${utente.id}/${utente.Username}")
                        }
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFF005684)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = utente.Username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005684))
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF005684))
                }
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}