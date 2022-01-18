package com.planscollective.plansapp.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Size
import androidx.core.content.FileProvider
import com.planscollective.plansapp.PLANS_APP
import java.io.File

object OSHelper {

    val isSamsung : Boolean
        get()  {
            return Build.MANUFACTURER.lowercase().contains("samsung")
        }

    private val displayMetrics = DisplayMetrics().apply {
        PLANS_APP.currentActivity?.windowManager?.defaultDisplay?.getMetrics(this)
    }

    var widthScreen: Int = displayMetrics.widthPixels
    var heightScreen: Int = displayMetrics.heightPixels
    var sizeScreen = Size(widthScreen, heightScreen)

    val statusBarHeight : Int
        get() {
            val resSystem = Resources.getSystem()
            val idStatusBarHeight = resSystem.getIdentifier("status_bar_height", "dimen", "android")
            return if (idStatusBarHeight > 0) {
                resSystem.getDimensionPixelSize(idStatusBarHeight)
            } else 0
        }

    fun isTablet(activity: Activity): Boolean {
        return (activity.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun isLandscape(activity: Activity): Boolean {
        val ptScreenSize = Point()
        activity.windowManager.defaultDisplay.getSize(ptScreenSize)
        return ptScreenSize.y > ptScreenSize.y
    }

    @SuppressLint("InlinedApi")
    fun isAirPlaneMode(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    fun getVersion(applicationContext: Context): String {
        val manager = applicationContext.packageManager
        val info = manager.getPackageInfo(applicationContext.packageName, 0)
        return info.versionName
    }

    fun getBuild(applicationContext: Context): Int {
        val manager = applicationContext.packageManager
        val info = manager.getPackageInfo(applicationContext.packageName, 0)
        return info.versionCode
    }

    fun sendEmail(
        activity: Activity,
        email: String,
        subject: String = "",
        body: String = "",
        attachmentPath: String = ""
    ): Boolean {
        try {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            emailIntent.putExtra(Intent.EXTRA_TEXT, body)
            if (email.isNotEmpty())
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            emailIntent.type = "plain/text"

            if (attachmentPath.isNotEmpty()) {
                val file = File(attachmentPath)
                val uri = FileProvider.getUriForFile(
                    activity,
                    activity.applicationContext.packageName + ".provider",
                    file
                )
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(emailIntent)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun callPhoneNumber(activity: Activity, phoneNumber: String): Boolean {
        if (phoneNumber.isEmpty())
            return false

        try {
            val uri = android.net.Uri.parse("tel:" + phoneNumber.trim().replace(" ", ""))
            val intent = Intent(Intent.ACTION_DIAL, uri)
            activity.startActivity(intent)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun copyTextToClipboard(applicationContext: Context, text: String) {
        val clipboard =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(applicationContext.packageName, text)
        clipboard.setPrimaryClip(clip)
    }

    fun getUriFrom(context: Context, filePath: String): android.net.Uri {
        val file = java.io.File(filePath)
        var targetUri: android.net.Uri? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            targetUri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
        } else {
            targetUri = android.net.Uri.fromFile(file)
        }
        return targetUri
    }

    fun openFile(context: Context, filePath: String, title: String? = null): Boolean {
        try {
            val targetUri = getUriFrom(context, filePath)
            val fileName = targetUri.toString()

            val intent = Intent(Intent.ACTION_VIEW)
            if (!title.isNullOrEmpty()) {
                intent.putExtra(Intent.EXTRA_TITLE, title)
            }

            // set flag to give temporary permission to external app to use your FileProvider
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Check what kind of file you are trying to open, by comparing the url with extensions.
            // When the if condition is matched, plugin sets the correct intent (mime) type,
            // so Android knew what application to use to open the file
            if (fileName.contains(".doc") || fileName.contains(".docx")) {
                // Word document
                intent.setDataAndType(targetUri, "application/msword")
            } else if (fileName.contains(".pdf")) {
                // PDF file
                intent.setDataAndType(targetUri, "application/pdf")
            } else if (fileName.contains(".ppt") || fileName.contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(targetUri, "application/vnd.ms-powerpoint")
            } else if (fileName.contains(".xls") || fileName.contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(targetUri, "application/vnd.ms-excel")
            } else if (fileName.contains(".zip") || fileName.contains(".rar")) {
                // WAV audio file
                intent.setDataAndType(targetUri, "application/x-wav")
            } else if (fileName.contains(".rtf")) {
                // RTF file
                intent.setDataAndType(targetUri, "application/rtf")
            } else if (fileName.contains(".wav") || fileName.contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(targetUri, "audio/x-wav")
            } else if (fileName.contains(".gif")) {
                // GIF file
                intent.setDataAndType(targetUri, "image/gif")
            } else if (fileName.contains(".jpg") || fileName.contains(".jpeg") || fileName.contains(
                    ".png"
                )
            ) {
                // JPG file
                intent.setDataAndType(targetUri, "image/jpeg")
            } else if (fileName.contains(".txt")) {
                // Text file
                intent.setDataAndType(targetUri, "text/plain")
            } else if (fileName.contains(".3gp") || fileName.contains(".mpg") || fileName.contains(".mpeg") || fileName.contains(
                    ".mpe"
                ) || fileName.contains(".mp4") || fileName.contains(".avi")
            ) {
                // Video files
                intent.setDataAndType(targetUri, "video/*")
            } else {
                //if you want you can also define the intent type for any other file

                //additionally use else clause below, to manage other unknown extensions
                //in this case, Android will show all applications installed on the device
                //so you can choose which application to use
                intent.setDataAndType(targetUri, "*/*")
            }

            context.startActivity(intent)
            /*val intentChooser = Intent.createChooser(intent, "Open File")
            try {
                context.startActivity(intentChooser)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                // Instruct the user to install a PDF reader here, or something
                return false
            }*/

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun openUrl(context: Context, url: String, title: String? = null): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            if (!title.isNullOrEmpty()) {
                intent.putExtra(Intent.EXTRA_TITLE, title)
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}