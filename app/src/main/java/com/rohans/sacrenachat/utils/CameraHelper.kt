package com.rohans.sacrenachat.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object CameraHelper {

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp: String = System.currentTimeMillis().toString();
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

}