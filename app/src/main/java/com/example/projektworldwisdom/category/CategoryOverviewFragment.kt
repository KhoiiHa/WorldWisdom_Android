package com.example.projektworldwisdom.category

import android.graphics.Rect
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.os.bundleOf
import kotlin.math.roundToInt

class CategoryOverviewFragment : Fragment(R.layout.fragment_category_overview) {

    companion object {
        // Key used to pass the selected category back to the previous screen (Collection).
        const val RESULT_SELECTED_CATEGORY = "selectedCategory"

        // Optional argument (from nav_graph) to know where this screen was opened from.
        const val ARG_ORIGIN = "origin"
        const val ORIGIN_HOME = "home"
        const val ORIGIN_COLLECTION = "collection"

        // Optional argument (from nav_graph) to preselect/scroll to a category.
        const val ARG_INITIAL_CATEGORY = "initialCategory"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: CategoryAdapter

    private var initialCategory: String = ""

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerCategories)
        emptyText = view.findViewById(R.id.textEmptyCategories)

        val origin = arguments?.getString(ARG_ORIGIN) ?: ORIGIN_HOME
        initialCategory = arguments?.getString(ARG_INITIAL_CATEGORY).orEmpty().trim()

        // Toolbar: Back like other screens
        view.findViewById<MaterialToolbar>(R.id.toolbarCategoryOverview)
            .setNavigationOnClickListener {
                // Use the real back stack (same behavior as system back)
                findNavController().navigateUp()
            }

        adapter = CategoryAdapter { category ->
            val navController = findNavController()
            if (category.isBlank()) return@CategoryAdapter

            // If this screen was opened from Collection, keep the existing behavior:
            // set the selected category as a result so Collection can apply its local filter.
            if (origin == ORIGIN_COLLECTION) {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(RESULT_SELECTED_CATEGORY, category)
            }

            // Navigate to quote list for this category.
            // Use destination id instead of action id to avoid crashes if the action is missing.
            navController.navigate(
                R.id.categoryQuotesFragment,
                bundleOf(
                    "category" to category,
                    "origin" to "overview"
                )
            )
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        // Spacing for the 2-column grid (keeps tiles visually separated).
        if (recyclerView.itemDecorationCount == 0) {
            recyclerView.addItemDecoration(GridSpacingDecoration(spanCount = 2, spacingPx = dp(12), includeEdge = false))
        }

        // MVP: Kategorien-Liste aus Resources (Deutsch).
        // Später können wir das dynamisch aus Daten ableiten.
        val categories = resources.getStringArray(R.array.categories_default).toList()
        render(categories)
    }

    private fun render(categories: List<String>) {
        val showEmpty = categories.isEmpty()
        emptyText.visibility = if (showEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (showEmpty) View.GONE else View.VISIBLE

        adapter.submitList(categories)

        // If we got an initial category (e.g., from Collection), scroll to it so users see context.
        if (initialCategory.isNotBlank()) {
            val index = categories.indexOfFirst { it.equals(initialCategory, ignoreCase = true) }
            if (index >= 0) {
                recyclerView.post { recyclerView.scrollToPosition(index) }
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    private class GridSpacingDecoration(
        private val spanCount: Int,
        private val spacingPx: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return

            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacingPx - column * spacingPx / spanCount
                outRect.right = (column + 1) * spacingPx / spanCount

                if (position < spanCount) {
                    outRect.top = spacingPx
                }
                outRect.bottom = spacingPx
            } else {
                outRect.left = column * spacingPx / spanCount
                outRect.right = spacingPx - (column + 1) * spacingPx / spanCount

                if (position >= spanCount) {
                    outRect.top = spacingPx
                }
            }
        }
    }
}