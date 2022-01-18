package com.planscollective.plansapp.extension

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.planscollective.plansapp.R
import java.io.File

fun ImageView.setImage(urlString: String? = null,
                       placeHolder: Int? = null,
                       defaultImage: Int? = null,
                       skipMemoryCache: Boolean = false,
                       complete: ((success: Boolean?) -> Unit)? = null
) {
    val placeHolderId = placeHolder ?: defaultImage
    if (!urlString.isNullOrEmpty()) {

        var requestBuilder = Glide.with(this).load(urlString)

        if (skipMemoryCache || File(urlString).exists()) {
            requestBuilder = requestBuilder.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        }

        if (placeHolderId != null) {
            requestBuilder = requestBuilder.placeholder(placeHolderId)
        }

        if (complete != null) {
            requestBuilder = requestBuilder.listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    complete(false)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    complete(true)
                    return false
                }
            })
        }
        requestBuilder.into(this)
    }else {
        if (defaultImage != null) {
            setImageResource(defaultImage)
        }else if (placeHolder != null){
            setImageResource(placeHolder)
        }
        if (complete != null) {
            complete(true)
        }
    }
}

fun ImageView.setUserImage(urlString: String? = null,
                           placeHolder: Int? = null,
                           defaultImage: Int? = null,
                           skipMemoryCache: Boolean = false,
                           complete: ((success: Boolean?) -> Unit)? = null
){
    val placeHolderId = placeHolder ?: R.drawable.ic_user_placeholder
    val defaultImageId = defaultImage ?: R.drawable.ic_user_default
    setImage(urlString, placeHolderId, defaultImageId, skipMemoryCache, complete)
}

fun ImageView.setEventImage(urlString: String? = null,
                           placeHolder: Int? = null,
                           defaultImage: Int? = null,
                            skipMemoryCache: Boolean = false,
                           complete: ((success: Boolean?) -> Unit)? = null
){
    val placeHolderId = placeHolder ?: R.drawable.bkgnd_gray_loading
    setImage(urlString, placeHolderId, defaultImage, skipMemoryCache, complete)
}

