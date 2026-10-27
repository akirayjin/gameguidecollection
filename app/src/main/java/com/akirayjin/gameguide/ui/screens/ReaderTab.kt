package com.akirayjin.gameguide.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.data.AppDatabase
import com.akirayjin.gameguide.data.GameWithSections
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.ui.theme.VitaGold

@Composable
fun ReaderTab() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val games by db.gameDao().getAllGamesWithSections().collectAsState(initial = emptyList())

    var selectedGame by remember { mutableStateOf<GameWithSections?>(null) }
    var selectedSectionId by remember { mutableStateOf<Long?>(null) }
    var sectionDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        if (selectedGame == null) {
            Text(
                text = stringResource(R.string.offline_library_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.offline_library_subtitle),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (games.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_guides_available),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games, key = { it.game.id }) { g ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGame = g
                                        selectedSectionId = g.sections.firstOrNull()?.id
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = g.game.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VitaGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.sections_available_format, g.sections.size),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val game = selectedGame!!
            val sections = game.sections.sortedBy { it.orderIndex }
            val activeSection = sections.find { it.id == selectedSectionId } ?: sections.firstOrNull()

            // Top bar with back button & game title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedGame = null }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VitaBlue)
                }
                Text(
                    text = game.game.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inline Dropdown for Sections
            if (sections.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { sectionDropdownExpanded = !sectionDropdownExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.current_section),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeSection?.title ?: stringResource(R.string.select_section),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = VitaGold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = VitaBlue
                        )
                    }

                    DropdownMenu(
                        expanded = sectionDropdownExpanded,
                        onDismissRequest = { sectionDropdownExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        sections.forEach { sec ->
                            DropdownMenuItem(
                                text = { Text(sec.title, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    selectedSectionId = sec.id
                                    sectionDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Native Section Content View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (activeSection != null) {
                    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                    val bgColor = if (isDark) "#0d1117" else "#ffffff"
                    val textColor = if (isDark) "#c9d1d9" else "#24292f"
                    val borderColor = if (isDark) "#30363d" else "#d0d7de"
                    val headerBg = if (isDark) "#161b22" else "#f6f8fa"

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = false
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            }
                        },
                        update = { webView ->
                            val normalizedContent = activeSection.content
                                .replace(Regex("<image\\s+", RegexOption.IGNORE_CASE), "<img ")
                                .replace(Regex("</image>", RegexOption.IGNORE_CASE), "")

                            val htmlData = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            background-color: $bgColor;
                                            color: $textColor;
                                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                                            font-size: 15px;
                                            line-height: 1.6;
                                            margin: 0;
                                            padding: 16px;
                                        }
                                        h1, h2, h3 { color: #58a6ff; margin-top: 16px; margin-bottom: 8px; }
                                        h1 { font-size: 22px; border-bottom: 1px solid $borderColor; padding-bottom: 6px; }
                                        h2 { font-size: 18px; }
                                        h3 { font-size: 16px; }
                                        .section-title {
                                            font-size: 20px;
                                            font-weight: bold;
                                            color: #d4a373;
                                            margin-top: 0;
                                            margin-bottom: 12px;
                                            border-bottom: 1px solid $borderColor;
                                            padding-bottom: 8px;
                                        }
                                        table {
                                            width: 100%;
                                            border-collapse: collapse;
                                            margin: 12px 0;
                                            font-size: 14px;
                                        }
                                        th, td {
                                            border: 1px solid $borderColor;
                                            padding: 8px 12px;
                                            text-align: left;
                                        }
                                        th {
                                            background-color: $headerBg;
                                            color: $textColor;
                                            font-weight: bold;
                                        }
                                        img, image {
                                            max-width: 100%;
                                            height: auto;
                                            border-radius: 6px;
                                            margin: 8px 0;
                                        }
                                        .note-box {
                                            background-color: ${if (isDark) "#1f2d3d" else "#ddf4ff"};
                                            border-left: 4px solid #388bfd;
                                            padding: 10px 14px;
                                            border-radius: 4px;
                                            margin: 12px 0;
                                            color: $textColor;
                                        }
                                        .warning-box {
                                            background-color: ${if (isDark) "#3d2d1f" else "#fff8c5"};
                                            border-left: 4px solid #d29922;
                                            padding: 10px 14px;
                                            border-radius: 4px;
                                            margin: 12px 0;
                                            color: $textColor;
                                        }
                                        code {
                                            background-color: ${if (isDark) "#161b22" else "#afb8c133"};
                                            padding: 2px 6px;
                                            border-radius: 4px;
                                            font-family: monospace;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="section-title">${escapeHtml(activeSection.title)}</div>
                                    $normalizedContent
                                </body>
                                </html>
                            """.trimIndent()

                            webView.loadDataWithBaseURL("http://localhost/", htmlData, "text/html", "UTF-8", null)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_sections_available_game),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

private fun escapeHtml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
