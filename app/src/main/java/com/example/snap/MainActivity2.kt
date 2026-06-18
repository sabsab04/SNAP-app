package com.example.snap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        // 1. Troviamo i componenti usando gli ID reali del tuo XML
        val etEmail = findViewById<EditText>(R.id.etEmailLogin)
        val etPassword = findViewById<EditText>(R.id.etPasswordLogin)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn) // Il tuo tasto "Sign In"
        val btnRegistrati = findViewById<TextView>(R.id.btnRegistrati) // Il tuo testo "Registrati"

        // Passaggio alla registrazione (Il tuo codice originale intatto)
        btnRegistrati.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 2. Gestione del Login reale con Supabase
        btnSignIn.setOnClickListener {
            val emailInserita = etEmail.text.toString().trim()
            val passwordInserita = etPassword.text.toString().trim()

            if (emailInserita.isEmpty() || passwordInserita.isEmpty()) {
                Toast.makeText(this, "Inserisci email e password per accedere!", Toast.LENGTH_SHORT).show()
            } else {

                lifecycleScope.launch {
                    try {
                        // Chiediamo l'accesso a Supabase Auth
                        val session = supabase.client.auth.signInWith(Email) {
                            email = emailInserita
                            password = passwordInserita
                        }

                        val userId = supabase.client.auth.currentUserOrNull()?.id

                        if (userId != null) {
                            // Leggiamo la riga specifica nella tabella profili per capire chi è entrato
                            val profiloUtente = supabase.client.postgrest["profili"]
                                .select(columns = Columns.ALL) {
                                    filter {
                                        eq("id", userId)
                                    }
                                }.decodeSingle<UserProfile>()

                            Toast.makeText(this@MainActivity2, "Bentornato, ${profiloUtente.Username}!", Toast.LENGTH_SHORT).show()

                            // Smistamento in base al ruolo dell'account
                            if (profiloUtente.Ruolo == "psicologo") {
                                val intentPsicologo = Intent(this@MainActivity2,
                                    RegistratiPsicologoFragment::class.java)
                                startActivity(intentPsicologo)
                            } else {
                                val intentUtente = Intent(this@MainActivity2, RegistratiPsicologoFragment::class.java)
                                startActivity(intentUtente)
                            }

                            finish() // Chiude la pagina di Login
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity2, "Credenziali errate o errore di rete!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Margini per gli schermi (Il tuo codice originale intatto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}