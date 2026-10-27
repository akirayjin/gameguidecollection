package com.akirayjin.gameguide.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageUtils {

    const val DEFAULT_MAX_DIMENSION = 800
    const val DEFAULT_JPEG_QUALITY = 75

    suspend fun uriToBase64(
        context: Context,
        uri: Uri,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()
            processBytesToBase64(bytes, maxDimension, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun urlToBase64(
        urlString: String,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): String? = withContext(Dispatchers.IO) {
        urlToBase64Sync(urlString, maxDimension, quality)
    }

    fun urlToBase64Sync(
        urlString: String,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): String? {
        if (urlString.startsWith("data:image")) {
            return urlString
        }
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val bytes = connection.inputStream.readBytes()
                connection.inputStream.close()
                processBytesToBase64(bytes, maxDimension, quality)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun processBytesToBase64(
        bytes: ByteArray,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): String? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val origWidth = options.outWidth
        val origHeight = options.outHeight
        if (origWidth <= 0 || origHeight <= 0) return null

        var sampleSize = 1
        while (origWidth / sampleSize > maxDimension || origHeight / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
            val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
            val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }

        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val imageBytes = baos.toByteArray()
        val base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64Str"
    }
}
