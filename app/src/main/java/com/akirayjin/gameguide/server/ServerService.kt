package com.akirayjin.gameguide.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.akirayjin.gameguide.MainActivity
import com.akirayjin.gameguide.R
import com.akirayjin.gameguide.data.AppDatabase
import com.akirayjin.gameguide.data.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var httpServer: VitaHttpServer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Populate sample guides in DB if empty
        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            SampleData.populateSampleGuidesIfEmpty(db.gameDao())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startServer()
            ACTION_STOP -> stopServer()
        }
        return START_STICKY
    }

    private fun startServer() {
        if (_isRunning.value) return

        try {
            acquireWakeLock()
            val db = AppDatabase.getInstance(applicationContext)
            var boundPort = 8080
            var serverStarted = false

            // Try port 8080, fallback to 8081, 8888, 8000 if occupied
            for (p in listOf(8080, 8081, 8888, 8000)) {
                try {
                    val server = VitaHttpServer(p, db) {
                        _requestCount.value += 1
                        _lastRequestTime.value = System.currentTimeMillis()
                        updateNotification()
                    }
                    server.start()
                    httpServer = server
                    boundPort = p
                    serverStarted = true
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!serverStarted) {
                throw IllegalStateException("Failed to bind local HTTP server to any port.")
            }

            val ip = NetworkUtils.getLocalIpAddress(applicationContext)
            val url = "http://$ip:$boundPort"

            _isRunning.value = true
            _serverUrl.value = url
            _requestCount.value = 0

            val notification = buildNotification("Server running at $url")
            startForegroundSafely(notification)

        } catch (e: Exception) {
            e.printStackTrace()
            releaseWakeLock()
            _isRunning.value = false
            _serverUrl.value = ""
            stopSelf()
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GameGuide::ServerWakeLock").apply {
                acquire(3 * 60 * 60 * 1000L) // 3 hours timeout
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundSafely(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                }
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun stopServer() {
        try {
            httpServer?.stop()
            httpServer = null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            releaseWakeLock()
            _isRunning.value = false
            _serverUrl.value = ""
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ServerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PS Vita Game Guide Server")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop Server", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val url = _serverUrl.value
        val reqs = _requestCount.value
        val notification = buildNotification("URL: $url ($reqs requests served)")
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PS Vita HTTP Server Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status of local HTTP server for PS Vita"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.akirayjin.gameguide.START_SERVER"
        const val ACTION_STOP = "com.akirayjin.gameguide.STOP_SERVER"

        private const val CHANNEL_ID = "ps_vita_server_channel"
        private const val NOTIFICATION_ID = 1001

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _serverUrl = MutableStateFlow("")
        val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

        private val _requestCount = MutableStateFlow(0)
        val requestCount: StateFlow<Int> = _requestCount.asStateFlow()

        private val _lastRequestTime = MutableStateFlow(0L)
        val lastRequestTime: StateFlow<Long> = _lastRequestTime.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, ServerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ServerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
