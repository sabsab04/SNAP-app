package com.example.snap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backgroundColor = Color(0xFFF1EEEE)
    val coroutineScope = rememberCoroutineScope()

    // --- VARIABILI DI STATO ---
    var searchQuery by remember { mutableStateOf("") } // Testo della ricerca
    var avatarUrl by remember { mutableStateOf<String?>(null) } // Link foto profilo

    // --- CARICAMENTO AVATAR ALL'AVVIO ---
    LaunchedEffect(Unit) {
        try {
            val userId = supabase.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                // Scarica SOLO l'url della foto per essere super veloce
                val profile = supabase.client.postgrest["profili"]
                    .select(columns = Columns.list("avatar_url")) {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<UserProfile>()

                avatarUrl = profile?.avatarUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
    ) {
        // SOSTITUITO: Usiamo la nuova barra interattiva
        MainTopBar(
            avatarUrl = avatarUrl,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onProfileClick = { navController.navigate("profilo") }
        )

        CustomNavBar(navController)

        NavHost(
            navController = navController,
            startDestination = "news",
            modifier = Modifier.weight(1f)
        ) {

            composable("news") {
                NewsScreen(searchQuery = searchQuery)
            }
            composable("community") { CommunityScreen() }
            composable("psicologi") { PsicologiScreen(navController = navController) }
            composable("chat_detail/{destinatarioId}/{destinatarioNome}") { backStackEntry ->
                val destId = backStackEntry.arguments?.getString("destinatarioId") ?: ""
                val destNome = backStackEntry.arguments?.getString("destinatarioNome") ?: "Chat"

                ChatDetailScreen(
                    destinatarioId = destId,
                    destinatarioNome = destNome,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("locale") { MapScreen() }
            composable(route = "profilo") {
                ProfiloScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate("modifica_profilo") }
                )
            }
            composable(route = "modifica_profilo") {
                ModificaProfiloScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// --- NUOVA BARRA HEADER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    avatarUrl: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FOTO PROFILO
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profilo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // BARRA DI RICERCA VERA
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cerca", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun CustomNavBar(navController: NavHostController) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF8A80), // News
            Color(0xFFFFF080), // Community
            Color(0xFF80FFD4), // Psicologi
            Color(0xFFCC99FF)  // Locale
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(
            icon = Icons.Filled.Newspaper,
            label = "News",
            onClick = { navController.navigate("news") }
        )
        NavBarItem(
            icon = Icons.Filled.Groups,
            label = "Community",
            onClick = { navController.navigate("community") }
        )
        NavBarItem(
            icon = Icons.Filled.Psychology,
            label = "Psicologi",
            onClick = { navController.navigate("psicologi") }
        )
        NavBarItem(
            icon = Icons.Filled.Place,
            label = "Locale",
            onClick = { navController.navigate("locale") }
        )
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )
    }
}