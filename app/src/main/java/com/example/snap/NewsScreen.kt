package com.example.snap

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class NewsArticle(
    val title: String,
    val source: String,
    val authors: String,
    val description: String,
    val url: String
)


val fintaListaArticoli = listOf(
    NewsArticle(
        "L'importanza della diagnosi precoce",
        "PubMed",
        "Dr. Rossi, Dr. Bianchi",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor...",
        "https://pubmed.ncbi.nlm.nih.gov/" // Link inserito qui
    ),
    NewsArticle(
        "Nuovi studi sull'integrazione sensoriale",
        "Journal of Autism",
        "Dr.ssa Verdi",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor...",
        "https://www.google.com" // Puoi mettere qualsiasi link
    ),
    NewsArticle(
        "Supporto alle famiglie: linee guida",
        "Ministero della Salute",
        "Autori Vari",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor...",
        "https://www.salute.gov.it"
    )
)

@Composable
fun NewsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(fintaListaArticoli) { articolo ->
            NewsCard(article = articolo)
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
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