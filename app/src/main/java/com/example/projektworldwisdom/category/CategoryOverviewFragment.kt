package com.example.projektworldwisdom.category

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.os.bundleOf

class CategoryOverviewFragment : Fragment(R.layout.fragment_category_overview) {

    companion object {
        // Key used to pass the selected category back to the previous screen (Collection).
        const val RESULT_SELECTED_CATEGORY = "selectedCategory"

        // Optional argument (from nav_graph) to know where this screen was opened from.
        const val ARG_ORIGIN = "origin"
        const val ORIGIN_HOME = "home"
        const val ORIGIN_COLLECTION = "collection"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerCategories)
        emptyText = view.findViewById(R.id.textEmptyCategories)

        val origin = arguments?.getString(ARG_ORIGIN) ?: ORIGIN_HOME

        // Toolbar: Back like other screens
        view.findViewById<MaterialToolbar>(R.id.toolbarCategoryOverview)
            .setNavigationOnClickListener {
                navigateBackToOrigin(origin)
            }

        adapter = CategoryAdapter { category ->
            val navController = findNavController()

            // If this screen was opened from Collection, keep the existing behavior:
            // set the selected category as a result so Collection can apply its local filter.
            if (origin == ORIGIN_COLLECTION) {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(RESULT_SELECTED_CATEGORY, category)
            }

            // Decision A: open the quote list for this category.
            navController.navigate(
                R.id.action_categoryOverviewFragment_to_categoryQuotesFragment,
                bundleOf(
                    "category" to category,
                    "origin" to "overview"
                )
            )
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        // MVP: Kategorien-Liste lokal (damit der Screen sofort funktioniert).
        // Später können wir das wieder dynamisch aus dem SharedViewModel/Quotes ableiten.
        val categories = listOf(
            "Gesellschaft",
            "Erfolg",
            "Arbeit",
            "Wissen",
            "Freiheit",
            "Liebe",
            "Philosophie"
        )
        render(categories)
    }
    private fun navigateBackToOrigin(origin: String) {
        val navController = findNavController()

        // Prefer popping back to the known origin destination (no duplicate screens).
        val destinationId = when (origin) {
            ORIGIN_HOME -> R.id.homeFragment
            ORIGIN_COLLECTION -> R.id.collectionFragment
            else -> null
        }

        val popped = destinationId?.let { navController.popBackStack(it, false) } ?: false

        // Fallback: just go back one step.
        if (!popped) {
            navController.popBackStack()
        }
    }

    private fun render(categories: List<String>) {
        val showEmpty = categories.isEmpty()
        emptyText.visibility = if (showEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (showEmpty) View.GONE else View.VISIBLE

        adapter.submitList(categories)
    }
}