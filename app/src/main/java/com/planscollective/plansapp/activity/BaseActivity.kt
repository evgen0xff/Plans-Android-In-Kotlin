package com.planscollective.plansapp.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.gowtham.library.utils.LogMessage
import com.gowtham.library.utils.TrimVideo
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.RequestCodes
import com.planscollective.plansapp.extension.getMimeType
import com.planscollective.plansapp.extension.setLayoutHeight
import com.planscollective.plansapp.fragment.utils.PlansDialogFragment
import com.planscollective.plansapp.helper.FileHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.webServices.event.EventWebService
import com.theartofdev.edmodo.cropper.CropImage
import qiu.niorgai.StatusBarCompat


abstract class BaseActivity<VBinding: ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VBinding
    var mNavController : NavController? = null
    var viewStatusBar: View? = null

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK &&
            result.getData() != null) {
            val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()))
            MediaPickerHelper.onTrimmedVideo(uri.path)
        }else
            LogMessage.v("videoTrimResultLauncher data is null");
    }


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        initialize()
   }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    override fun onStart() {
        super.onStart()
        PLANS_APP.currentActivity = this
    }

    override fun onResume() {
        super.onResume()
        PLANS_APP.currentActivity = this
    }

    open fun initialize() {
        PLANS_APP.currentActivity = this
    }

    open fun setupUI() {
        transparentStatusBar()
    }

    open fun transparentStatusBar() {
        StatusBarCompat.setStatusBarColor(this, Color.TRANSPARENT, 0)
        StatusBarCompat.translucentStatusBar(this, true)

        // Status Bar View
        binding.root.findViewById<View>(R.id.statusBar)?.apply {
            viewStatusBar = this
            setLayoutHeight(OSHelper.statusBarHeight)
        }
    }


    open fun showOverlay(message: String?,
                         imageResId: Int? = null,
                         delayForHide: Long? = null,
                         actionAfterHide: (() -> Unit)? = null
    ) {

    }

    open fun hideOverlay() {

    }

    open fun showAlert(message: String?) {
    }

    open fun showWaiting(message: String?) {
    }

    open fun hideAlertMessage() {
    }

    open fun hideWaitingMessage() {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        when(requestCode) {
            RequestCodes.MEDIA_GALLERY -> {
                if (data != null && data.data != null) {
                    val filePath = FileHelper.getFilePath(data.data, this)
                    val mineType = filePath?.getMimeType()
                    if (mineType?.contains("image/") == true) {
                        MediaPickerHelper.onSelectedPicture(filePath)
                    }else if (mineType?.contains("video/") == true) {
                        MediaPickerHelper.onSelectedVideo(filePath, data.data)
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                MediaPickerHelper.onCroppedPicture(result.uri.path)
            }
        }
    }

    fun showPopupEndEvent(event: EventModel?, complete: ((event: EventModel?) -> Unit)? = null) : PlansDialogFragment? {
        if (event == null) {
            complete?.also { it(event) }
            return null
        }

        val message = "All guests left your event! Do you want to end ${event.eventName ?: ""}?"
        val attributedMsg = "All guests left your event! Do you want to end <b>${event.eventName ?: ""}</b>?"

        val dialog = PlansDialogFragment(
            message, "KEEP LIVE", "END", data = event, attributedMsg = attributedMsg,
            actionComplete = { data ->
                complete?.also { it(data as? EventModel) }
            },
            actionNo = {
                EventWebService.endEvent(event.id){ success, msg ->
                    if (success) {
                        PLANS_APP.refreshCurrentScreen()
                    }
                    ToastHelper.showMessage(msg)
                }
            },
            actionYes = {
                EventWebService.keepLivedEvent(event.id){ newEvent, msg ->
                    if (newEvent != null) {
                        PLANS_APP.refreshCurrentScreen()
                    }else {
                        ToastHelper.showMessage(msg)
                    }
                }
            }
        )

        dialog.show(supportFragmentManager, PlansDialogFragment.TAG)

        return dialog
    }

}


class AndroidBug5497Workaround private constructor(activity: Activity) {
    private val mChildOfContent: View
    private var usableHeightPrevious = 0
    private val frameLayoutParams: FrameLayout.LayoutParams
    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard
            }
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(): Int {
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    companion object {
        // For more information, see https://issuetracker.google.com/issues/36911528
        // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.
        fun assistActivity(activity: Activity) {
            AndroidBug5497Workaround(activity)
        }
    }

    init {
        val content = activity.findViewById<View>(android.R.id.content) as FrameLayout
        mChildOfContent = content.getChildAt(0)
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }
}
