package com.akirayjin.gameguide.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    private const val TAG = "NetworkUtils"

    fun getLocalIpAddress(context: Context): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            var privateIp: String? = null
            var hotspotApIp: String? = null
            var anyIp: String? = null

            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val name = networkInterface.name.lowercase()

                if (!networkInterface.isUp || networkInterface.isLoopback) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val hostAddress = addr.hostAddress ?: continue
                        Log.d(TAG, "Found interface: name=$name, ip=$hostAddress")

                        val isPrivate = hostAddress.startsWith("192.168.") || 
                                        hostAddress.startsWith("10.") || 
                                        is172Private(hostAddress)

                        if (name.contains("ap") || name.contains("softap") || hostAddress.startsWith("192.168.43.")) {
                            hotspotApIp = hostAddress
                        }

                        if (isPrivate) {
                            if (name.contains("wlan") || name.contains("wifi") || name.contains("eth") || name.contains("bridge")) {
                                Log.d(TAG, "Selected Wi-Fi/Ethernet IP: $hostAddress")
                                return hostAddress
                            }
                            if (privateIp == null) {
                                privateIp = hostAddress
                            }
                        }

                        if (anyIp == null && !hostAddress.startsWith("127.")) {
                            anyIp = hostAddress
                        }
                    }
                }
            }

            if (hotspotApIp != null) {
                Log.d(TAG, "Selected Hotspot AP IP: $hotspotApIp")
                return hotspotApIp
            }
            if (privateIp != null) {
                Log.d(TAG, "Selected Private IP: $privateIp")
                return privateIp
            }
            if (anyIp != null) {
                Log.d(TAG, "Selected Fallback IP: $anyIp")
                return anyIp
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP address", e)
        }
        return "127.0.0.1"
    }

    private fun is172Private(ip: String): Boolean {
        if (!ip.startsWith("172.")) return false
        val parts = ip.split(".")
        if (parts.size >= 2) {
            val secondOctet = parts[1].toIntOrNull() ?: return false
            return secondOctet in 16..31
        }
        return false
    }

    fun isWifiOrHotspotActive(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
               caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || 
               caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
