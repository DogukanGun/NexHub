package com.dag.nexwallet.base.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ImageDownloader @Inject constructor() {
    
    fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String = "AI_Generated_Image"): Boolean {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val finalFileName = "${fileName}_$timestamp.jpg"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, use MediaStore API
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, finalFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NexWallet")
                }
                
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { imageUri ->
                    context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.flush()
                    }
                    true
                } ?: false
            } else {
                // For older Android versions, use traditional file API
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val nexWalletDir = File(imagesDir, "NexWallet")
                if (!nexWalletDir.exists()) {
                    nexWalletDir.mkdirs()
                }
                
                val imageFile = File(nexWalletDir, finalFileName)
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 