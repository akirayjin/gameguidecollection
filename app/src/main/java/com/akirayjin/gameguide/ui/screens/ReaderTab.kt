package com.akirayjin.gameguide.ui.screens

import android.graphics.Color as AndroidColor
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.akirayjin.gameguide.util.ImageDownloader
import java.io.File

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (activeSection != null) {
                        Text(
                            text = activeSection.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitaGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val textColorHex = if (MaterialTheme.colorScheme.background.red > 0.5f) "#24292F" else "#C9D1D9"

                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    textSize = 15f
                                    setTextColor(AndroidColor.parseColor(textColorHex))
                                    setLinkTextColor(AndroidColor.parseColor("#58A6FF"))
                                    movementMethod = LinkMovementMethod.getInstance()
                                }
                            },
                            update = { textView ->
                                textView.setTextColor(AndroidColor.parseColor(textColorHex))
                                val htmlText = activeSection.content
                                val spanned = Html.fromHtml(
                                    htmlText,
                                    Html.FROM_HTML_MODE_COMPACT,
                                    { source ->
                                        try {
                                            val imageName = source?.substringAfter("/images/") ?: ""
                                            val imgFile = File(ImageDownloader.getImagesDirectory(), imageName)
                                            if (imgFile.exists()) {
                                                val d = android.graphics.drawable.Drawable.createFromPath(imgFile.absolutePath)
                                                if (d != null) {
                                                    val intrinsicWidth = d.intrinsicWidth
                                                    val intrinsicHeight = d.intrinsicHeight
                                                    val targetWidth = 1000
                                                    val targetHeight = if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                                                        (intrinsicHeight.toFloat() * (targetWidth.toFloat() / intrinsicWidth.toFloat())).toInt()
                                                    } else {
                                                        800
                                                    }
                                                    d.setBounds(0, 0, targetWidth, targetHeight)
                                                    d
                                                } else null
                                            } else null
                                        } catch (e: Exception) {
                                            null
                                        }
                                    },
                                    null
                                )
                                textView.text = spanned
                            }
                        )
                    } else {
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
