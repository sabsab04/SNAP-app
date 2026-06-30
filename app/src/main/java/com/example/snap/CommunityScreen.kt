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
import androidx.compose.material.icons.outlined.Person
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
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CommunityPost(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("author_id") val authorId: String? = null, // <--- Per riconoscere chi lo ha creato
    @SerialName("author_name") val authorName: String,
    @SerialName("author_level") val authorLevel: Int,
    val title: String,
    val content: String,
    @SerialName("has_image") val hasImage: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("liked_by") val likedBy: List<String> = emptyList() // <--- Lista di chi ha messo like
)

val SfondoCommunity = Color(0xFFE8F5E9)
val ColoreCardPost = Color(0xFFC5E1A5)

@Composable
fun CommunityScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var currentUserId by remember { mutableStateOf<String?>(null) }

    var isCreatingPost by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }

    fun fetchData() {
        scope.launch {
            try {
                // A. RECUPERO ID UTENTE LOGGATO
                val userId = supabase.client.auth.currentUserOrNull()?.id
                currentUserId = userId

                // B. SCARICA I POST
                val fetchedPosts = supabase.client.postgrest["community_posts"]
                    .select()
                    .decodeList<CommunityPost>()
                    .sortedByDescending { it.id }
                posts = fetchedPosts

                // C. SCARICA IL PROFILO DELL'UTENTE
                if (userId != null) {
                    currentUserProfile = supabase.client.postgrest["profili"]
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
    }

    LaunchedEffect(Unit) {
        fetchData()
    }

    BackHandler(enabled = isCreatingPost && !isUploading) { isCreatingPost = false }

    Box(modifier = Modifier.fillMaxSize().background(SfondoCommunity)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColoreCardPost)
            }
        } else if (isCreatingPost) {
            CreatePostView(
                isUploading = isUploading,
                onBack = { isCreatingPost = false },
                onPostCreated = { titolo, testo, fotoScelta ->
                    scope.launch {
                        isUploading = true
                        try {
                            var uploadedImageUrl: String? = null

                            if (fotoScelta != null) {
                                val inputStream = context.contentResolver.openInputStream(fotoScelta)
                                val bytes = inputStream?.readBytes()

                                if (bytes != null) {
                                    val fileName = "${UUID.randomUUID()}.jpg"
                                    supabase.client.storage["community_images"].upload(fileName, bytes)
                                    uploadedImageUrl = supabase.client.storage["community_images"].publicUrl(fileName)
                                }
                            }

                            val nomeAutore = currentUserProfile?.Username
                                ?: currentUserProfile?.NomeCognome
                                ?: "Utente Sconosciuto"

                            val livello = currentUserProfile?.Livelloautismo?.filter { it.isDigit() }?.toIntOrNull() ?: 1

                            // CREAZIONE CON SALVATAGGIO ID AUTORE
                            val newPost = CommunityPost(
                                authorId = currentUserId, // Salviamo chi lo ha creato!
                                authorName = nomeAutore,
                                authorLevel = livello,
                                title = titolo,
                                content = testo,
                                hasImage = uploadedImageUrl != null,
                                imageUrl = uploadedImageUrl,
                                authorAvatarUrl = currentUserProfile?.avatarUrl
                            )

                            supabase.client.postgrest["community_posts"].insert(newPost)

                            fetchData()
                            isCreatingPost = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isUploading = false
                        }
                    }
                }
            )
        } else {
            CommunityFeedView(
                posts = posts,
                currentUserId = currentUserId, // Passiamo l'ID corrente
                onAddPostClick = { isCreatingPost = true },
                onDeletePost = { postId ->
                    scope.launch {
                        try {
                            // ELIMINA DA SUPABASE
                            supabase.client.postgrest["community_posts"].delete { filter { eq("id", postId) } }
                            // AGGIORNA INTERFACCIA
                            posts = posts.filter { it.id != postId }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                onLikeToggle = { postId ->
                    val index = posts.indexOfFirst { it.id == postId }
                    val userId = currentUserId
                    if (index != -1 && userId != null) {
                        val currentPost = posts[index]
                        scope.launch {
                            try {
                                // CONTROLLA SE AVEVA GIA' MESSO LIKE
                                val alreadyLiked = currentPost.likedBy.contains(userId)

                                val newLikedBy = if (alreadyLiked) {
                                    currentPost.likedBy - userId // Rimuove like
                                } else {
                                    currentPost.likedBy + userId // Aggiunge like
                                }

                                val newLikesCount = newLikedBy.size

                                // AGGIORNA GRAFICAMENTE
                                val updatedPosts = posts.toMutableList()
                                updatedPosts[index] = currentPost.copy(
                                    likedBy = newLikedBy,
                                    likesCount = newLikesCount
                                )
                                posts = updatedPosts

                                // AGGIORNA SUL DATABASE
                                supabase.client.postgrest["community_posts"].update(
                                    {
                                        set("liked_by", newLikedBy)
                                        set("likes_count", newLikesCount)
                                    }
                                ) {
                                    filter { eq("id", postId) }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
    currentUserId: String?,
    onAddPostClick: () -> Unit,
    onDeletePost: (String) -> Unit,
    onLikeToggle: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (posts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Nessun post presente.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text("Sii il primo a scrivere nella community!", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 30.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        onDeleteClick = { onDeletePost(post.id) },
                        onLikeClick = { onLikeToggle(post.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddPostClick,
            containerColor = ColoreCardPost,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 90.dp, end = 24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuovo Post", tint = Color.Black)
        }
    }
}

@Composable
fun PostCard(post: CommunityPost, currentUserId: String?, onDeleteClick: () -> Unit, onLikeClick: () -> Unit) {
    val context = LocalContext.current

    // Controlla se il mio ID è dentro la lista dei "mi piace" scaricata dal database
    val isLikedByMe = currentUserId != null && post.likedBy.contains(currentUserId)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!post.authorAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = "Avatar Autore",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "livello ${post.authorLevel}", fontSize = 12.sp, color = Color.DarkGray)
            }

            // TASTO ELIMINA (Visibile solo se sei l'autore)
            if (currentUserId != null && post.authorId == currentUserId) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // TASTO CONDIVIDI
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

                if (post.imageUrl != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Immagine caricata",
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
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
            val buttonColor by animateColorAsState(targetValue = if (isLikedByMe) Color(0xFF7CB342) else ColoreCardPost, label = "")
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.background(buttonColor, CircleShape).size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Mi Piace",
                    modifier = Modifier.size(16.dp),
                    tint = if (isLikedByMe) Color.White else Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun CreatePostView(isUploading: Boolean, onBack: () -> Unit, onPostCreated: (String, String, Uri?) -> Unit) {
    var titolo by remember { mutableStateOf("") }
    var contenuto by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Column(modifier = Modifier.fillMaxSize().padding(top = 30.dp, start = 16.dp, end = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp), enabled = !isUploading) {
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
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    enabled = !isUploading
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
                    ),
                    enabled = !isUploading
                )

                if (selectedImageUri != null) {
                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Anteprima",
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.White, CircleShape).size(24.dp),
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Rimuovi", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ElevatedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (selectedImageUri != null) Color(0xFF7CB342) else Color.White
                        ),
                        enabled = !isUploading
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
                    onPostCreated(titolo, contenuto, selectedImageUri)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(150.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColoreCardPost),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Post", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}