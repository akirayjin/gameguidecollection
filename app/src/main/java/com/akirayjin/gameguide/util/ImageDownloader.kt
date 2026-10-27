package com.akirayjin.gameguide.util

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Matcher
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
        val imgPattern = Pattern.compile("<(img|image)\\s+([^>]*)\\s*src=\"([^\"]+)\"([^>]*)>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        val matcher = imgPattern.matcher(htmlContent)
        val sb = StringBuffer()

        while (matcher.find()) {
            val tagName = matcher.group(1) ?: "image"
            val attrBefore = matcher.group(2) ?: ""
            val srcUrl = matcher.group(3) ?: ""
            val attrAfter = matcher.group(4) ?: ""

            // If it's already a base64 data URI or a local /images/ path, keep it
            if (srcUrl.startsWith("data:") || srcUrl.startsWith("/images/")) {
                val replacement = "<$tagName $attrBefore src=\"$srcUrl\" $attrAfter>"
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
                continue
            }

            if (srcUrl.startsWith("http://") || srcUrl.startsWith("https://") || srcUrl.startsWith("//")) {
                val resolvedUrl = if (srcUrl.startsWith("//")) "https:$srcUrl" else srcUrl
                try {
                    val base64Data = ImageUtils.urlToBase64Sync(resolvedUrl)
                    if (base64Data != null) {
                        Log.d(TAG, "Converted image URL to base64: $resolvedUrl")
                        val replacement = "<$tagName $attrBefore src=\"$base64Data\" $attrAfter>"
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
                        continue
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to convert image URL to base64: $resolvedUrl", e)
                }
            }
            // Fallback: keep original src if conversion failed
            val replacement = "<$tagName $attrBefore src=\"$srcUrl\" $attrAfter>"
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }
}
