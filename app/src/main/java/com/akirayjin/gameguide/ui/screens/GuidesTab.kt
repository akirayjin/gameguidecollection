package com.akirayjin.gameguide.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.data.GameEntity
import com.akirayjin.gameguide.data.GameWithSections
import com.akirayjin.gameguide.data.GuideSectionEntity
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.ui.theme.VitaErrorRed
import com.akirayjin.gameguide.ui.theme.VitaGold

@Composable
fun GuidesTab(
    games: List<GameWithSections>,
    onAddGameClick: () -> Unit,
    onExportGameClick: (GameWithSections) -> Unit,
    onEditGameClick: (GameEntity) -> Unit,
    onDeleteGameClick: (GameEntity) -> Unit,
    onAddSectionClick: (Long) -> Unit,
    onEditSectionClick: (GuideSectionEntity) -> Unit,
    onDeleteSectionClick: (GuideSectionEntity) -> Unit,
    onSaveSectionOrder: (List<GuideSectionEntity>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredGames = remember(games, searchQuery) {
        if (searchQuery.isBlank()) games
        else {
            games.filter { g ->
                g.game.title.contains(searchQuery, ignoreCase = true) ||
                g.game.summary.contains(searchQuery, ignoreCase = true) ||
                g.sections.any { s -> s.title.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_placeholder), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = VitaBlue
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VitaBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) stringResource(R.string.no_game_guides_yet) else stringResource(R.string.no_matching_guides),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.import_load_samples_hint),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGames, key = { it.game.id }) { gameWithSections ->
                        GameGuideCard(
                            gameWithSections = gameWithSections,
                            onExportGame = { onExportGameClick(gameWithSections) },
                            onEditGame = { onEditGameClick(gameWithSections.game) },
                            onDeleteGame = { onDeleteGameClick(gameWithSections.game) },
                            onAddSection = { onAddSectionClick(gameWithSections.game.id) },
                            onEditSection = onEditSectionClick,
                            onDeleteSection = onDeleteSectionClick,
                            onSaveSectionOrder = onSaveSectionOrder
                        )
                    }
                }
            }
        }

        // FAB to add new Game manually
        FloatingActionButton(
            onClick = onAddGameClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = VitaBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Game")
        }
    }
}

@Composable
private fun GameGuideCard(
    gameWithSections: GameWithSections,
    onExportGame: () -> Unit,
    onEditGame: () -> Unit,
    onDeleteGame: () -> Unit,
    onAddSection: () -> Unit,
    onEditSection: (GuideSectionEntity) -> Unit,
    onDeleteSection: (GuideSectionEntity) -> Unit,
    onSaveSectionOrder: (List<GuideSectionEntity>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteGameDialog by remember { mutableStateOf(false) }
    var showReorderDialog by remember { mutableStateOf(false) }
    var sectionToDelete by remember { mutableStateOf<GuideSectionEntity?>(null) }

    if (showDeleteGameDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteGameDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(R.string.delete_game_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.delete_game_message, gameWithSections.game.title), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteGameDialog = false
                        onDeleteGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitaErrorRed)
                ) {
                    Text(stringResource(R.string.delete_btn))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteGameDialog = false }) {
                    Text(stringResource(R.string.cancel_btn), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    if (showReorderDialog) {
        ReorderSectionsDialog(
            gameTitle = gameWithSections.game.title,
            sections = gameWithSections.sections,
            onDismiss = { showReorderDialog = false },
            onSaveNewOrder = { newOrder ->
                showReorderDialog = false
                onSaveSectionOrder(newOrder)
            }
        )
    }

    if (sectionToDelete != null) {
        val sec = sectionToDelete!!
        AlertDialog(
            onDismissRequest = { sectionToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(R.string.delete_section_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.delete_section_message, sec.title), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        sectionToDelete = null
                        onDeleteSection(sec)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitaErrorRed)
                ) {
                    Text(stringResource(R.string.delete_btn))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sectionToDelete = null }) {
                    Text(stringResource(R.string.cancel_btn), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(VitaBlue, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = gameWithSections.game.platform,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = gameWithSections.game.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VitaGold
                    )
                    if (gameWithSections.game.summary.isNotBlank()) {
                        Text(
                            text = gameWithSections.game.summary,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2
                        )
                    }
                }

                Row {
                    IconButton(onClick = onExportGame) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Guide File",
                            tint = VitaBlue
                        )
                    }
                    IconButton(onClick = onEditGame) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Game",
                            tint = VitaBlue
                        )
                    }
                    IconButton(onClick = { showDeleteGameDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Game",
                            tint = VitaErrorRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sections toggle bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.guide_sections_count_format, gameWithSections.sections.size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Sections",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = onAddSection,
                                colors = ButtonDefaults.buttonColors(containerColor = VitaBlue),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.add_section_btn), fontSize = 12.sp)
                            }

                            if (gameWithSections.sections.size > 1) {
                                OutlinedButton(
                                    onClick = { showReorderDialog = true },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Reorder,
                                        contentDescription = null,
                                        tint = VitaBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reorder", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (gameWithSections.sections.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_sections_in_guide),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        gameWithSections.sections.sortedBy { it.orderIndex }.forEach { section ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = section.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditSection(section) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Section",
                                                tint = VitaBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { sectionToDelete = section },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Section",
                                                tint = VitaErrorRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
