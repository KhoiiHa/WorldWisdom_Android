package com.example.projektworldwisdom.ui


import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.adapter.QuotesAdapter
import com.example.projektworldwisdom.databinding.FragmentAllQuotesBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class AllQuotesFragment : Fragment() {
    private lateinit var binding: FragmentAllQuotesBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: QuotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter initialisieren (nur einmal)
        adapter = QuotesAdapter(emptyList(), sharedViewModel) { quote ->
            // Navigiere zu den Details des Autors
            navigateToAuthorDetails(quote)
        }
        binding.allQuotesList.adapter = adapter

        // Zitate abrufen
        sharedViewModel.getQuotes()

        // Beobachte die Zitate
        sharedViewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            adapter.updateQuotes(quotes) // Setze alle Zitate
        }

        // Beobachte die gefilterten Zitate
        sharedViewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            adapter.updateQuotes(filteredQuotes) // Aktualisiere den Adapter mit gefilterten Zitaten
        }

        // Suchleiste
        binding.searchEditText.addTextChangedListener { text ->
            val searchQuery = text.toString()
            sharedViewModel.filterQuotesForAll(searchQuery) // Aufruf der Filtermethode im SharedViewModel
        }

        // Dynamische Filterbuttons hinzufügen
        addFilterButtons()
    }

    private fun addFilterButtons() {
        // Definiere die Keywords für die Filter
        val keywords = listOf(
            "Motivation", "Inspiration", "Humor", "Leben", "Erfolg", "Veränderung", "Frieden", "Gestaltung", "Rache", "Glaube",
            "Zweifel", "Träume", "Verständnis", "Kreativität", "Intelligenz", "Chancen", "Freiheit", "Produktivität", "Verantwortung",
            "Vorbereitung", "Weg", "Ziel", "Schwierigkeiten", "Möglichkeiten", "Kommunikation"
        )
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.robotoslab)

        // Für jedes Keyword einen Button dynamisch erstellen
        for (keyword in keywords) {
            val button = Button(requireContext())
            button.setPadding(16, 8, 16, 8) // Optionales Padding für die Buttons

            // Setze die benutzerdefinierte Schriftart für den Button
            button.typeface = typeface
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.keywordsfilter_lavender))

            // SpannableString für jedes Keyword anwenden
            val spannable = SpannableString(keyword)
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.filter_grau_dunkel)),
                0, keyword.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


            // Setze den formatierten Text auf den Button
            button.text = spannable

            // LayoutParams mit Margins erstellen
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginStart = 8 // 8dp Abstand links
            params.marginEnd = 8 // 8dp Abstand rechts
            button.layoutParams = params

            // Listener für Klick auf den Button, um das Keyword als Filter anzuwenden
            button.setOnClickListener {
                applyFilter(keyword) // Aufruf der Methode, um den Filter anzuwenden
            }

            // Füge den Button dem LinearLayout im HorizontalScrollView hinzu
            binding.filterContainer.addView(button)
        }

        // Gravity des LinearLayout auf CENTER setzen
        binding.filterContainer.gravity = Gravity.CENTER
    }

    private fun applyFilter(keyword: String) {
        // Aufruf der Filtermethode im SharedViewModel
        sharedViewModel.filterQuotesByCategory(keyword).observe(viewLifecycleOwner) { filteredQuotes ->
            adapter.updateQuotes(filteredQuotes) // Aktualisiere den Adapter mit gefilterten Zitaten
        }
        // Optionale Rückmeldung hinzufügen
        Toast.makeText(requireContext(), "Filter angewendet: $keyword", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        // Setze den ausgewählten Autor im SharedViewModel
        sharedViewModel.selectQuote(quote)
        sharedViewModel.selectAuthor(quote.author)
        val action = AllQuotesFragmentDirections.actionAllQuotesFragmentToAuthorDetailsFragment()
        findNavController().navigate(action)
    }

}