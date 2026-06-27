package com.example.snap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText // <-- AGGIUNTO
import android.widget.Toast    // <-- AGGIUNTO
import androidx.fragment.app.Fragment

class RegistratiFragment : Fragment(R.layout.fragment_registrati) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recuperiamo i campi di testo (Assicurati che gli ID siano corretti rispetto al tuo XML)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)       // Cambia l'ID se nel tuo XML è diverso
        val etPassword = view.findViewById<EditText>(R.id.etPassword) // Cambia l'ID se nel tuo XML è diverso
        val btnNext = view.findViewById<Button>(R.id.btnSignIn) // Il tuo bottone "Next page"

        btnNext.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci email e password!", Toast.LENGTH_SHORT).show()
            } else {
                // 2. Creiamo il pacchetto dati
                val bundle = Bundle()
                bundle.putString("email_key", email)
                bundle.putString("password_key", password)

                // 3. Creiamo il frammento successivo e gli attacchiamo il pacchetto
                val prossimoFragment = registraticomeFragment()
                prossimoFragment.arguments = bundle

                // 4. Cambiamo schermata
                parentFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, prossimoFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}