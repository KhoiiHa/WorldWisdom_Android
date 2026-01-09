package com.example.projektworldwisdom.category

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private val onCategoryClick: (String) -> Unit
) : ListAdapter<String, CategoryAdapter.CategoryViewHolder>(DIFF) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        val card = view.findViewById<MaterialCardView>(R.id.categoryCard)
        val title = view.findViewById<TextView>(R.id.tvCategoryTitle)

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

        private var boundCategory: String? = null

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val category = boundCategory?.trim().orEmpty()
                if (category.isBlank()) return@setOnClickListener

                onCategoryClick(category)
            }
        }

        fun bind(category: String) {
            val trimmed = category.trim()
            boundCategory = trimmed
            title.text = trimmed
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}