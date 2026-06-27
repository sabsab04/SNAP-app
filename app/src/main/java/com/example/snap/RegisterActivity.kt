package com.example.snap

import android.content.Intent
import android.os.Bundle
import android.view.View // <--- AGGIUNTO
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment // <--- AGGIUNTO
import androidx.fragment.app.FragmentManager // <--- AGGIUNTO

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carica il layout della registrazione (che ora contiene il FragmentContainerView)
        setContentView(R.layout.register_main)

        // --- NASCONDI FRASE IN UTENTE E PSICOLOGO ---
        // Trova il LinearLayout della scritta in fondo
        val linearLayout2 = findViewById<View>(R.id.linearLayout2)

        // Ascolta quale fragment è attualmente visibile sullo schermo
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)

                // Se siamo nella registrazione finale di Utente o Psicologo, nascondi la scritta
                if (f is RegistratiUtenteFragment || f is RegistratiPsicologoFragment) {
                    linearLayout2?.visibility = View.GONE
                } else {
                    // Nelle altre pagine (credenziali e scelta ruolo) la mostra normalmente
                    linearLayout2?.visibility = View.VISIBLE
                }
            }
        }, false)
        // --------------------------------------------

        // --- PASSO 3: CARICA IL PRIMO FRAGMENT ALL'AVVIO ---
        // Controlla che il fragment venga inserito solo la prima volta che si apre la schermata
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.register_fragment_container, RegistratiFragment())
                .commit()
        }
        // ---------------------------------------------------

        // Manteniamo il tuo codice per andare alla schermata di Login
        val btnGoToLogin = findViewById<TextView>(R.id.btnRegistrati)

        btnGoToLogin.setOnClickListener {
            // Crea un Intent per avviare l'Activity del Login (MainActivity2)
            val intent = Intent(this, MainActivity2::class.java)

            // Avvia la nuova attività
            startActivity(intent)

            // Chiude l'activity corrente
            finish()
        }
    }
}