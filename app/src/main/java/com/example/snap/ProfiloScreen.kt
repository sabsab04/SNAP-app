package com.example.snap

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun ProfiloScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    // Il contesto ci serve per far ripartire l'app verso il Login
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Stati per la gestione della UI
    var profiloUtente by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) } // Stato per l'Alert Dialog

    // Colori presi dal tuo design
    val palePurpleBackground = Color(0xFFFCF5FF)
    val iconContainerColor = Color(0xFFF3E5FF)
    val iconTint = Color(0xFFC7B1E4)
    val editButtonColor = Color(0xFF6B53A2)

    // Recupero dati da Supabase appena si apre la schermata
    LaunchedEffect(Unit) {
        try {
            val userId = supabase.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                profiloUtente = supabase.client.postgrest["profili"]
                    .select(columns = Columns.ALL) {
                        filter { eq("id", userId) }
                    }.decodeSingle<UserProfile>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // --- FINESTRA DI DIALOGO PER ELIMINAZIONE ACCOUNT ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina Account", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler eliminare definitivamente il tuo account? Questa azione è irreversibile.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            try {
                                // Scollega l'utente (o inserisci l'API per l'eliminazione definitiva)
                                supabase.client.auth.signOut()

                                // Riporta al login pulendo la coda delle activity
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                ) {
                    Text("Elimina", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profilo", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()) // <--- AGGIUNTO LO SCORRIMENTO QUI
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- AVATAR CON TASTO EDIT ---
                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                tint = Color.White
                            )
                        }

                        // Tasto MATITA (Modifica)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(editButtonColor)
                                .clickable {
                                    onNavigateToEdit()
                                    },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Modifica Profilo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // --- LISTA DELLE CARD ---
                    ProfileCard(
                        icon = Icons.Outlined.Person,
                        title = "Username",
                        value = profiloUtente?.Username ?: "...",
                        cardBgColor = palePurpleBackground,
                        iconBgColor = iconContainerColor,
                        iconColor = iconTint
                    )

                    ProfileCard(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "Livello Autismo DSM-5",
                        value = profiloUtente?.Livelloautismo ?: "...",
                        cardBgColor = palePurpleBackground,
                        iconBgColor = iconContainerColor,
                        iconColor = iconTint
                    )

                    ProfileCard(
                        icon = Icons.Outlined.Mic,
                        title = "Verbalità",
                        value = profiloUtente?.Verbalita ?: "...",
                        cardBgColor = palePurpleBackground,
                        iconBgColor = iconContainerColor,
                        iconColor = iconTint
                    )

                    ProfileCard(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "Biografia",
                        value = profiloUtente?.Biografia ?: "...",
                        cardBgColor = palePurpleBackground,
                        iconBgColor = iconContainerColor,
                        iconColor = iconTint
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TASTI DI AZIONE (LOGOUT ED ELIMINA) ---

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    supabase.client.auth.signOut()

                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Log Out")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elimina Account", color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp)) // Aggiunge un po' di spazio vuoto in fondo
                }
            }
        }
    }
}

// Componente per la singola riga
@Composable
fun ProfileCard(
    icon: ImageVector,
    title: String,
    value: String,
    cardBgColor: Color,
    iconBgColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, iconBgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value.ifEmpty { "..." },
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}