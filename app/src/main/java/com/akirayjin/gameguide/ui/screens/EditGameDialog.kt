package com.akirayjin.gameguide.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.data.GameEntity
import com.akirayjin.gameguide.data.Platforms
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.ui.theme.VitaCardBorder
import com.akirayjin.gameguide.ui.theme.VitaSurfaceDark
import com.akirayjin.gameguide.ui.theme.VitaTextPrimary
import com.akirayjin.gameguide.ui.theme.VitaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGameDialog(
    game: GameEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, platform: String, summary: String) -> Unit
) {
    var title by remember { mutableStateOf(game?.title ?: "") }
    var platform by remember { mutableStateOf(game?.platform ?: "PS Vita") }
    var summary by remember { mutableStateOf(game?.summary ?: "") }
    var platformExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitaSurfaceDark,
        title = {
            Text(
                text = if (game == null) stringResource(R.string.add_game_guide) else stringResource(R.string.edit_game_guide),
                fontWeight = FontWeight.Bold,
                color = VitaTextPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.game_title_label), color = VitaTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitaBlue,
                        unfocusedBorderColor = VitaCardBorder,
                        focusedTextColor = VitaTextPrimary,
                        unfocusedTextColor = VitaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = platformExpanded,
                    onExpandedChange = { platformExpanded = !platformExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = platform,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.platform_label), color = VitaTextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VitaBlue,
                            unfocusedBorderColor = VitaCardBorder,
                            focusedTextColor = VitaTextPrimary,
                            unfocusedTextColor = VitaTextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = platformExpanded,
                        onDismissRequest = { platformExpanded = false }
                    ) {
                        Platforms.ALL.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p, color = VitaTextPrimary) },
                                onClick = {
                                    platform = p
                                    platformExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(stringResource(R.string.summary_label), color = VitaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitaBlue,
                        unfocusedBorderColor = VitaCardBorder,
                        focusedTextColor = VitaTextPrimary,
                        unfocusedTextColor = VitaTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), platform.trim(), summary.trim())
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VitaBlue)
            ) {
                Text(stringResource(R.string.save_btn))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_btn), color = VitaTextSecondary)
            }
        }
    )
}
