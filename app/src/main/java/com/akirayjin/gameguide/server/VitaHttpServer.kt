package com.akirayjin.gameguide.server

import android.util.Log
import com.akirayjin.gameguide.data.AppDatabase
import com.akirayjin.gameguide.data.FileGuideManager
import com.akirayjin.gameguide.data.GameEntity
import com.akirayjin.gameguide.data.GameWithSections
import com.akirayjin.gameguide.data.GuideSectionEntity
import com.akirayjin.gameguide.util.ImageDownloader
import com.akirayjin.gameguide.util.ZipGuideManager
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.net.URLDecoder

class VitaHttpServer(
    port: Int,
    private val db: AppDatabase,
    private val onRequestHandled: (() -> Unit)? = null
) : NanoHTTPD("0.0.0.0", port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        val parms = session.parms
        Log.d(TAG, "==> HTTP Request: method=$method, uri=$uri")

        onRequestHandled?.invoke()

        val userAgent = session.headers["user-agent"]?.lowercase() ?: ""
        val isConsoleOrMobile = userAgent.contains("vita") || 
                                 userAgent.contains("3ds") || 
                                 userAgent.contains("playstation") || 
                                 userAgent.contains("nintendo") || 
                                 userAgent.contains("mobile") ||
                                 userAgent.contains("android") ||
                                 userAgent.contains("iphone") ||
                                 userAgent.contains("ipad")

        if (uri.startsWith("/admin") && isConsoleOrMobile) {
            return newFixedLengthResponse(
                Response.Status.FORBIDDEN,
                "text/html",
                "<h1>403 Forbidden</h1><p>The Admin Panel is restricted to Desktop web browsers only. Please use a computer browser to manage guides.</p><p><a href='/'>Return to Home</a></p>"
            )
        }

        if (method == Method.POST) {
            try {
                val contentLength = session.headers["content-length"]?.toInt() ?: 0
                if (contentLength > 0) {
                    val bytes = ByteArray(contentLength)
                    var totalRead = 0
                    val inputStream = session.inputStream
                    while (totalRead < contentLength) {
                        val read = inputStream.read(bytes, totalRead, contentLength - totalRead)
                        if (read == -1) break
                        totalRead += read
                    }
                    val bodyString = String(bytes, 0, totalRead, Charsets.UTF_8)
                    Log.d(TAG, "Raw POST body read (${bytes.size} bytes): $bodyString")

                    for (param in bodyString.split("&")) {
                        val pair = param.split("=", limit = 2)
                        if (pair.size == 2) {
                            val key = URLDecoder.decode(pair[0], "UTF-8")
                            val value = URLDecoder.decode(pair[1], "UTF-8")
                            parms[key] = value
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading raw POST body", e)
            }
        }

        Log.d(TAG, "Final parsed parms: gameId=${parms["gameId"]}, sectionId=${parms["sectionId"]}, title=${parms["title"]}, contentLen=${parms["content"]?.length}")

        return try {
            when {
                uri == "/css/vita.css" -> {
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/css",
                        VitaHtmlRenderer.getCss()
                    )
                }
                uri == "/js/vita.js" -> {
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "application/javascript",
                        VitaHtmlRenderer.getJs()
                    )
                }
                uri.startsWith("/images/") -> {
                    val imageName = uri.substringAfter("/images/")
                    val imageFile = File(ImageDownloader.getImagesDirectory(), imageName)
                    if (imageFile.exists() && imageFile.isFile) {
                        val mimeType = when {
                            imageName.endsWith(".png", true) -> "image/png"
                            imageName.endsWith(".webp", true) -> "image/webp"
                            imageName.endsWith(".gif", true) -> "image/gif"
                            else -> "image/jpeg"
                        }
                        newChunkedResponse(Response.Status.OK, mimeType, FileInputStream(imageFile))
                    } else {
                        newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Image not found")
                    }
                }
                uri == "/admin" -> {
                    val games = runBlocking(Dispatchers.IO) {
                        db.gameDao().getAllGamesWithSectionsSync()
                    }
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/html; charset=utf-8",
                        VitaHtmlRenderer.renderAdminDashboard(games)
                    )
                }
                uri == "/admin/new-section" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/html; charset=utf-8",
                        VitaHtmlRenderer.renderAdminEditSection(gameId, null)
                    )
                }
                uri == "/admin/edit" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val sectionId = parms["sectionId"]?.toLongOrNull() ?: 0L
                    val section = runBlocking(Dispatchers.IO) {
                        if (sectionId > 0) db.gameDao().getSectionById(sectionId) else null
                    }
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/html; charset=utf-8",
                        VitaHtmlRenderer.renderAdminEditSection(gameId, section)
                    )
                }
                uri == "/admin/edit-game" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val game = runBlocking(Dispatchers.IO) {
                        if (gameId > 0) db.gameDao().getGameById(gameId) else null
                    }
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/html; charset=utf-8",
                        VitaHtmlRenderer.renderAdminEditGame(game)
                    )
                }
                uri == "/admin/delete-section" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val sectionId = parms["sectionId"]?.toLongOrNull() ?: 0L
                    runBlocking(Dispatchers.IO) {
                        val sec = db.gameDao().getSectionById(sectionId)
                        if (sec != null) {
                            db.gameDao().deleteSection(sec)
                            val fullGuide = db.gameDao().getGameWithSectionsById(gameId)
                            if (fullGuide != null) {
                                saveGuideJson(fullGuide)
                            }
                        }
                    }
                    newFixedLengthResponse(
                        Response.Status.REDIRECT,
                        "text/html",
                        ""
                    ).apply {
                        addHeader("Location", "/admin")
                    }
                }
                uri == "/admin/move-section" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val sectionId = parms["sectionId"]?.toLongOrNull() ?: 0L
                    val direction = parms["direction"] ?: "down"
                    runBlocking(Dispatchers.IO) {
                        moveSectionLogic(db, gameId, sectionId, direction)
                    }
                    newFixedLengthResponse(
                        Response.Status.REDIRECT,
                        "text/html",
                        ""
                    ).apply {
                        addHeader("Location", "/admin")
                    }
                }
                uri == "/admin/delete-game" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    runBlocking(Dispatchers.IO) {
                        val game = db.gameDao().getGameById(gameId)
                        if (game != null) {
                            val fullGuide = db.gameDao().getGameWithSectionsById(gameId)
                            if (fullGuide != null) {
                                val safeName = fullGuide.game.title.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json"
                                val file = File(File(ZipGuideManager.getGuidesRootDir(), "guides"), safeName)
                                if (file.exists()) file.delete()
                            }
                            db.gameDao().deleteGame(game)
                        }
                    }
                    newFixedLengthResponse(
                        Response.Status.REDIRECT,
                        "text/html",
                        ""
                    ).apply {
                        addHeader("Location", "/admin")
                    }
                }
                uri == "/admin/export-game" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val gameWithSections = runBlocking(Dispatchers.IO) {
                        db.gameDao().getGameWithSectionsById(gameId)
                    }
                    if (gameWithSections != null) {
                        val jsonStr = runBlocking(Dispatchers.Default) {
                            FileGuideManager.exportGuideToJson(gameWithSections)
                        }
                        val safeName = gameWithSections.game.title.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json"
                        newFixedLengthResponse(Response.Status.OK, "application/json", jsonStr).apply {
                            addHeader("Content-Disposition", "attachment; filename=\"$safeName\"")
                        }
                    } else {
                        newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Game not found")
                    }
                }
                uri == "/admin/save-game" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val title = parms["title"]?.trim() ?: "Untitled Game"
                    val platform = parms["platform"]?.trim() ?: "PS Vita"
                    val summary = parms["summary"]?.trim() ?: ""

                    Log.d(TAG, "Saving Game from admin: gameId=$gameId, title='$title', platform='$platform', summary='$summary'")

                    if (title.isNotBlank()) {
                        runBlocking(Dispatchers.IO) {
                            if (gameId <= 0L) {
                                val newGame = GameEntity(title = title, platform = platform, summary = summary)
                                val gId = db.gameDao().insertGame(newGame)
                                val guide = GameWithSections(game = newGame.copy(id = gId), sections = emptyList())
                                saveGuideJson(guide)
                            } else {
                                val existing = db.gameDao().getGameById(gameId)
                                if (existing != null) {
                                    val updated = existing.copy(title = title, platform = platform, summary = summary)
                                    db.gameDao().updateGame(updated)
                                    val fullGuide = db.gameDao().getGameWithSectionsById(gameId)
                                    if (fullGuide != null) {
                                        saveGuideJson(fullGuide)
                                    }
                                }
                            }
                        }
                    }

                    newFixedLengthResponse(
                        Response.Status.REDIRECT,
                        "text/html",
                        ""
                    ).apply {
                        addHeader("Location", "/admin")
                    }
                }
                uri == "/admin/save-section" -> {
                    val gameId = parms["gameId"]?.toLongOrNull() ?: 0L
                    val sectionId = parms["sectionId"]?.toLongOrNull() ?: 0L
                    val title = parms["title"]?.trim() ?: ""
                    val rawContent = parms["content"] ?: ""
                    
                    val content = ImageDownloader.downloadImagesAndReplaceHtmlSync(rawContent)

                    Log.d(TAG, "Saving section: gameId=$gameId, sectionId=$sectionId, title=$title, contentLen=${content.length}")

                    runBlocking(Dispatchers.IO) {
                        var validGameId = gameId
                        val existingGame = db.gameDao().getGameById(validGameId)
                        if (existingGame == null) {
                            val allGames = db.gameDao().getAllGamesWithSectionsSync()
                            if (allGames.isNotEmpty()) {
                                validGameId = allGames.first().game.id
                            } else {
                                val defaultGame = GameEntity(title = "My Game Guide", platform = "PS Vita", summary = "Auto-created game guide")
                                validGameId = db.gameDao().insertGame(defaultGame)
                            }
                        }

                        if (sectionId <= 0L) {
                            val maxOrder = db.gameDao().getMaxOrderIndexForGame(validGameId) ?: 0
                            db.gameDao().insertSection(
                                GuideSectionEntity(
                                    gameId = validGameId, 
                                    title = if (title.isBlank()) "New Section" else title, 
                                    content = content,
                                    orderIndex = maxOrder + 1
                                )
                            )
                        } else {
                            val existing = db.gameDao().getSectionById(sectionId)
                            if (existing != null) {
                                val finalTitle = if (title.isBlank()) existing.title else title
                                val finalContent = if (content.isBlank() && existing.content.isNotBlank()) existing.content else content
                                db.gameDao().updateSection(existing.copy(gameId = validGameId, title = finalTitle, content = finalContent))
                            } else {
                                val maxOrder = db.gameDao().getMaxOrderIndexForGame(validGameId) ?: 0
                                db.gameDao().insertSection(
                                    GuideSectionEntity(
                                        gameId = validGameId, 
                                        title = if (title.isBlank()) "New Section" else title, 
                                        content = content,
                                        orderIndex = maxOrder + 1
                                    )
                                )
                            }
                        }
                        val fullGuide = db.gameDao().getGameWithSectionsById(validGameId)
                        if (fullGuide != null) {
                            saveGuideJson(fullGuide)
                        }
                    }

                    newFixedLengthResponse(
                        Response.Status.REDIRECT,
                        "text/html",
                        ""
                    ).apply {
                        addHeader("Location", "/admin")
                    }
                }
                uri == "/game" -> {
                    val gameId = parms["id"]?.toLongOrNull()
                    val sectionId = parms["sectionId"]?.toLongOrNull()

                    if (gameId != null) {
                        val gameWithSections = runBlocking(Dispatchers.IO) {
                            db.gameDao().getGameWithSectionsById(gameId)
                        }

                        if (gameWithSections != null) {
                            newFixedLengthResponse(
                                Response.Status.OK,
                                "text/html; charset=utf-8",
                                VitaHtmlRenderer.renderGamePage(gameWithSections, sectionId, !isConsoleOrMobile)
                            )
                        } else {
                            newFixedLengthResponse(
                                Response.Status.NOT_FOUND,
                                "text/html",
                                "<h1>404 Game Not Found</h1><p><a href='/'>Return to Home</a></p>"
                            )
                        }
                    } else {
                        newFixedLengthResponse(
                            Response.Status.BAD_REQUEST,
                            "text/html",
                            "<h1>400 Missing Game ID</h1>"
                        )
                    }
                }
                uri == "/search" -> {
                    val query = parms["q"] ?: ""
                    val matchingGames = runBlocking(Dispatchers.IO) {
                        if (query.isBlank()) {
                            db.gameDao().getAllGamesWithSectionsSync()
                        } else {
                            val games = db.gameDao().searchGames(query)
                            games.mapNotNull { g ->
                                db.gameDao().getGameWithSectionsById(g.id)
                            }
                        }
                    }

                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/html; charset=utf-8",
                        VitaHtmlRenderer.renderSearchResultsPage(query, matchingGames, !isConsoleOrMobile)
                    )
                }
                uri == "/api/status" -> {
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        """{"status":"online","server":"PSVita Game Guide"}"""
                    )
                }
                uri == "/" || uri.isEmpty() -> {
                    val games = runBlocking(Dispatchers.IO) {
                        db.gameDao().getAllGamesWithSectionsSync()
                    }
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "text/html; charset=utf-8",
                        VitaHtmlRenderer.renderIndexPage(games, !isConsoleOrMobile)
                    )
                }
                else -> {
                    newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "text/html",
                        "<h1>404 Not Found</h1><p><a href='/'>Back to Home</a></p>"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error serving request for uri: $uri", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "text/html",
                "<h1>500 Server Error</h1><pre>${e.localizedMessage}</pre>"
            )
        }
    }

    private suspend fun moveSectionLogic(db: AppDatabase, gameId: Long, sectionId: Long, direction: String) {
        val guide = db.gameDao().getGameWithSectionsById(gameId) ?: return
        val sortedSections = guide.sections.sortedBy { it.orderIndex }.toMutableList()
        val index = sortedSections.indexOfFirst { it.id == sectionId }
        if (index == -1) return

        val targetIndex = if (direction == "up") index - 1 else index + 1
        if (targetIndex in sortedSections.indices) {
            val current = sortedSections[index]
            val target = sortedSections[targetIndex]

            val tempOrder = current.orderIndex
            val updatedCurrent = current.copy(orderIndex = target.orderIndex)
            val updatedTarget = target.copy(orderIndex = tempOrder)

            db.gameDao().updateSection(updatedCurrent)
            db.gameDao().updateSection(updatedTarget)

            val fullGuide = db.gameDao().getGameWithSectionsById(gameId)
            if (fullGuide != null) {
                saveGuideJson(fullGuide)
            }
        }
    }

    private suspend fun saveGuideJson(guide: GameWithSections) {
        val jsonStr = FileGuideManager.exportGuideToJson(guide)
        val guidesDir = File(ZipGuideManager.getGuidesRootDir(), "guides")
        if (!guidesDir.exists()) {
            guidesDir.mkdirs()
        }
        val safeName = guide.game.title.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json"
        File(guidesDir, safeName).writeText(jsonStr, Charsets.UTF_8)
    }

    companion object {
        private const val TAG = "VitaHttpServer"
    }
}
