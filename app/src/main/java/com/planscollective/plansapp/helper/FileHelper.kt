package com.planscollective.plansapp.helper

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.constants.AppInfo
import com.planscollective.plansapp.extension.toFormatString
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import com.planscollective.plansapp.webServices.file.FileApis
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.threeten.bp.LocalDateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

object FileHelper {
    val webService = WebServiceBuilder.buildService(FileApis::class.java)
    const val nameCameraFolder = "folder_plans_camera"

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun fileDownload(type: String? = null) : File? {
        val extension = if (type == "image") ".png" else if (type == "video") ".mp4" else ""
        return File(PLANS_APP.filesDir, "downloadedFil$extension")
    }

    fun fileForPlansImage(extension: String? = null) : File {
        val nameFile = LocalDateTime.now().toFormatString("yyyyMMddHHmmss")
        val folder = File(PLANS_APP.filesDir, nameCameraFolder)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder, "$nameFile.${extension.takeIf { !it.isNullOrEmpty() } ?: "jpg"}")
    }

    fun fileForPlansVideo(extension: String? = null): File {
        val nameFile = LocalDateTime.now().toFormatString("yyyyMMddHHmmss")
        val folder = File(PLANS_APP.filesDir, nameCameraFolder)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder, "$nameFile.${extension.takeIf { !it.isNullOrEmpty() } ?: "mp4"}")
    }

    fun removeAllFilesInCameraFolder() {
        val folder = File(PLANS_APP.filesDir, nameCameraFolder)
        deleteRecursive(folder)
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (!fileOrDirectory.exists()) return

        if (fileOrDirectory.isDirectory){
            fileOrDirectory.listFiles()?.forEach {
                deleteRecursive(it)
            }
        }

        fileOrDirectory.delete()
    }


    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    fun getFilePath(uri: Uri?, context: Context?): String? {
        val uri = uri ?: return null
        val context = context ?: return null

        //check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )

                // File
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )

            // File
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getImageFilePath(contentUri: Uri?, context: Context?): String? {
        if (contentUri == null || context == null)
            return null

        var filePath = ""
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context?.contentResolver?.query(contentUri, proj, null, null, null)
            if (cursor == null)
                return null
            val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            filePath = cursor.getString(column_index)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            cursor?.close()
        }

        return filePath
    }

    fun downloadFileFrom(
        fileUrl: String?,
        type: String? = null,
        complete: ((file: File?, message: String?) -> Unit)? = null
    ) {

        val url = fileUrl?.takeIf { it.isNotEmpty() } ?: run {
            complete?.also { it(null, "Failed to download the file, Invalid file url") }
            return
        }

        webService.downloadFileWithDynamicUrl(url).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    writeResponseBodyToDisk(response.body(), type, complete)
                }else {
                    complete?.also { it(null, "Failed to download the file") }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                complete?.also { it(null, "Failed to download the file") }
            }
        })
    }

    fun downloadFileToPhotoAlbum(
        fileUrl: String?,
        type: String?,
        complete: ((success: Boolean?, message: String?) -> Unit)? = null
    ){
        downloadFileFrom(fileUrl, type) {
            file, message ->
            if (file?.exists() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveFileInGalleryOnSdk29Up(file, type, complete)
                }else {
                    saveFileInGalleryOnSdk29Down(file, type, complete)
                }
            }else {
                complete?.also { it(null, message) }
            }
        }
    }

    fun saveFileInGalleryOnSdk29Down(
        file: File?,
        type: String?,
        complete: ((success: Boolean?, message: String?) -> Unit)? = null
    ) {
        if (file == null || !file.exists() || type.isNullOrEmpty()) {
            complete?.also { it(false, "Failed to save the file, invalid parameters")}
            return
        }

        val fileName = LocalDateTime.now().toFormatString("yyyy-MM-dd-HH-mm-ss") + if (type == "image") ".jpg" else ".mp4"
        val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + AppInfo.APP_NAME).apply { if (!exists()) mkdirs() }
        val fileOutPut = File(folder, fileName)

        GlobalScope.launch (Dispatchers.IO) {
            var fileInputStream: FileInputStream? = null
            var fileOutPutStream: FileOutputStream? = null
            val fileReader = ByteArray(4096)
            var result = false

            try {
                fileOutPutStream = FileOutputStream(fileOutPut)
                fileInputStream = FileInputStream(file)

                while(true) {
                    val read = fileInputStream.read(fileReader)
                    if (read == -1) break
                    fileOutPutStream.write(fileReader, 0, read)
                }
                fileOutPutStream.flush()
                fileOutPutStream.close()
                fileInputStream.close()
                result = true
            } catch (e: IOException) {
                e.printStackTrace()
                fileOutPutStream?.flush()
                fileOutPutStream?.close()
                fileInputStream?.close()
            }

            withContext(Dispatchers.Main) {
                if (result) {
                    complete?.also { it(true, "Successfully saved")}
                }else {
                    complete?.also { it(false, "Failed to save the file")}
                }
            }
        }


    }

    fun saveFileInGalleryOnSdk29Up(
        file: File?,
        type: String?,
        complete: ((success: Boolean?, message: String?) -> Unit)? = null
    ){
        if (file == null || !file.exists() || type.isNullOrEmpty()) {
            complete?.also { it(false, "Failed to save the file, invalid parameters")}
            return
        }
        GlobalScope.launch (Dispatchers.IO){
            var inputStream : InputStream? = null
            var outputStream : OutputStream? = null
            var result = false

            try {
                val titleNow = LocalDateTime.now().toFormatString("yyyy-MM-dd-HH-mm-ss")
                val dirDest = File( Environment.DIRECTORY_PICTURES, AppInfo.APP_NAME)
                val mineType = if (type == "image") "image/png" else "video/mp4"
                val collection = if (type == "image") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }else  {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    } else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.TITLE, titleNow)
                    put(MediaStore.MediaColumns.DISPLAY_NAME, titleNow)
                    put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
                    put(MediaStore.MediaColumns.MIME_TYPE, mineType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.VOLUME_NAME, AppInfo.APP_NAME)
                        put(MediaStore.MediaColumns.BUCKET_ID, AppInfo.APP_NAME)
                        put(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, AppInfo.APP_NAME)
                        put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirDest${File.separator}")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        put(MediaStore.MediaColumns.ALBUM, AppInfo.APP_NAME)
                    }
                }

                val uri = collection?.let { PLANS_APP.contentResolver.insert(it, values) }
                if (uri != null) {
                    val fileReader = ByteArray(4096)
                    outputStream = PLANS_APP.contentResolver.openOutputStream(uri)
                    inputStream = FileInputStream(file)
                    while(true) {
                        val read = inputStream.read(fileReader)
                        if (read == -1) break
                        outputStream?.write(fileReader, 0, read)
                    }
                    outputStream?.flush()
                    outputStream?.close()
                    inputStream.close()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        PLANS_APP.contentResolver.update(uri, values, null, null)
                    }
                    result = true
                }
            }catch (e : Exception) {
                e.printStackTrace()
                outputStream?.flush()
                outputStream?.close()
                inputStream?.close()
            }

            withContext(Dispatchers.Main) {
                if (result) {
                    complete?.also { it(true, "Successfully saved")}
                }else {
                    complete?.also { it(false, "Failed to save the file")}
                }
            }
        }
    }


    @DelicateCoroutinesApi
    private fun writeResponseBodyToDisk(
        body: ResponseBody?,
        type: String? = null,
        complete: ((file: File?, message: String?) -> Unit)? = null
    ) {
        val responseBody = body ?: run{
            complete?.also { it(null, "Failed to save the file, the response body is empty") }
            return
        }

        GlobalScope.launch(Dispatchers.IO){

            var inputStream : InputStream? = null
            var outputStream : OutputStream? = null
            val file = fileDownload(type)
            var result = false

            try {
                val fileReader = ByteArray(4096)
                var fileSizeDownloaded : Long = 0

                inputStream = responseBody.byteStream()
                outputStream = FileOutputStream(file)

                while(true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) break
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read
                }
                outputStream.flush()
                inputStream.close()
                outputStream.close()
                result = true
            }catch (e: Exception) {
                e.printStackTrace()
                inputStream?.close()
                outputStream?.close()
            }

            complete?.also {
                withContext(Dispatchers.Main) {
                    if (result) {
                        it(file, "Successfully downloaded")
                    }else {
                        it(null, "Failed to save the file")
                    }
                }
            }
        }
    }

}