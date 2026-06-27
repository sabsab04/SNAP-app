package com.example.snap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegistratiPsicologoFragment : Fragment(R.layout.fragment_registrati_psicologo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperiamo email e password passate dagli step precedenti
        val emailRicevuta = arguments?.getString("email_key") ?: ""
        val passwordRicevuta = arguments?.getString("password_key") ?: ""

        val tvLogin = activity?.findViewById<TextView>(R.id.tvLogin)
        tvLogin?.text = "Registrati"

        // Riferimenti ai componenti grafici dello psicologo
        val etNomeCognome = view.findViewById<EditText>(R.id.etNomeCognome)
        val etMansione = view.findViewById<EditText>(R.id.etMansione)
        val etTitoloStudio = view.findViewById<EditText>(R.id.etTitoloStudio)
        val etBiografia = view.findViewById<EditText>(R.id.etBiografia)
        val btnRegistratiFinal = view.findViewById<Button>(R.id.btnRegistratiFinal)

        // Click finale sul tasto "Registrati"
        btnRegistratiFinal.setOnClickListener {
            val nome = etNomeCognome.text.toString().trim()
            val mansione = etMansione.text.toString().trim()
            val titolo = etTitoloStudio.text.toString().trim()
            val bio = etBiografia.text.toString().trim()

            if (nome.isEmpty() || mansione.isEmpty() || titolo.isEmpty() || bio.isEmpty()) {
                Toast.makeText(requireContext(), "Per favore, compila tutti i campi!", Toast.LENGTH_SHORT).show()
            } else if (emailRicevuta.isEmpty() || passwordRicevuta.isEmpty()) {
                Toast.makeText(requireContext(), "Errore: Dati di accesso mancanti dal primo step!", Toast.LENGTH_SHORT).show()
            } else {

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // 1. Registrazione su Supabase Auth (crea l'account in Authentication)
                        supabase.client.auth.signUpWith(Email) {
                            email = emailRicevuta
                            password = passwordRicevuta
                        }

                        // 2. CORREZIONE: Recupero dell'ID in modo sicuro e diretto da Supabase
                        val userIdReale = supabase.client.auth.currentUserOrNull()?.id

                        if (userIdReale != null) {
                            // 3. Prepariamo il profilo Psicologo seguendo i tuoi nomi precisi
                            val nuovoProfilo = UserProfile(
                                id = userIdReale,
                                Email = emailRicevuta,
                                Ruolo = "psicologo",
                                Username = nome,
                                NomeCognome = nome,
                                Biografia = bio,
                                Mansione = mansione,
                                Titolodistudio = titolo,
                                Livelloautismo = null,
                                Verbalita = null
                            )

                            // 4. Salva sul database nella tabella 'profili'
                            supabase.client.postgrest["profili"].insert(nuovoProfilo)

                            Toast.makeText(requireContext(), "Registrazione completata con successo!", Toast.LENGTH_SHORT).show()

                            // Torna al Login azzerando la coda delle schermate
                            val intent = Intent(requireActivity(), MainActivity2::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "Errore nella generazione dell'ID utente", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Mostra il VERO errore se qualcosa fallisce sul DB
                        Toast.makeText(requireContext(), "Errore reale: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}