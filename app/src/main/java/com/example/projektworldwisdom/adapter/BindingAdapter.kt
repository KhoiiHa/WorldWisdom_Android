package com.example.projektworldwisdom.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.projektworldwisdom.R

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(url)
            .into(view)
    } else {
        view.setImageResource(R.drawable.ic_launcher_background) // Dein Fallback-Bild
    }
}