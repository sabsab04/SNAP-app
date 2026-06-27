package com.example.snap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class registraticomeFragment : Fragment(R.layout.fragment_registraticome) {

    private var ruoloSelezionato: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Aggiorna il testo nell'Activity principale
        val tvLogin = activity?.findViewById<TextView>(R.id.tvLogin)
        tvLogin?.text = "Registrati come:"

        // 2. Riferimenti ai bottoni del layout
        val btnPsicologo = view.findViewById<Button>(R.id.btnPsicologo)
        val btnUtente = view.findViewById<Button>(R.id.btnUtente)
        val btnNextPage = view.findViewById<Button>(R.id.btnNextPage)

        // 3. Listener per la selezione del ruolo
        btnPsicologo.setOnClickListener {
            ruoloSelezionato = "psicologo"
            Toast.makeText(requireContext(), "Hai selezionato: Psicologo", Toast.LENGTH_SHORT).show()
        }

        btnUtente.setOnClickListener {
            ruoloSelezionato = "utente"
            Toast.makeText(requireContext(), "Hai selezionato: Utente", Toast.LENGTH_SHORT).show()
        }

        // 4. Click sul tasto Avanti
        btnNextPage.setOnClickListener {
            if (ruoloSelezionato == null) {
                Toast.makeText(requireContext(), "Seleziona una delle due opzioni!", Toast.LENGTH_SHORT).show()
            } else {

                // AGGIUNTO: Recuperiamo il pacchetto con email e password arrivato dallo Step 1
                val bundleRicevuto = arguments

                // Scegliamo quale classe .kt di Fragment istanziare
                val prossimoFragment: Fragment = if (ruoloSelezionato == "psicologo") {
                    RegistratiPsicologoFragment()
                } else {
                    RegistratiUtenteFragment()
                }

                // AGGIUNTO: Attacchiamo lo stesso identico pacchetto al nuovo fragment che sta per aprirsi
                prossimoFragment.arguments = bundleRicevuto

                // Eseguiamo il cambio del Fragment sul container dell'Activity
                parentFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, prossimoFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}