package com.esatan.simplelist.view

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("loadImage", "loadErrorImage")
fun loadImage(view: ImageView, url: String, @DrawableRes errorRes: Int? = null) {
    Glide.with(view)
        .load(url)
        .error(errorRes)
        .into(view)
}