package com.example.snap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

data class CentroLocale(
    val nome: String,
    val indirizzo: String,
    val lat: Double,
    val lng: Double,
    val rating: Int
)

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    // --- DATI SPARSI PER L'ITALIA ---
    val listaCentri = listOf(
        // Marche
        CentroLocale("Lega del Filo d'Oro", "Via Montecerno 1, Osimo", 43.4862, 13.4820, 5),
        CentroLocale("Centro Regionale Autismo", "Via Conca 71, Ancona", 43.6053, 13.4357, 4),
        // Lombardia
        CentroLocale("Centro Autismo Milano", "Via Pergolesi 8, Milano", 45.4845, 9.2045, 5),
        CentroLocale("Polo Diagnostico", "Viale Europa 12, Brescia", 45.5415, 10.2118, 3),
        // Lazio
        CentroLocale("Polo Autismo Roma", "Via dei Sabelli 108, Roma", 41.8985, 12.5134, 4),
        // Campania
        CentroLocale("Centro Neapolis", "Via Posillipo 22, Napoli", 40.8214, 14.2052, 4),
        // Emilia-Romagna
        CentroLocale("Clinica San Luca", "Via Mazzini 15, Bologna", 44.4891, 11.3556, 5)
    )

    var centroSelezionato by remember { mutableStateOf<CentroLocale?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Controlla se abbiamo già i permessi GPS
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanciatore per il pop-up dei permessi
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    // All'avvio, chiedi i permessi se non li abbiamo
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Posizione di default della telecamera (Italia intera)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.9028, 12.4964), 5.5f)
    }

    // Quando otteniamo i permessi, cerchiamo la posizione e animiamo la telecamera
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation = latLng

                    // Fa "volare" la telecamera sulla posizione dell'utente
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(latLng, 11f),
                            durationMs = 1500
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- MAPPA GOOGLE NATIVA ---
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 130.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            listaCentri.forEach { centro ->
                Marker(
                    state = MarkerState(position = LatLng(centro.lat, centro.lng)),
                    title = centro.nome,
                    snippet = centro.indirizzo,
                    onClick = {
                        centroSelezionato = centro
                        false
                    }
                )
            }
        }

        // --- LA TUA CARD VIOLA IN BASSO ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    color = Color(0xFFEADBFF),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = centroSelezionato?.nome ?: "Esplora i Centri",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                        }
                        Text(
                            text = centroSelezionato?.indirizzo ?: "Tocca un segnalino per i dettagli",
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD08CFF))
                            .clickable {
                                centroSelezionato?.let { centro ->
                                    val uri = Uri.parse("google.navigation:q=${centro.lat},${centro.lng}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Naviga", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ratingScelto = centroSelezionato?.rating ?: 0
                    repeat(ratingScelto) {
                        Icon(Icons.Outlined.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                    }
                    repeat(5 - ratingScelto) {
                        Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }

                // Lasciamo un po' di margine in basso per chiudere la card in modo elegante
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}