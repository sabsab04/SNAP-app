package com.example.snap

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.UUID

// Aggiungiamo 'imageUri' per salvare la vera foto
data class CommunityPost(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorLevel: Int,
    val title: String,
    val content: String,
    val hasImage: Boolean = false,
    val imageUri: Uri? = null, // <--- Memorizza la foto reale
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false
)

val SfondoCommunity = Color(0xFFE8F5E9)
val ColoreCardPost = Color(0xFFC5E1A5)

@Composable
fun CommunityScreen() {
    val posts = remember {
        mutableStateListOf(
            CommunityPost(authorName = "Anna Bianchi", authorLevel = 2, title = "Consiglio routine serale", content = "Qualcuno ha strategie efficaci per gestire la transizione verso il sonno senza troppe crisi? Grazie!", hasImage = true, likesCount = 4),
            CommunityPost(authorName = "Giulio Bruni", authorLevel = 1, title = "Integrazione scolastica", content = "Oggi primo giorno con il nuovo assistente all'autonomia, è andata benissimo!", hasImage = false, likesCount = 2)
        )
    }

    var isCreatingPost by remember { mutableStateOf(false) }

    BackHandler(enabled = isCreatingPost) { isCreatingPost = false }

    Box(modifier = Modifier.fillMaxSize().background(SfondoCommunity)) {
        if (isCreatingPost) {
            CreatePostView(
                onBack = { isCreatingPost = false },
                onPostCreated = { titolo, testo, fotoScelta ->
                    posts.add(0, CommunityPost(
                        authorName = "Tu (Utente)",
                        authorLevel = 1,
                        title = titolo,
                        content = testo,
                        imageUri = fotoScelta,
                        hasImage = fotoScelta != null
                    ))
                    isCreatingPost = false
                }
            )
        } else {
            CommunityFeedView(
                posts = posts,
                onAddPostClick = { isCreatingPost = true },
                onLikeToggle = { postId ->
                    val index = posts.indexOfFirst { it.id == postId }
                    if (index != -1) {
                        val currentPost = posts[index]
                        if (currentPost.isLikedByMe) {
                            posts[index] = currentPost.copy(isLikedByMe = false, likesCount = currentPost.likesCount - 1)
                        } else {
                            posts[index] = currentPost.copy(isLikedByMe = true, likesCount = currentPost.likesCount + 1)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CommunityFeedView(
    posts: List<CommunityPost>,
    onAddPostClick: () -> Unit,
    onLikeToggle: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(post = post, onLikeClick = { onLikeToggle(post.id) })
            }
        }

        FloatingActionButton(
            onClick = onAddPostClick,
            containerColor = ColoreCardPost,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuovo Post", tint = Color.Black)
        }
    }
}

@Composable
fun PostCard(post: CommunityPost, onLikeClick: () -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "livello ${post.authorLevel}", fontSize = 12.sp, color = Color.DarkGray)
            }
            IconButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${post.title}\n\n${post.content}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Condividi"))
                },
                modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Condividi", modifier = Modifier.size(20.dp))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColoreCardPost)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(text = post.content, fontSize = 14.sp)

                // MOSTRA LA FOTO VERA (Se presente)
                if (post.imageUri != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = post.imageUri,
                        contentDescription = "Immagine caricata",
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop // Taglia i bordi in eccesso per farla quadrata/rettangolare
                    )
                }
                // Altrimenti mostra la vecchia finta immagine grigia per i post di prova
                else if (post.hasImage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Immagine", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.likesCount > 0) {
                Text(text = post.likesCount.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
            }
            val buttonColor by animateColorAsState(targetValue = if (post.isLikedByMe) Color(0xFF7CB342) else ColoreCardPost, label = "")
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.background(buttonColor, CircleShape).size(32.dp)
            ) {
                Icon(
                    imageVector = if (post.isLikedByMe) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                    contentDescription = "Mi Piace",
                    modifier = Modifier.size(16.dp),
                    tint = if (post.isLikedByMe) Color.White else Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun CreatePostView(onBack: () -> Unit, onPostCreated: (String, String, Uri?) -> Unit) {
    var titolo by remember { mutableStateOf("") }
    var contenuto by remember { mutableStateOf("") }

    // Stato per salvare l'URI della foto vera presa dalla galleria
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // IL MOTORE DELLA GALLERIA: Questo gestisce l'apertura in automatico e la sicurezza!
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri } // Salva l'immagine quando l'utente la seleziona
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Previous", color = Color.Black)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ColoreCardPost)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = titolo,
                    onValueChange = { titolo = it },
                    placeholder = { Text("Titolo del post", fontSize = 18.sp, color = Color.DarkGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = contenuto,
                    onValueChange = { contenuto = it },
                    placeholder = { Text("Dialogo del post", color = Color.DarkGray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Se hai scelto un'immagine, ti fa vedere l'anteprima piccola nel form!
                if (selectedImageUri != null) {
                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Anteprima",
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Tasto per rimuovere la foto se hai sbagliato
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.White, CircleShape).size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Rimuovi", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ElevatedButton(
                        // LANCIAMO LA GALLERIA QUI:
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (selectedImageUri != null) Color(0xFF7CB342) else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (selectedImageUri != null) Icons.Default.Check else Icons.Default.Image,
                            contentDescription = null,
                            tint = if (selectedImageUri != null) Color.White else ColoreCardPost
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri != null) "Immagine aggiunta" else "Aggiungi immagine",
                            color = if (selectedImageUri != null) Color.White else Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (titolo.isNotBlank() && contenuto.isNotBlank()) {
                    // Inviamo il file della foto vera alla funzione
                    onPostCreated(titolo, contenuto, selectedImageUri)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(150.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColoreCardPost)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Post", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}