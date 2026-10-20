package com.akirayjin.gameguide.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.akirayjin.gameguide.data.AppDatabase
import com.akirayjin.gameguide.data.FileGuideManager
import com.akirayjin.gameguide.data.GameWithSections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipGuideManager {

    fun getGuidesRootDir(): File {
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "GameGuideData"
        )
        if (!root.exists()) {
            root.mkdirs()
        }
        return root
    }

    suspend fun exportAllGuidesToZip(context: Context, destUri: Uri) = withContext(Dispatchers.IO) {
        val rootDir = getGuidesRootDir()
        context.contentResolver.openOutputStream(destUri)?.use { os ->
            ZipOutputStream(os).use { zos ->
                zipFile(rootDir, rootDir.name, zos)
            }
        }
    }

    private fun zipFile(fileToZip: File, fileName: String, zos: ZipOutputStream) {
        if (fileToZip.isHidden) return
        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles() ?: return
            for (child in children) {
                zipFile(child, "$fileName/${child.name}", zos)
            }
            return
        }
        val fis = FileInputStream(fileToZip)
        val zipEntry = ZipEntry(fileName)
        zos.putNextEntry(zipEntry)
        val bytes = ByteArray(1024)
        var length: Int
        while (fis.read(bytes).also { length = it } >= 0) {
            zos.write(bytes, 0, length)
        }
        fis.close()
    }

    suspend fun importGuidesFromZip(context: Context, zipUri: Uri): Int = withContext(Dispatchers.IO) {
        val rootDir = getGuidesRootDir()
        var importedCount = 0

        context.contentResolver.openInputStream(zipUri)?.use { isStream ->
            ZipInputStream(isStream).use { zis ->
                var zipEntry = zis.nextEntry
                while (zipEntry != null) {
                    val newFile = File(rootDir, zipEntry.name)
                    if (zipEntry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        val fos = FileOutputStream(newFile)
                        val bytes = ByteArray(1024)
                        var len: Int
                        while (zis.read(bytes).also { len = it } > 0) {
                            fos.write(bytes, 0, len)
                        }
                        fos.close()
                    }
                    zis.closeEntry()
                    zipEntry = zis.nextEntry
                }
            }
        }

        // After unzipping files into GameGuideData/guides/, import JSON files into DB
        val guidesFolder = File(rootDir, "guides")
        if (guidesFolder.exists() && guidesFolder.isDirectory) {
            val db = AppDatabase.getInstance(context)
            val jsonFiles = guidesFolder.listFiles { f -> f.extension.lowercase() == "json" } ?: emptyArray()
            for (jsonFile in jsonFiles) {
                try {
                    val jsonStr = jsonFile.readText(Charsets.UTF_8)
                    val guide = FileGuideManager.parseGuideFromJson(jsonStr)
                    val gId = db.gameDao().insertGame(guide.game)
                    guide.sections.forEach { sec ->
                        db.gameDao().insertSection(sec.copy(gameId = gId))
                    }
                    importedCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        importedCount
    }
}
