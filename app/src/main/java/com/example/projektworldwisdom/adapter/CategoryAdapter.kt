package com.example.projektworldwisdom.category

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlin.math.roundToInt

class CategoryAdapter(
    private val onCategoryClick: (String) -> Unit
) : ListAdapter<String, CategoryAdapter.CategoryViewHolder>(DIFF) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val context = parent.context

        val margin = dpInt(context, 8f)
        val padding = dpInt(context, 14f)

        val card = MaterialCardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(margin, margin, margin, margin)
            }

            radius = dp(context, 14f)
            cardElevation = dp(context, 1.5f)
            useCompatPadding = true

            isClickable = true
            isFocusable = true

            // Material3-ish surface + subtle outline
            setCardBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainer, 0))
            strokeWidth = dpInt(context, 1f)
            setStrokeColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant, 0))

            setContentPadding(padding, padding, padding, padding)
        }

        val title = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Material3 typography (TitleMedium) for a clean, premium look
            TextViewCompat.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0))

            // Slightly tighter, consistent scaling
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

            gravity = Gravity.CENTER
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
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

        private var boundCategory: String? = null

        init {
            card.setOnClickListener {
                boundCategory?.let(onCategoryClick)
            }
        }

        fun bind(category: String) {
            boundCategory = category
            title.text = category.trim()
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