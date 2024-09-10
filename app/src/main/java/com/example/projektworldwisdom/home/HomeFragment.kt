package com.example.projektworldwisdom.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote





class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModels()

    //    private val viewModel: HomeViewModel by hiltViewModel() {
//        val sharedPrefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        HomeViewModelFactory(sharedPrefs)
//    }
    private lateinit var binding: FragmentHomeBinding
    private lateinit var quoteAdapter: QuoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Lade alle Zitate beim Start des Fragments
        viewModel.loadQuotes()

        // Ladeanzeige standardmäßig anzeigen
        viewModel?.isLoading?.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // RecyclerView einrichten
        quoteAdapter = QuoteAdapter(emptyList())
        binding.quotesList?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = quoteAdapter
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireActivity().getSharedPreferences(
            "app_prefs",
            Context.MODE_PRIVATE
        )
        viewModel.setSharedPrefs(sharedPrefs) // Methode im ViewModel erstellen

        // ...


        // LiveData beobachten und Adapter aktualisieren
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            quoteAdapter.updateQuotes(quotes)
        }

//        viewModel.initializeData()

        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorSlug = quote.authorSlug
                val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(authorSlug)
                findNavController().navigate(action)
            }
        })



//        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
//            override fun onItemClick(quote: Quote) {
//                viewModel._selectedAuthorSlug.value = quote.authorSlug
//                findNavController().navigate(R.id.action_homeFragment_to_authorDetailsFragment)
//            }
//        })

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nicht benötigt
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchQuotes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Nicht benötigt
            }
        })

//        viewModel.dailyAffirmation.observe(viewLifecycleOwner) { quote ->
//            quote?.let {
//                binding.affirmationText.text = it.content
//                binding.affirmationAuthor.text = "- ${it.author}"
//            } ?: run {
//                // Handle den Fall, dass kein Zitat geladen wurde (optional)
//                binding.affirmationText.text = "Keine Zitate gefunden."
//                binding.affirmationAuthor.text = ""
//            }
//        }


        // Klick-Listener für Filter
        binding.filterSociety.setOnClickListener {
            viewModel.loadQuotesByTag("society")
        }

        binding.filterSuccess.setOnClickListener {
            viewModel.loadQuotesByTag("success")
        }

        binding.filterWork.setOnClickListener {
            viewModel.loadQuotesByTag("work")
        }

        binding.filterWisdom.setOnClickListener {
            viewModel.loadQuotesByTag("wisdom")
        }

        binding.filterGratitude.setOnClickListener {
            viewModel.loadQuotesByTag("gratitude")
        }

        binding.filterAlle.setOnClickListener {
            viewModel.loadQuotes()
        }

        // Fehlerbehandlung
        viewModel?.error?.observe(viewLifecycleOwner)
        { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()

                viewModel?._error?.postValue(null)
            }
        }


    }
}
