package com.akirayjin.gameguide.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.data.GuideSectionEntity
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.ui.theme.VitaCardBorder
import com.akirayjin.gameguide.ui.theme.VitaSurfaceDark
import com.akirayjin.gameguide.ui.theme.VitaTextPrimary
import com.akirayjin.gameguide.ui.theme.VitaTextSecondary
import com.akirayjin.gameguide.util.ImageUtils
import kotlinx.coroutines.launch

@Composable
fun AdvancedEditorDialog(
    section: GuideSectionEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(section?.title ?: "") }
    var contentText by remember { mutableStateOf(section?.content ?: "") }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showUrlInputDialog by remember { mutableStateOf(false) }
    var imageUrlInput by remember { mutableStateOf("") }
    var isProcessingImage by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }

    fun insertImageTag(base64Image: String) {
        val tag = "<image src=\"$base64Image\" />"
        contentText += if (contentText.isNotEmpty() && !contentText.endsWith("\n")) "\n$tag" else tag
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingImage = true
            scope.launch {
                val base64 = ImageUtils.uriToBase64(context, uri)
                if (base64 != null) {
                    insertImageTag(base64)
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_image_process), Toast.LENGTH_SHORT).show()
                }
                isProcessingImage = false
            }
        }
    }

    fun insertHtml(tagStart: String, tagEnd: String) {
        contentText += "$tagStart...$tagEnd"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitaSurfaceDark,
        title = {
            Text(
                text = if (section == null) stringResource(R.string.add_advanced_section) else stringResource(R.string.edit_advanced_section),
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
                    label = { Text(stringResource(R.string.section_title_label), color = VitaTextSecondary) },
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.html_formatting_toolbar),
                    fontSize = 12.sp,
                    color = VitaTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    EditorChip("H1") { insertHtml("<h1>", "</h1>") }
                    EditorChip("H2") { insertHtml("<h2>", "</h2>") }
                    EditorChip("H3") { insertHtml("<h3>", "</h3>") }
                    EditorChip("Bold") { insertHtml("<b>", "</b>") }
                    EditorChip("Italic") { insertHtml("<i>", "</i>") }
                    EditorChip("Code") { insertHtml("<code>", "</code>") }
                    EditorChip("List") { insertHtml("<ul>\n  <li>", "</li>\n</ul>") }
                    EditorChip("📊 Table") { showTableDialog = true }
                    EditorChip("Note Box") { insertHtml("<div class=\"note-box\">\n  <b>Note:</b> ", "\n</div>") }
                    EditorChip("Warning") { insertHtml("<div class=\"warning-box\">\n  <b>Warning:</b> ", "\n</div>") }
                    EditorChip("📷 Image") { showImageSourceDialog = true }
                }

                if (isProcessingImage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = VitaBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.processing_image_msg),
                            fontSize = 12.sp,
                            color = VitaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text(stringResource(R.string.html_content_label), color = VitaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 8,
                    maxLines = 14,
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
                        onSave(title.trim(), contentText.trim())
                    }
                },
                enabled = title.isNotBlank() && !isProcessingImage,
                colors = ButtonDefaults.buttonColors(containerColor = VitaBlue)
            ) {
                Text(stringResource(R.string.save_section_btn))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_btn), color = VitaTextSecondary)
            }
        }
    )

    if (showTableDialog) {
        var rowsText by remember { mutableStateOf("3") }
        var colsText by remember { mutableStateOf("2") }
        var includeHeaderRow by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showTableDialog = false },
            containerColor = VitaSurfaceDark,
            title = {
                Text(
                    text = stringResource(R.string.insert_table_title),
                    fontWeight = FontWeight.Bold,
                    color = VitaTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = colsText,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) colsText = it },
                            label = { Text(stringResource(R.string.columns_label), color = VitaTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VitaBlue,
                                unfocusedBorderColor = VitaCardBorder,
                                focusedTextColor = VitaTextPrimary,
                                unfocusedTextColor = VitaTextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = rowsText,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) rowsText = it },
                            label = { Text(stringResource(R.string.rows_label), color = VitaTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VitaBlue,
                                unfocusedBorderColor = VitaCardBorder,
                                focusedTextColor = VitaTextPrimary,
                                unfocusedTextColor = VitaTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeHeaderRow,
                            onCheckedChange = { includeHeaderRow = it },
                            colors = CheckboxDefaults.colors(checkedColor = VitaBlue)
                        )
                        Text(
                            text = stringResource(R.string.include_header_row),
                            color = VitaTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cols = colsText.toIntOrNull()?.coerceIn(1, 20) ?: 2
                        val rows = rowsText.toIntOrNull()?.coerceIn(1, 50) ?: 3
                        val sb = StringBuilder()
                        sb.append("<table>\n")
                        if (includeHeaderRow) {
                            sb.append("  <thead>\n    <tr>\n")
                            for (c in 1..cols) {
                                sb.append("      <th>Header $c</th>\n")
                            }
                            sb.append("    </tr>\n  </thead>\n")
                        }
                        sb.append("  <tbody>\n")
                        for (r in 1..rows) {
                            sb.append("    <tr>\n")
                            for (c in 1..cols) {
                                sb.append("      <td>Data $r-$c</td>\n")
                            }
                            sb.append("    </tr>\n")
                        }
                        sb.append("  </tbody>\n</table>")

                        val tableHtml = sb.toString()
                        contentText += if (contentText.isNotEmpty() && !contentText.endsWith("\n")) "\n$tableHtml" else tableHtml
                        showTableDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitaBlue)
                ) {
                    Text(stringResource(R.string.insert_btn))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTableDialog = false }) {
                    Text(stringResource(R.string.cancel_btn), color = VitaTextSecondary)
                }
            }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = VitaSurfaceDark,
            title = {
                Text(
                    text = stringResource(R.string.insert_image_title),
                    fontWeight = FontWeight.Bold,
                    color = VitaTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.choose_image_source_msg),
                        fontSize = 14.sp,
                        color = VitaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VitaBlue)
                    ) {
                        Text(stringResource(R.string.local_device_btn))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showImageSourceDialog = false
                            imageUrlInput = ""
                            showUrlInputDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.image_url_btn), color = VitaBlue)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showImageSourceDialog = false }) {
                    Text(stringResource(R.string.cancel_btn), color = VitaTextSecondary)
                }
            }
        )
    }

    if (showUrlInputDialog) {
        AlertDialog(
            onDismissRequest = { showUrlInputDialog = false },
            containerColor = VitaSurfaceDark,
            title = {
                Text(
                    text = stringResource(R.string.enter_image_url_title),
                    fontWeight = FontWeight.Bold,
                    color = VitaTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        label = { Text(stringResource(R.string.image_url_label), color = VitaTextSecondary) },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val url = imageUrlInput.trim()
                        showUrlInputDialog = false
                        if (url.isNotBlank()) {
                            isProcessingImage = true
                            scope.launch {
                                val base64 = ImageUtils.urlToBase64(url)
                                if (base64 != null) {
                                    insertImageTag(base64)
                                } else {
                                    Toast.makeText(context, context.getString(R.string.failed_image_url), Toast.LENGTH_SHORT).show()
                                }
                                isProcessingImage = false
                            }
                        }
                    },
                    enabled = imageUrlInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VitaBlue)
                ) {
                    Text(stringResource(R.string.save_btn))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUrlInputDialog = false }) {
                    Text(stringResource(R.string.cancel_btn), color = VitaTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun EditorChip(label: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, color = VitaBlue) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = VitaSurfaceDark),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = VitaBlue
        ),
        modifier = Modifier.padding(end = 6.dp)
    )
}
