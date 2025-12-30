package com.example.projektworldwisdom.category

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

class CategoryAdapter(
    private val onCategoryClick: (String) -> Unit
) : ListAdapter<String, CategoryAdapter.CategoryViewHolder>(DIFF) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val context = parent.context

        val card = MaterialCardView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            radius = dp(context, 14f)
            cardElevation = dp(context, 2f)
            useCompatPadding = true
            isClickable = true
            isFocusable = true
            setContentPadding(
                dpInt(context, 14f),
                dpInt(context, 14f),
                dpInt(context, 14f),
                dpInt(context, 14f)
            )
        }

        val title = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        card.addView(title)
        return CategoryViewHolder(card, title, onCategoryClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val card: MaterialCardView,
        private val title: TextView,
        private val onCategoryClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(card) {

        fun bind(category: String) {
            title.text = category
            card.setOnClickListener { onCategoryClick(category) }
        }
    }

    private fun dp(context: Context, value: Float): Float =
        value * context.resources.displayMetrics.density

    private fun dpInt(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density).roundToInt()

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}