package com.example.snap

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfiloScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var profiloUtente by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Variabile per mostrare l'anteprima istantanea dell'avatar
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    val palePurpleBackground = Color(0xFFFCF5FF)
    val iconContainerColor = Color(0xFFF3E5FF)
    val iconTint = Color(0xFFC7B1E4)
    val editButtonColor = Color(0xFF6B53A2)

    // Logica di upload della foto
    fun updateProfileAvatar(userId: String, uri: Uri) {
        coroutineScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch

                val fileName = "avatar_$userId.jpg"

                supabase.client.storage["avatars"].upload(fileName, bytes, upsert = true)
                val publicUrl = supabase.client.storage["avatars"].publicUrl(fileName)

                supabase.client.postgrest["profili"].update({
                    set("avatar_url", publicUrl)
                }) {
                    filter { eq("id", userId) }
                }

                avatarUrl = publicUrl
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val userId = supabase.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                updateProfileAvatar(userId, uri)
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val userId = supabase.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                val profile = supabase.client.postgrest["profili"]
                    .select(columns = Columns.ALL) {
                        filter { eq("id", userId) }
                    }.decodeSingle<UserProfile>()

                profiloUtente = profile
                avatarUrl = profile.avatarUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

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
                                supabase.client.auth.signOut()
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- AVATAR ---
                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Foto Profilo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = "Avatar Vuoto",
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        // Tasto MATITA
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(editButtonColor)
                                .clickable { onNavigateToEdit() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    // --- DATI CONDIVISI DA ENTRAMBI ---
                    ProfileCard(
                        icon = Icons.Outlined.Person,
                        title = "Username",
                        value = profiloUtente?.Username ?: "...",
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

                    // --- DISTINZIONE RUOLI ---
                    // Controlliamo il ruolo ignorando maiuscole/minuscole
                    val isPsicologo = profiloUtente?.Ruolo?.equals("psicologo", ignoreCase = true) == true

                    if (isPsicologo) {
                        // CARD SPECIFICHE PER LO PSICOLOGO
                        ProfileCard(
                            icon = Icons.Outlined.Badge,
                            title = "Nome e Cognome",
                            value = profiloUtente?.NomeCognome ?: "...",
                            cardBgColor = palePurpleBackground,
                            iconBgColor = iconContainerColor,
                            iconColor = iconTint
                        )

                        ProfileCard(
                            icon = Icons.Outlined.WorkOutline,
                            title = "Mansione",
                            value = profiloUtente?.Mansione ?: "...",
                            cardBgColor = palePurpleBackground,
                            iconBgColor = iconContainerColor,
                            iconColor = iconTint
                        )

                        ProfileCard(
                            icon = Icons.Outlined.School,
                            title = "Titolo di Studio",
                            value = profiloUtente?.Titolodistudio ?: "...",
                            cardBgColor = palePurpleBackground,
                            iconBgColor = iconContainerColor,
                            iconColor = iconTint
                        )
                    } else {
                        // CARD SPECIFICHE PER L'UTENTE STANDARD
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
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TASTI DI AZIONE ---
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    supabase.client.auth.signOut()
                                    val intent = Intent(context, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
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

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// (La funzione ProfileCard rimane identica a quella che ti avevo scritto nel blocco precedente)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, iconBgColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value.ifEmpty { "..." }, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}