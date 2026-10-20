package com.akirayjin.gameguide.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object FileGuideManager {

    suspend fun exportGuideToJson(gameWithSections: GameWithSections): String = withContext(Dispatchers.Default) {
        val root = JSONObject()
        root.put("version", 1)

        val gameObject = JSONObject()
        gameObject.put("title", gameWithSections.game.title)
        gameObject.put("platform", gameWithSections.game.platform)
        gameObject.put("summary", gameWithSections.game.summary)
        gameObject.put("updatedAt", gameWithSections.game.updatedAt)
        root.put("game", gameObject)

        val sectionsArray = JSONArray()
        gameWithSections.sections.sortedBy { it.orderIndex }.forEachIndexed { index, sec ->
            val secObj = JSONObject()
            secObj.put("title", sec.title)
            secObj.put("content", sec.content)
            secObj.put("orderIndex", index + 1)
            sectionsArray.put(secObj)
        }
        root.put("sections", sectionsArray)

        root.toString(2)
    }

    suspend fun saveGuideToLocalFile(context: Context, gameWithSections: GameWithSections): File = withContext(Dispatchers.IO) {
        val jsonStr = exportGuideToJson(gameWithSections)
        val guidesDir = File(context.filesDir, "guides")
        if (!guidesDir.exists()) {
            guidesDir.mkdirs()
        }
        val safeFileName = sanitizeFileName("${gameWithSections.game.title}.json")
        val file = File(guidesDir, safeFileName)
        file.writeText(jsonStr, Charsets.UTF_8)
        file
    }

    suspend fun exportGuideToUri(context: Context, gameWithSections: GameWithSections, uri: Uri): Unit = withContext(Dispatchers.IO) {
        val jsonStr = exportGuideToJson(gameWithSections)
        context.contentResolver.openOutputStream(uri)?.use { os ->
            os.write(jsonStr.toByteArray(Charsets.UTF_8))
        }
    }

    suspend fun parseGuideFromJson(jsonStr: String): GameWithSections = withContext(Dispatchers.Default) {
        val root = JSONObject(jsonStr)
        val gameObj = root.getJSONObject("game")

        val game = GameEntity(
            title = gameObj.optString("title", "Imported Guide"),
            platform = gameObj.optString("platform", "PS Vita"),
            summary = gameObj.optString("summary", ""),
            updatedAt = gameObj.optLong("updatedAt", System.currentTimeMillis())
        )

        val sectionsArray = root.optJSONArray("sections") ?: JSONArray()
        val sections = mutableListOf<GuideSectionEntity>()

        for (i in 0 until sectionsArray.length()) {
            val secObj = sectionsArray.getJSONObject(i)
            sections.add(
                GuideSectionEntity(
                    gameId = 0,
                    title = secObj.optString("title", "Section ${i + 1}"),
                    content = secObj.optString("content", ""),
                    orderIndex = secObj.optInt("orderIndex", i + 1)
                )
            )
        }

        GameWithSections(game = game, sections = sections)
    }

    suspend fun importGuideFromUri(context: Context, uri: Uri): GameWithSections = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { isStream ->
            BufferedReader(InputStreamReader(isStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
            }
        }
        parseGuideFromJson(sb.toString())
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
