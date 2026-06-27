package com.example.snap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegistratiUtenteFragment : Fragment(R.layout.fragment_registrati_utente) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recuperiamo email e password passate dagli step precedenti
        val emailRicevuta = arguments?.getString("email_key") ?: ""
        val passwordRicevuta = arguments?.getString("password_key") ?: ""

        // Assicuriamoci che il titolo in alto sia "Registrati"
        val tvLogin = activity?.findViewById<TextView>(R.id.tvLogin)
        tvLogin?.text = "Registrati"

        // 2. Troviamo i componenti nel layout tramite i loro ID
        val etUsername = view.findViewById<EditText>(R.id.etUsername)

        // Spinner per il livello di autismo
        val etLivello = view.findViewById<Spinner>(R.id.etLivelloAutismo)
        val opzioniLivello = arrayOf("Livello 1(Autismo lieve)", "Livello 2(Austismo moderato)", "Livello 3(Autismo severo)")

        // Adattatore per lo Spinner (testo nero e sfondo bianco)
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, opzioniLivello) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val v = super.getView(position, convertView, parent) as TextView
                v.setTextColor(android.graphics.Color.BLACK)
                v.textSize = 16f
                return v
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent) as TextView
                v.setTextColor(android.graphics.Color.BLACK)
                v.setBackgroundColor(android.graphics.Color.WHITE)
                v.setPadding(32, 32, 32, 32)
                return v
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        etLivello.adapter = adapter

        val etVerbalita = view.findViewById<EditText>(R.id.etVerbalita)
        val etBio = view.findViewById<EditText>(R.id.etBiografiaUtente)
        val btnRegistrati = view.findViewById<Button>(R.id.btnRegistratiUtente)

        // 3. Gestiamo il click sul pulsante finale "Registrati"
        btnRegistrati.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val livello = etLivello.selectedItem.toString()
            val verbalita = etVerbalita.text.toString().trim()
            val bio = etBio.text.toString().trim()

            // Controllo se i campi principali sono compilati
            if (user.isEmpty() || verbalita.isEmpty()) {
                Toast.makeText(requireContext(), "Compila i campi principali!", Toast.LENGTH_SHORT).show()
            } else if (emailRicevuta.isEmpty() || passwordRicevuta.isEmpty()) {
                Toast.makeText(requireContext(), "Errore: Dati di accesso mancanti dal primo step!", Toast.LENGTH_SHORT).show()
            } else {

                // Avviamo la comunicazione con Supabase
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // A. Creiamo l'account utente su Supabase Auth
                        supabase.client.auth.signUpWith(Email) {
                            email = emailRicevuta
                            password = passwordRicevuta
                        }

                        // B. Recupero dell'ID generato in tempo reale
                        val userIdReale = supabase.client.auth.currentUserOrNull()?.id

                        if (userIdReale != null) {

                            // C. Costruiamo l'oggetto usando i tuoi NOMI CORRETTI (con le maiuscole)
                            val nuovoProfilo = UserProfile(
                                id = userIdReale,
                                Email = emailRicevuta,
                                Ruolo = "utente",
                                Username = user,
                                Biografia = bio,
                                Livelloautismo = livello,
                                Verbalita = verbalita,
                                NomeCognome = null,
                                Mansione = null,
                                Titolodistudio = null
                            )

                            // D. Salva i dati nella tabella 'profili' su Supabase
                            supabase.client.postgrest["profili"].insert(nuovoProfilo)

                            Toast.makeText(requireContext(), "Registrazione Utente Completata!", Toast.LENGTH_SHORT).show()

                            // Ritorniamo alla schermata di Login (MainActivity2) pulendo la coda
                            val intent = Intent(requireActivity(), MainActivity2::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "Errore nella generazione dell'ID utente", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Usiamo e.message che di solito contiene il codice HTTP e il dettaglio del database
                        Toast.makeText(requireContext(), "Errore HTTP: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}