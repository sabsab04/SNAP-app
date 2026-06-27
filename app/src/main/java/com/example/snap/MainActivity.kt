package com.example.snap

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Import fondamentale per far funzionare l'Auth
import io.github.jan.supabase.gotrue.auth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Troviamo il layout per applicare l'arcobaleno
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- SFONDO ARCOBALENO ---
        val rainbowGradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                0xFFFF8A80.toInt(), // Rosso
                //0xFFFF7F00.toInt(), // Arancione
                0xFFFFF080.toInt(), // Giallo
                0xFF80FFD4.toInt(), // Verde
                //0xFF0000FF.toInt(), // Blu
                0xFFCC99FF.toInt(), // Indaco
                //0xFF9400D3.toInt()  // Violetto
            )
        )
        mainLayout.background = rainbowGradient

        // --- TIMER PER IL PASSAGGIO AUTOMATICO ---
        Handler(Looper.getMainLooper()).postDelayed({
            // Usiamo lifecycleScope per eseguire la verifica su Supabase
            lifecycleScope.launch {

                // LA MODIFICA CHIAVE: aggiunta di ".client"
                val session = supabase.client.auth.currentSessionOrNull()

                if (session != null) {
                    // L'utente è loggato: vai alla tua HomeActivity
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // Non è loggato: vai al login (MainActivity2)
                    val intent = Intent(this@MainActivity, MainActivity2::class.java)
                    startActivity(intent)
                }

                // Chiudiamo la MainActivity in ogni caso
                finish()
            }
        }, 5000)
    }
}