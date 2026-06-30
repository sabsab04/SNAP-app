package com.example.snap

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
// Assicurati di avere importato il client di Supabase (es. io.github.jan.supabase.postgrest.postgrest)

// Aggiungiamo @Serializable per permettere a Supabase di mappare automaticamente il JSON
@Serializable
data class NewsArticle(
    val title: String,
    val source: String,
    val authors: String,
    val description: String,
    val url: String
)

@Composable
fun NewsScreen() {
    // Stati per gestire la lista e il caricamento
    var newsList by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Chiamata al database quando si apre la schermata
    LaunchedEffect(Unit) {
        scope.launch {
            try {

                val articles = supabase.client.postgrest["news_articles"]
                    .select()
                    .decodeList<NewsArticle>()

                newsList = articles
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        // Schermata di caricamento mentre scarica i dati
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF8A80))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Ho spostato il padding generale nei contentPadding per evitare che
            // la lista venga tagliata in fondo durante lo scorrimento
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(newsList) { articolo ->
                NewsCard(article = articolo)
            }
        }
    }
}

@Composable
fun NewsCard(article: NewsArticle) {
    val coloreIntestazione = Color(0xFFFF8A80)
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(coloreIntestazione)
                    .padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(text = "Fonte: ${article.source}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = "Autori: ${article.authors}", fontSize = 12.sp, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = article.description, fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = coloreIntestazione),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(text = "Continua a leggere", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}