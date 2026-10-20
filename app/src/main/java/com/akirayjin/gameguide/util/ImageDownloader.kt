package com.akirayjin.gameguide.util

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object ImageDownloader {
    private const val TAG = "ImageDownloader"

    fun getImagesDirectory(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "GameGuideImages"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun downloadImagesAndReplaceHtml(context: Context, htmlContent: String): String = withContext(Dispatchers.IO) {
        downloadImagesAndReplaceHtmlSync(htmlContent)
    }

    fun downloadImagesAndReplaceHtmlSync(htmlContent: String): String {
        val imgPattern = Pattern.compile("<img\\s+([^>]*)\\s*src=\"([^\"]+)\"([^>]*)>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        val matcher = imgPattern.matcher(htmlContent)
        val sb = StringBuffer()

        val picturesDir = getImagesDirectory()

        while (matcher.find()) {
            val attrBefore = matcher.group(1) ?: ""
            val srcUrl = matcher.group(2) ?: ""
            val attrAfter = matcher.group(3) ?: ""

            // If it's already a local /images/ path, keep it
            if (srcUrl.startsWith("/images/")) {
                matcher.appendReplacement(sb, "<img $attrBefore src=\"$srcUrl\" $attrAfter>")
                continue
            }

            if (srcUrl.startsWith("http://") || srcUrl.startsWith("https://") || srcUrl.startsWith("//")) {
                val resolvedUrl = if (srcUrl.startsWith("//")) "https:$srcUrl" else srcUrl
                try {
                    val localFile = downloadAndSaveImage(resolvedUrl, picturesDir)
                    if (localFile != null && localFile.exists()) {
                        // Serve via HTTP local endpoint /images/... so desktop browser and consoles can load it without CORS / file:// blocks
                        val localHttpPath = "/images/${localFile.name}"
                        Log.d(TAG, "Downloaded image $resolvedUrl -> $localHttpPath")
                        matcher.appendReplacement(sb, "<img $attrBefore src=\"$localHttpPath\" $attrAfter>")
                        continue
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download image from $resolvedUrl", e)
                }
            }
            // Fallback: keep original src if download failed
            matcher.appendReplacement(sb, "<img $attrBefore src=\"$srcUrl\" $attrAfter>")
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun downloadAndSaveImage(urlString: String, outputDir: File): File? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            val fileName = "img_${Math.abs(urlString.hashCode())}.jpg"
            val file = File(outputDir, fileName)
            if (file.exists() && file.length() > 0) {
                return file
            }

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image: $urlString", e)
            null
        }
    }
}
