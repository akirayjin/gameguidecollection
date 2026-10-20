package com.akirayjin.gameguide.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.ui.theme.VitaBlue
import com.akirayjin.gameguide.ui.theme.VitaSurfaceDark
import com.akirayjin.gameguide.ui.theme.VitaTextPrimary

@Composable
fun WebPreviewDialog(
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = VitaSurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.web_preview_title),
                        fontWeight = FontWeight.Bold,
                        color = VitaTextPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_btn), color = VitaBlue)
                    }
                }

                Spacer(modifier = Modifier.padding(vertical = 4.dp))

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        webView.loadUrl(url)
                    }
                )
            }
        }
    }
}
