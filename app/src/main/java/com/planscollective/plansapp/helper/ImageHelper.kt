package com.planscollective.plansapp.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.extension.toPx
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Float.min
import kotlin.math.roundToInt





object ImageHelper {
    fun setImage(imageView: ImageView, url: String?, placeholderResId: Int? = null) {
        if (url.isNullOrEmpty()) {
            return
        }

        try {
            val file = File(url)
            if (file.exists()) {
                if (placeholderResId != null) {
                    Glide.with(imageView.context).load(file).placeholder(placeholderResId).into(imageView)
                } else {
                    Glide.with(imageView.context).load(file).into(imageView)
                }
            } else {
                if (placeholderResId != null) {
                    Glide.with(imageView.context).load(url).placeholder(placeholderResId).into(imageView)
                } else {
                    Glide.with(imageView.context).load(url).into(imageView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (placeholderResId != null) {
                imageView.setImageResource(placeholderResId)
            }
        }
    }

    fun saveBitmap(bitmap: Bitmap?, filePath: String?): Boolean {
        if (bitmap == null) return false
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(filePath)
            bitmap.compress(
                Bitmap.CompressFormat.PNG /* PNG*/,
                100,
                out
            ) // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun getExifOrientation(filePath: String?) : Float {
        return if (filePath.isNullOrEmpty()) 0f else {
            val ei = ExifInterface(filePath)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            when (orientation) {
                ExifInterface.ORIENTATION_UNDEFINED,
                ExifInterface.ORIENTATION_NORMAL,
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
                ExifInterface.ORIENTATION_FLIP_VERTICAL,
                ExifInterface.ORIENTATION_TRANSPOSE,
                ExifInterface.ORIENTATION_TRANSVERSE -> 0f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f // Nexus 7 landscape...
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f // might need to flip horizontally too...
                else -> 90f // portrait
            }
        }
    }

    fun getBitmap(filePath: String?, inSampleSize: Int? = null): Bitmap? {
        if (filePath.isNullOrEmpty()) {
            return null
        }

        try {
            val options = BitmapFactory.Options()
            if (inSampleSize != null) options.inSampleSize = inSampleSize
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inDither = true

            val bitmap = BitmapFactory.decodeFile(filePath, options)
            val angle = getExifOrientation(filePath)
            var resultBitmap = bitmap
            if (angle != 0f) {
                val matrix = Matrix()
                matrix.postRotate(angle)
                resultBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
                bitmap.recycle()
            }

            return resultBitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun getResizedBitmap(filePath: String?, maxWidth: Int = 512, maxHeight: Int = 512): Bitmap? {
        if (filePath.isNullOrEmpty()) {
            return null
        }

        try {
            // First we get the the dimensions of the file on disk
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inDither = true
            BitmapFactory.decodeFile(filePath, options)

            // Next we calculate the ratio that we need to resize the image by
            // in order to fit the requested dimensions.
            val outHeight = options.outHeight
            val outWidth = options.outWidth
            var inSampleSize = 1

            if (outHeight > maxHeight || outWidth > maxWidth) {
                inSampleSize =
                    if (outWidth > outHeight) outHeight / maxHeight else outWidth / maxWidth
            }

            return getBitmap(filePath, inSampleSize)

        } catch (e: Exception) {
            return null
        }
    }

    fun saveVideoThumbnail(szVideoFilePath: String?, thumbFilePath: String?): Boolean {
        val bmThumbnail = getVideoThumbnail(szVideoFilePath) ?: return false
        return saveBitmap(bmThumbnail, thumbFilePath)
    }

    fun getVideoThumbnail(filePath: String?): Bitmap? {
        if (filePath.isNullOrEmpty()) return null
        return ThumbnailUtils.createVideoThumbnail(
            filePath,
            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
        )
    }

    fun convertBitmapToBytes(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        bitmap.recycle()
        return byteArray
    }

    fun convertBytesToBitmap(bitmapData: ByteArray?): Bitmap? {
        if (bitmapData == null)
            return null

        return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size);
    }

    suspend fun getCompressedImage(imageFile: File?, context: Context?): String? {
        var mFilePath = ""
        var compressedImageFile: File? = null
        try {
            if (context != null && imageFile != null) {
                compressedImageFile = Compressor.compress(context, imageFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (compressedImageFile != null) {
            mFilePath = compressedImageFile.absolutePath
            Log.d(ContentValues.TAG, "onImageRecieved:File Path $mFilePath")
        }
        return mFilePath
    }

    fun createFlippedBitmap(source: Bitmap?, xFlip: Boolean = true, yFlip: Boolean = false): Bitmap? {
        return source?.let {
            val matrix = Matrix()
            matrix.postScale(
                if (xFlip) -1f else 1f,
                if (yFlip) -1f else 1f,
                it.width / 2f,
                it.height / 2f
            )

            Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
        }
    }

    fun getAvatarBitmap(urlSource: String?, complete: ((bitmap: Bitmap?, message: String?) -> Unit)? = null) {
        if (urlSource.isNullOrEmpty()) {
            val bitmapAvatar = BitmapFactory.decodeResource(PLANS_APP.resources, R.drawable.ic_user_default)
            complete?.also { it(bitmapAvatar, null) }
            return
        }

        downloadBitmap(urlSource){ bitmap, message ->
            val avatar = bitmap?.resize(40.toPx(), 40.toPx())?.let { getCircledBitmap(it) }
            complete?.also { it(avatar, message) }
        }
    }

    fun getAvatarBitmap(urlSource: String?): Bitmap? {
        return if (!urlSource.isNullOrEmpty()) {
            try{
                Glide.with(PLANS_APP)
                    .asBitmap()
                    .load(urlSource)
                    .submit().get()
            }catch (e: Exception) {
                BitmapFactory.decodeResource(PLANS_APP.resources, R.drawable.ic_user_default)
            }
        }else {
            BitmapFactory.decodeResource(PLANS_APP.resources, R.drawable.ic_user_default)
        }.resize(40.toPx(), 40.toPx())?.let { getCircledBitmap(it) }
    }

    fun downloadBitmap(urlSource: String?, complete: ((bitmap: Bitmap?, message: String?) -> Unit)? = null) {
        if (urlSource.isNullOrEmpty()) {
            complete?.also { it(null, "Image url is null or empty") }
            return
        }

        Glide.with(PLANS_APP)
            .asBitmap()
            .load(urlSource)
            .thumbnail()
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    complete?.also { it(resource, null) }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    complete?.also { it(null, "Failed to download") }
                }
            })
    }

    fun getCircledBitmap(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color

        canvas.drawCircle(
            bitmap.width / 2f, bitmap.height / 2f,
            min(bitmap.width / 2f, bitmap.height / 2f), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

}

// Extension function to resize bitmap using new width value by keeping aspect ratio
fun Bitmap.resizeByWidth(width:Int):Bitmap{
    val ratio:Float = this.width.toFloat() / this.height.toFloat()
    val height:Int = (width / ratio).roundToInt()

    return Bitmap.createScaledBitmap(
        this,
        width,
        height,
        false
    )
}


// Extension function to resize bitmap using new height value by keeping aspect ratio
fun Bitmap.resizeByHeight(height:Int):Bitmap{
    val ratio:Float = this.height.toFloat() / this.width.toFloat()
    val width:Int = (height / ratio).roundToInt()

    return Bitmap.createScaledBitmap(
        this,
        width,
        height,
        false
    )
}

fun Bitmap.resize(maxWidth: Int = 100, maxHeight: Int = 100): Bitmap? {
    return if ((width > maxHeight || height > maxHeight) && (maxHeight > 0 && maxWidth > 0)) {
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        Bitmap.createScaledBitmap(this, finalWidth, finalHeight, true)
    } else {
        this
    }
}