package com.example.skincure.utils

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class LoadImage {
    companion object {

        fun load(
            context: Context,
            imageView: ImageView?,
            imageUrl: String,
            placeholder: Int,
            isCircle: Boolean = false
        ) {
            if (imageView == null) return

            val placeholderDrawable = ColorDrawable(ContextCompat.getColor(context, placeholder))

            if (imageUrl.isBlank()) {
                imageView.setImageDrawable(placeholderDrawable)
                return
            }

            val picassoBuilder = Picasso.get()
                .load(imageUrl)
                .placeholder(placeholderDrawable)
                .error(placeholderDrawable)
                .fit()
                .centerCrop()

            if (isCircle) {
                picassoBuilder.transform(CropCircleTransformation())
            } else {
                picassoBuilder.transform(RoundedCornersTransformation(12, 0))
            }

            picassoBuilder.into(imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                }

                override fun onError(e: Exception?) {
                }
            })
        }
    }
}