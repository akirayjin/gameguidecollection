package com.akirayjin.gameguide

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akirayjin.gameguide.ui.theme.GameGuideTheme

class MainActivity : ComponentActivity() {

    private var isDarkTheme by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("game_guide_prefs", Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("dark_theme", true)

        checkPermissions()

        setContent {
            GameGuideTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { dark ->
                        isDarkTheme = dark
                        prefs.edit().putBoolean("dark_theme", dark).apply()
                    }
                )
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                needed.toTypedArray(),
                101
            )
        }
    }
}
