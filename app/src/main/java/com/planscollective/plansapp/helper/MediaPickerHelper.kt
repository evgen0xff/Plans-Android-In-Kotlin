package com.planscollective.plansapp.helper

import android.Manifest
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.PlansCameraActivity
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.constants.RequestCodes
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.interfaces.OnSelectedMenuItem
import com.planscollective.plansapp.models.dataModels.MenuModel
import com.theartofdev.edmodo.cropper.CropImage
import permissions.dispatcher.ktx.constructPermissionsRequest
import java.io.File




object MediaPickerHelper: OnSelectedMenuItem{

    enum class MediaType {
        ALL {
            override val mineType: String = String.format("*/*")
        },
        IMAGE_ONLY {
            override val mineType: String = "image/*"
        },
        VIDEO_ONLY {
            override val mineType: String = "video/*"
        };

        abstract val mineType: String
    }

    enum class PickerType {
        DEFAULT_ALL {
            override val mediaType = MediaType.ALL
            override val isCropped = false
            override val isFixAspectRatio: Boolean? = null
            override val aspectRatioX: Int? = null
            override val aspectRatioY: Int? = null
        },
        DEFAULT_IMAGE {
            override val mediaType = MediaType.IMAGE_ONLY
            override val isCropped = false
            override val isFixAspectRatio: Boolean? = null
            override val aspectRatioX: Int? = null
            override val aspectRatioY: Int? = null
        },
        DEFAULT_VIDEO {
            override val mediaType = MediaType.VIDEO_ONLY
            override val isCropped = false
            override val isFixAspectRatio: Boolean? = null
            override val aspectRatioX: Int? = null
            override val aspectRatioY: Int? = null
        },
        EVENT_COVER {
            override val mediaType = MediaType.ALL
            override val isCropped = true
            override val isFixAspectRatio = true
            override val aspectRatioX = 3
            override val aspectRatioY = 2
        };

        abstract val mediaType: MediaType
        abstract val isCropped: Boolean
        abstract val isFixAspectRatio: Boolean?
        abstract val aspectRatioX: Int?
        abstract val aspectRatioY: Int?
    }

    var mediaPickerListener: MediaPickerListener? = null
    var typePicker = PickerType.DEFAULT_ALL

    fun showPickerOptions(
        activity: FragmentActivity?,
        listener: MediaPickerListener? = null,
        typePicker: PickerType = PickerType.DEFAULT_ALL
    ) {
        this.typePicker = typePicker

        activity?.apply {
            constructPermissionsRequest(Manifest.permission.CAMERA) {
                constructPermissionsRequest(Manifest.permission.RECORD_AUDIO){
                    constructPermissionsRequest(Manifest.permission.READ_EXTERNAL_STORAGE){
                        constructPermissionsRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE){
                            val menuItems = ArrayList<MenuModel>()
                            menuItems.add(MenuModel(R.drawable.ic_camera_outline_black, "Camera"))
                            menuItems.add(MenuModel(R.drawable.ic_picture_outline_black, "Gallery"))
                            MenuOptionHelper.showBottomMenu(menuItems, this, this@MediaPickerHelper)
                            mediaPickerListener = listener
                        }.launch()
                    }.launch()

                }.launch()
            }.launch()
        }
    }

    fun showPickerOptions(
        fragment: Fragment?,
        listener: MediaPickerListener? = null,
        typePicker: PickerType = PickerType.DEFAULT_ALL
    ) {
        showPickerOptions(fragment?.requireActivity(), listener, typePicker)
    }

    override fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any?) {
        menuItem?.apply {
            when (this.titleText) {
                "Camera" -> {
                    openPlansCamera(mediaPickerListener, typePicker)
                }
                "Gallery" -> {
                    openMediaGallery(mediaPickerListener, typePicker)
                }
            }
        }
    }

    fun openPlansCamera(
        listener: MediaPickerListener? = null,
        typePicker: PickerType = PickerType.DEFAULT_ALL
    ) {
        listener?.also {
            mediaPickerListener = it
        }
        this.typePicker = typePicker

        mediaPickerListener?.requireActivity()?.also {
            val intent = Intent(it, PlansCameraActivity::class.java)
            it.startActivity(intent)
        }
    }

    fun openMediaGallery(
        listener: MediaPickerListener? = null,
        typePicker: PickerType = PickerType.DEFAULT_ALL
    ) {
        listener?.also {
            mediaPickerListener = it
        }
        this.typePicker = typePicker

        mediaPickerListener?.requireActivity()?.also {
            val intent = Intent()
            intent.type = typePicker.mediaType.mineType
            intent.action = Intent.ACTION_GET_CONTENT
            it.startActivityForResult(intent, RequestCodes.MEDIA_GALLERY)
        }
    }

    fun onSelectedPicture(filePath: String?) {
        if (typePicker.isCropped && !filePath.isNullOrEmpty()) {
            mediaPickerListener?.requireActivity()?.also {
                CropImage.activity(Uri.fromFile(File(filePath))).apply {
                    setInitialCropWindowPaddingRatio(0.0f)
                    if (typePicker.aspectRatioX != null && typePicker.aspectRatioY != null) {
                        setAspectRatio(typePicker.aspectRatioX!!, typePicker.aspectRatioY!!)
                        setTouchRadius(0.0f)
                    }
                    if (typePicker.isFixAspectRatio != null) {
                        setFixAspectRatio(typePicker.isFixAspectRatio!!)
                    }
                }.start(it)
            }
        }else {
            mediaPickerListener?.onSelectedPhoto(checkRotation(filePath))
        }
    }

    fun onCroppedPicture(filePath: String?) {
        mediaPickerListener?.onSelectedPhoto(checkRotation(filePath))
    }

    fun onSelectedVideo(filePath: String?, uri: Uri? = null) {
        getVideoDuration(filePath)?.takeIf { it >= (Constants.LIMIT_DURATION_VIDEO + 1000)}?.also {
            PLANS_APP.currentActivity?.also {
                TrimVideo.activity(uri?.toString())
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setMinToMax(1, 30)  //seconds
                    .start(it, it.startForResult)
            }
        } ?: run {
            mediaPickerListener?.onSelectedVideo(filePath)
        }
    }

    fun onTrimmedVideo(filePath: String?) {
        mediaPickerListener?.onSelectedVideo(filePath)
    }


    fun getVideoDuration(filePath: String?): Long? {
        return filePath?.takeIf { it.isNotEmpty() }?.let {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(PLANS_APP, Uri.fromFile(File(filePath)))
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            time?.toLong()
        }
    }

    fun checkRotation(filePath: String?) : String? {
        return if (ImageHelper.getExifOrientation(filePath) == 0f) filePath else {
            ImageHelper.getBitmap(filePath)?.let {
                val resultPath =  FileHelper.fileForPlansImage()
                if (ImageHelper.saveBitmap(it, resultPath.path)) resultPath.path else null
            }
        }
    }

}