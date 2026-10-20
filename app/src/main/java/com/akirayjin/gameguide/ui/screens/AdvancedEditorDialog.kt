package com.akirayjin.gameguide.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@Composable
fun AdvancedEditorDialog(
    section: GuideSectionEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(section?.title ?: "") }
    var contentText by remember { mutableStateOf(section?.content ?: "") }

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
                    EditorChip("Table") { insertHtml("<table>\n  <tr><th>Header</th></tr>\n  <tr><td>Data</td></tr>\n</table>", "") }
                    EditorChip("Note Box") { insertHtml("<div class=\"note-box\">\n  <b>Note:</b> ", "\n</div>") }
                    EditorChip("Warning") { insertHtml("<div class=\"warning-box\">\n  <b>Warning:</b> ", "\n</div>") }
                    EditorChip("Image") { insertHtml("<img src=\"", "\" />") }
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
                enabled = title.isNotBlank(),
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
