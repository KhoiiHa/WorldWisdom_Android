package com.example.projektworldwisdom.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentCollectionBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class CollectionFragment : Fragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    // Gemeinsames ViewModel (liefert aktuell die Quote-Liste aus der API)
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var quoteAdapter: QuoteAdapter

    private var latestFavorites: List<Quote> = emptyList()
    private var latestError: String? = null
    private var latestLoading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupActions()

        setupObservers()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onFavoriteClick = { quote: Quote ->
                // In Favorites-Screen bedeutet ⭐-Klick: entfernen/hinzufügen (toggle)
                sharedViewModel.toggleFavorite(quote)
            }
        )

        binding.recyclerViewCollection.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }
    }

    private fun setupActions() {
        // Empty CTA → zurück zu Home (Entdecken)
        binding.btnEmptyCta.setOnClickListener {
            navigateToHome()
        }

        // Retry → nochmal den aktuellen Zustand auswerten
        // (Später: ViewModel.triggerReload())
        binding.btnRetry.setOnClickListener {
            retryRender()
        }
    }

    private fun setupObservers() {
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            latestLoading = isLoading == true
            renderFromState()
        }

        sharedViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            latestError = errorMsg
            renderFromState()
        }

        sharedViewModel.favoriteQuotes.observe(viewLifecycleOwner) { favorites ->
            latestFavorites = favorites.orEmpty()
            if (latestFavorites.isNotEmpty()) {
                latestError = null
            }
            renderFromState()
        }
    }

    private fun renderFromState() {
        when {
            latestLoading -> {
                renderLoading()
            }
            latestFavorites.isNotEmpty() -> {
                renderContent(latestFavorites)
            }
            latestError != null -> {
                renderError(latestError)
            }
            else -> {
                renderEmpty()
            }
        }
    }

    private fun retryRender() {
        latestError = null
        sharedViewModel.clearError()
        sharedViewModel.loadQuotes()
    }

    private fun navigateToHome() {
        val navController = findNavController()

        // Prefer popping back to Home if it exists in the back stack.
        val popped = navController.popBackStack(R.id.homeFragment, false)
        if (!popped) {
            navController.navigate(R.id.homeFragment)
        }
    }

    private fun setUiState(
        showContent: Boolean,
        showEmpty: Boolean,
        showLoading: Boolean,
        showError: Boolean
    ) {
        binding.recyclerViewCollection.isVisible = showContent
        binding.layoutEmptyState.isVisible = showEmpty
        binding.progressLoading.isVisible = showLoading
        binding.layoutErrorState.isVisible = showError
    }

    // --- UI State Rendering ---

    private fun renderLoading() {
        setUiState(
            showContent = false,
            showEmpty = false,
            showLoading = true,
            showError = false
        )

        // Reset any previous error message (prevents stale text after leaving error state)
        binding.tvErrorSubtitle.setText(R.string.common_error_subtitle)
    }

    private fun renderEmpty() {
        setUiState(
            showContent = false,
            showEmpty = true,
            showLoading = false,
            showError = false
        )

        // Reset any previous error message
        binding.tvErrorSubtitle.setText(R.string.common_error_subtitle)

        quoteAdapter.updateQuotes(emptyList())
    }

    private fun renderError(message: String?) {
        setUiState(
            showContent = false,
            showEmpty = false,
            showLoading = false,
            showError = true
        )

        val msg = message?.takeIf { it.isNotBlank() }
        if (msg != null) {
            binding.tvErrorSubtitle.text = msg
        } else {
            binding.tvErrorSubtitle.setText(R.string.common_error_subtitle)
        }

        quoteAdapter.updateQuotes(emptyList())
    }

    private fun renderContent(quotes: List<Quote>) {
        setUiState(
            showContent = true,
            showEmpty = false,
            showLoading = false,
            showError = false
        )

        // Reset any previous error message
        binding.tvErrorSubtitle.setText(R.string.common_error_subtitle)

        quoteAdapter.updateQuotes(quotes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}