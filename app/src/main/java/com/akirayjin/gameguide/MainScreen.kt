package com.akirayjin.gameguide

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akirayjin.gameguide.data.AppDatabase
import com.akirayjin.gameguide.data.FileGuideManager
import com.akirayjin.gameguide.data.GameEntity
import com.akirayjin.gameguide.data.GameWithSections
import com.akirayjin.gameguide.data.GuideSectionEntity
import com.akirayjin.gameguide.data.SampleData
import com.akirayjin.gameguide.ui.screens.AdvancedEditorDialog
import com.akirayjin.gameguide.ui.screens.EditGameDialog
import com.akirayjin.gameguide.ui.screens.GuidesTab
import com.akirayjin.gameguide.ui.screens.ReaderTab
import com.akirayjin.gameguide.ui.screens.ServerTab
import com.akirayjin.gameguide.ui.screens.SettingsTab
import com.akirayjin.gameguide.ui.screens.WebPreviewDialog
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.util.ImageDownloader
import com.akirayjin.gameguide.util.ZipGuideManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val gamesWithSections by db.gameDao().getAllGamesWithSections().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showEditGameDialog by remember { mutableStateOf(false) }
    var gameToEdit by remember { mutableStateOf<GameEntity?>(null) }

    var showEditSectionDialog by remember { mutableStateOf(false) }
    var sectionToEdit by remember { mutableStateOf<GuideSectionEntity?>(null) }
    var targetGameIdForSection by remember { mutableStateOf<Long?>(null) }

    var exportTargetGame by remember { mutableStateOf<GameWithSections?>(null) }
    var webPreviewUrl by remember { mutableStateOf<String?>(null) }

    val gameSavedMsg = stringResource(R.string.game_saved_toast)
    val sectionSavedMsg = stringResource(R.string.section_saved_toast)

    // File Pickers (.json or .zip)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val uriString = uri.toString().lowercase()
                    if (uriString.endsWith(".zip") || uri.path?.contains("zip") == true) {
                        val count = ZipGuideManager.importGuidesFromZip(context, uri)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Successfully imported $count guide(s) from ZIP!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val importedGuide = FileGuideManager.importGuideFromUri(context, uri)
                        val gId = db.gameDao().insertGame(importedGuide.game)
                        importedGuide.sections.forEach { sec ->
                            db.gameDao().insertSection(sec.copy(gameId = gId))
                        }
                        FileGuideManager.saveGuideToLocalFile(context, importedGuide.copy(game = importedGuide.game.copy(id = gId)))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Successfully imported '${importedGuide.game.title}'!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to import: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && exportTargetGame != null) {
            val target = exportTargetGame!!
            scope.launch(Dispatchers.IO) {
                try {
                    FileGuideManager.exportGuideToUri(context, target, uri)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Successfully exported '${target.game.title}' to JSON!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val exportZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    ZipGuideManager.exportAllGuidesToZip(context, uri)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Successfully exported all guides & images to ZIP!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "ZIP Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.catalogue_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Public, contentDescription = stringResource(R.string.nav_reader)) },
                    label = { Text(stringResource(R.string.nav_reader)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VitaBlue,
                        selectedTextColor = VitaBlue,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Book, contentDescription = stringResource(R.string.nav_guides)) },
                    label = { Text(stringResource(R.string.nav_guides)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VitaBlue,
                        selectedTextColor = VitaBlue,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Dns, contentDescription = stringResource(R.string.nav_server)) },
                    label = { Text(stringResource(R.string.nav_server)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VitaBlue,
                        selectedTextColor = VitaBlue,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VitaBlue,
                        selectedTextColor = VitaBlue,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ReaderTab()
                1 -> GuidesTab(
                    games = gamesWithSections,
                    onAddGameClick = {
                        gameToEdit = null
                        showEditGameDialog = true
                    },
                    onExportGameClick = { game ->
                        exportTargetGame = game
                        val defaultName = "${game.game.title.replace(Regex("[^a-zA-Z0-9._-]"), "_")}.json"
                        createDocumentLauncher.launch(defaultName)
                    },
                    onEditGameClick = { game ->
                        gameToEdit = game
                        showEditGameDialog = true
                    },
                    onDeleteGameClick = { game ->
                        scope.launch(Dispatchers.IO) {
                            db.gameDao().deleteGame(game)
                        }
                    },
                    onAddSectionClick = { gameId ->
                        sectionToEdit = null
                        targetGameIdForSection = gameId
                        showEditSectionDialog = true
                    },
                    onEditSectionClick = { section ->
                        sectionToEdit = section
                        targetGameIdForSection = section.gameId
                        showEditSectionDialog = true
                    },
                    onDeleteSectionClick = { section ->
                        scope.launch(Dispatchers.IO) {
                            db.gameDao().deleteSection(section)
                        }
                    },
                    onSaveSectionOrder = { newOrder ->
                        scope.launch(Dispatchers.IO) {
                            newOrder.forEach { sec ->
                                db.gameDao().updateSection(sec)
                            }
                            val gameId = newOrder.firstOrNull()?.gameId ?: return@launch
                            val fullGuide = db.gameDao().getGameWithSectionsById(gameId)
                            if (fullGuide != null) {
                                val jsonStr = FileGuideManager.exportGuideToJson(fullGuide)
                                val guidesDir = File(ZipGuideManager.getGuidesRootDir(), "guides")
                                if (!guidesDir.exists()) guidesDir.mkdirs()
                                val safeName = fullGuide.game.title.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json"
                                File(guidesDir, safeName).writeText(jsonStr, Charsets.UTF_8)
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Section order saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                2 -> ServerTab(
                    onOpenWebPreview = { url -> webPreviewUrl = url }
                )
                3 -> SettingsTab(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = onThemeChanged,
                    onImportClick = {
                        openDocumentLauncher.launch(arrayOf("application/json", "application/zip", "*/*"))
                    },
                    onExportZipClick = {
                        exportZipLauncher.launch("GameGuides_Backup.zip")
                    },
                    onPopulateSamplesClick = {
                        scope.launch(Dispatchers.IO) {
                            SampleData.populateSampleGuidesIfEmpty(db.gameDao())
                        }
                    }
                )
            }
        }
    }

    // Dialogs
    if (showEditGameDialog) {
        EditGameDialog(
            game = gameToEdit,
            onDismiss = { showEditGameDialog = false },
            onSave = { title, platform, summary ->
                scope.launch(Dispatchers.IO) {
                    if (gameToEdit == null) {
                        val newGame = GameEntity(title = title, platform = platform, summary = summary)
                        db.gameDao().insertGame(newGame)
                    } else {
                        db.gameDao().updateGame(gameToEdit!!.copy(title = title, platform = platform, summary = summary))
                    }
                    withContext(Dispatchers.Main) {
                        showEditGameDialog = false
                        Toast.makeText(context, gameSavedMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showEditSectionDialog && targetGameIdForSection != null) {
        AdvancedEditorDialog(
            section = sectionToEdit,
            onDismiss = { showEditSectionDialog = false },
            onSave = { title, content ->
                scope.launch(Dispatchers.IO) {
                    val processedContent = ImageDownloader.downloadImagesAndReplaceHtml(context, content)
                    if (sectionToEdit == null) {
                        val maxOrder = db.gameDao().getMaxOrderIndexForGame(targetGameIdForSection!!) ?: 0
                        db.gameDao().insertSection(
                            GuideSectionEntity(
                                gameId = targetGameIdForSection!!,
                                title = title,
                                content = processedContent,
                                orderIndex = maxOrder + 1
                            )
                        )
                    } else {
                        db.gameDao().updateSection(
                            sectionToEdit!!.copy(title = title, content = processedContent)
                        )
                    }
                    withContext(Dispatchers.Main) {
                        showEditSectionDialog = false
                        Toast.makeText(context, sectionSavedMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (webPreviewUrl != null) {
        WebPreviewDialog(
            url = webPreviewUrl!!,
            onDismiss = { webPreviewUrl = null }
        )
    }
}
