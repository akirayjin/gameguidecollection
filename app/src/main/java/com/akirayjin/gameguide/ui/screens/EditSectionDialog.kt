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
fun EditSectionDialog(
    section: GuideSectionEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(section?.title ?: "") }
    var contentText by remember { mutableStateOf(section?.content ?: "") }

    fun insertTag(prefix: String, suffix: String) {
        contentText += "$prefix $suffix"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitaSurfaceDark,
        title = {
            Text(
                text = if (section == null) stringResource(R.string.add_guide_section) else stringResource(R.string.edit_guide_section),
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
                    label = { Text(stringResource(R.string.section_title_hint), color = VitaTextSecondary) },
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

                Text(
                    text = stringResource(R.string.quick_format_tags),
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
                    ChipTag("Header") { insertTag("<h3>", "</h3>") }
                    ChipTag("Bold") { insertTag("<b>", "</b>") }
                    ChipTag("List") { insertTag("<ul>\n  <li>", "</li>\n</ul>") }
                    ChipTag("Table") { insertTag("<table>\n  <tr><th>Col 1</th><th>Col 2</th></tr>\n  <tr><td>Val 1</td><td>Val 2</td></tr>\n</table>", "") }
                    ChipTag("Note") { insertTag("<div class=\"note-box\">", "</div>") }
                    ChipTag("Warning") { insertTag("<div class=\"warning-box\">", "</div>") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text(stringResource(R.string.content_html_hint), color = VitaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 12,
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
private fun ChipTag(label: String, onClick: () -> Unit) {
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
