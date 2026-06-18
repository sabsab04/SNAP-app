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
                0xFFFF0000.toInt(), // Rosso
                0xFFFF7F00.toInt(), // Arancione
                0xFFFFFF00.toInt(), // Giallo
                0xFF00FF00.toInt(), // Verde
                0xFF0000FF.toInt(), // Blu
                0xFF4B0082.toInt(), // Indaco
                0xFF9400D3.toInt()  // Violetto
            )
        )
        mainLayout.background = rainbowGradient

        // --- TIMER PER IL PASSAGGIO AUTOMATICO ---
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)

            // Chiudiamo la MainActivity così l'utente non può tornarci
            // premendo il tasto "indietro" del telefono
            finish()
        }, 5000)
    }
}