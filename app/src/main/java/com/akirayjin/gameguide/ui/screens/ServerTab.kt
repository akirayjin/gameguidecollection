package com.akirayjin.gameguide.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.server.ServerService
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.ui.theme.VitaGold
import com.akirayjin.gameguide.ui.theme.VitaSuccessGreen
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ServerTab(
    onOpenWebPreview: (String) -> Unit
) {
    val context = LocalContext.current
    val isRunning by ServerService.isRunning.collectAsState()
    val serverUrl by ServerService.serverUrl.collectAsState()
    val requestCount by ServerService.requestCount.collectAsState()

    var showQrCode by remember { mutableStateOf(false) }

    val urlCopiedToast = stringResource(R.string.url_copied_toast)
    val requestsServedText = stringResource(R.string.requests_served, requestCount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Server Control Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.local_http_server),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRunning) stringResource(R.string.status_running) else stringResource(R.string.status_stopped),
                            fontSize = 13.sp,
                            color = if (isRunning) VitaSuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = isRunning,
                        onCheckedChange = { checked ->
                            if (checked) {
                                ServerService.startService(context)
                            } else {
                                ServerService.stopService(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VitaSuccessGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isRunning && serverUrl.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.server_address),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = serverUrl,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitaGold
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Server URL", serverUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, urlCopiedToast, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.copy_url_desc),
                                    tint = VitaBlue
                                )
                            }
                            IconButton(
                                onClick = { showQrCode = !showQrCode }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = stringResource(R.string.toggle_qr_desc),
                                    tint = VitaBlue
                                )
                            }
                            IconButton(onClick = { onOpenWebPreview(serverUrl) }) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = stringResource(R.string.preview_desc),
                                    tint = VitaBlue
                                )
                            }
                        }
                    }

                    if (showQrCode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            generateQrCodeBitmap(serverUrl, 512, 512)?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = stringResource(R.string.server_qr_code_desc),
                                    modifier = Modifier.size(180.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = requestsServedText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.port_label),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.ssl_warning),
                        fontSize = 11.sp,
                        color = VitaGold,
                        lineHeight = 15.sp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.server_toggle_hint),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Instructions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = stringResource(R.string.wifi_icon_desc),
                        tint = VitaBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.how_to_connect),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                InstructionStep(
                    stepNumber = "1",
                    title = stringResource(R.string.conn_step_1_title),
                    description = stringResource(R.string.conn_step_1_desc)
                )

                InstructionStep(
                    stepNumber = "2",
                    title = stringResource(R.string.conn_step_2_title),
                    description = stringResource(R.string.conn_step_2_desc)
                )

                InstructionStep(
                    stepNumber = "3",
                    title = stringResource(R.string.conn_step_3_title),
                    description = stringResource(R.string.conn_step_3_desc)
                )

                InstructionStep(
                    stepNumber = "4",
                    title = stringResource(R.string.conn_step_4_title),
                    description = stringResource(R.string.conn_step_4_desc)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.router_tip),
                        fontSize = 11.sp,
                        color = VitaGold,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(VitaBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
        }
    }
}

private fun generateQrCodeBitmap(text: String, width: Int, height: Int): Bitmap? {
    return try {
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
