package com.example.snap // <-- Controlla solo che questo sia il pacchetto giusto

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

class MainActivity : ComponentActivity() {
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
fun MainScreen()  {
    val navController = rememberNavController()
    val backgroundColor = Color(0xFFF1EEEE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding() // <--- AGGIUNGI QUESTA RIGA QUI
    ) {
        CustomHeader(navController)
        CustomNavBar(navController)

        NavHost(
            navController = navController,
            startDestination = "news",
            modifier = Modifier.weight(1f)
        ) {
            composable("news") { NewsScreen() }
            composable("community") { CommunityScreen() }
            composable("psicologi") { PsicologiScreen() }
            composable("locale") { MapScreen() }
            composable("profilo") { ProfiloScreen() }
        }
    }
}

@Composable
fun CustomHeader(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Button(
                onClick = { navController.navigate("profilo") },
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {}
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = "Cerca", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Search", color = Color.Gray)
            }
        }
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
        // Usiamo la nostra funzione personalizzata per ogni tasto
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
            icon = Icons.Filled.Psychology, // Questa icona è perfetta per gli psicologi!
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
    // Column mette l'icona sopra e il testo sotto
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() } // Rende cliccabile l'intera area
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

// --- Le nostre sezioni ---


@Composable
fun PsicologiScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Sezione Psicologi")
    }
}

@Composable
fun MapScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Sezione Locale: Mappe")
    }
}

@Composable
fun ProfiloScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Sezione Profilo Utente")
    }
}