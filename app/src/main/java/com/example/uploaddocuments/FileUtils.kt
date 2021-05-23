package com.example.uploaddocuments

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object FileUtils {

    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? { //check for KITKAT or above
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                    .toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }
            }
            else if (isDownloadsDocument(uri)) {
                return getDownloadFilePath(context, uri)
            }
            else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                    .toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)) { // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment
            else getDataColumn(context, uri, null, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getDownloadFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        try {
            cursor = context.contentResolver.query(uri!!, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val fileName = cursor.getString(0)
                val path = Environment.getExternalStorageDirectory()
                    .toString() + "/Download/" + fileName
                if (!TextUtils.isEmpty(path)) {
                    return path
                }
            }
        }
        finally {
            cursor!!.close()
        }
        val id = DocumentsContract.getDocumentId(uri)
        if (id.startsWith("raw:")) {
            return id.replaceFirst("raw:".toRegex(), "")
        }
        val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads"), java.lang.Long.valueOf(id))
        return getDataColumn(context, contentUri, null, null)
    }

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

    fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        }
        finally {
            cursor?.close()
        }
        return null
    }

    fun getCompressedBitmapForUpload(source: Bitmap): ByteArray {
        var bitmapWidth = source.width
        var bitmapHeight = source.height
        val oneKB = 1536
        val bos = ByteArrayOutputStream()
        var maxSide: Int = Math.max(bitmapHeight, bitmapWidth)
        if (maxSide < oneKB) {
            val size: Int = source.byteCount//source.rowBytes * source.height
            val byteBuffer: ByteBuffer = ByteBuffer.allocate(size)
            // source.copyPixelsToBuffer(byteBuffer)
            source.compress(Bitmap.CompressFormat.JPEG, 100, bos)

            return bos.toByteArray().also { source.recycle() }
        }
        val bitmapRatio = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        var newHeight = 0
        var newWidth = 0


        if (bitmapWidth > bitmapHeight) {
            newWidth = oneKB
            newHeight = (newWidth / bitmapRatio).toInt()
        }
        else if (bitmapHeight > bitmapWidth) {
            newHeight = oneKB
            newWidth = (newHeight * bitmapRatio).toInt()
        }
        else {
            newHeight = oneKB
            newWidth = oneKB
        }
        val compressionRatio: Int = (newHeight * 100) / (bitmapHeight)
        val target = Bitmap.createScaledBitmap(source, bitmapWidth, bitmapHeight, true)
        target?.compress(Bitmap.CompressFormat.JPEG, compressionRatio /*ignored for PNG*/, bos)
//        var target = Bitmap.createScaledBitmap(source, bitmapWidth, bitmapHeight, true)

        return bos.toByteArray().also { FunctionUtils.recycleBitmap(source)
            FunctionUtils.recycleBitmap(target)}
    }
}