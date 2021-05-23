package com.example.uploaddocuments

import android.graphics.Bitmap

object FunctionUtils {

    fun recycleBitmap(bitmap: Bitmap) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}
