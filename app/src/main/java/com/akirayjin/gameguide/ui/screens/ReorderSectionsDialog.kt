package com.akirayjin.gameguide.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.data.GuideSectionEntity
import com.akirayjin.gameguide.ui.theme.VitaBlue
import kotlinx.coroutines.launch

@Composable
fun ReorderSectionsDialog(
    gameTitle: String,
    sections: List<GuideSectionEntity>,
    onDismiss: () -> Unit,
    onSaveNewOrder: (List<GuideSectionEntity>) -> Unit
) {
    var orderedSections by remember { mutableStateOf(sections.sortedBy { it.orderIndex }) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.reorder_sections_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = gameTitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Use the Up (▲) and Down (▼) buttons to reorder sections:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(orderedSections, key = { _, sec -> sec.id }) { index, section ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${section.title}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            if (index > 0) {
                                                val list = orderedSections.toMutableList()
                                                val item = list.removeAt(index)
                                                val newIndex = index - 1
                                                list.add(newIndex, item)
                                                orderedSections = list
                                                scope.launch {
                                                    listState.animateScrollToItem(newIndex.coerceAtLeast(0))
                                                }
                                            }
                                        },
                                        enabled = index > 0,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("▲", fontSize = 12.sp, color = VitaBlue)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (index < orderedSections.size - 1) {
                                                val list = orderedSections.toMutableList()
                                                val item = list.removeAt(index)
                                                val newIndex = index + 1
                                                list.add(newIndex, item)
                                                orderedSections = list
                                                scope.launch {
                                                    listState.animateScrollToItem(newIndex.coerceAtMost(orderedSections.size - 1))
                                                }
                                            }
                                        },
                                        enabled = index < orderedSections.size - 1,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("▼", fontSize = 12.sp, color = VitaBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = orderedSections.mapIndexed { idx, sec ->
                        sec.copy(orderIndex = idx)
                    }
                    onSaveNewOrder(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VitaBlue)
            ) {
                Text(stringResource(R.string.save_order_btn))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_btn), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
