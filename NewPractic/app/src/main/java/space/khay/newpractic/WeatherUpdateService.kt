package space.khay.newpractic

import android.content.Intent
import androidx.core.app.JobIntentService
import okhttp3.OkHttpClient
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class WeatherUpdateService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val sharedPreferences = getSharedPreferences("MainActivityPrefs", MODE_PRIVATE)
        val cityName = sharedPreferences.getString("CITY_NAME", null)

        if (cityName != null) {
            val apiKey = "e0b9f80d7c8e8eef6bbe438a8b201830"
            val url = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey&units=metric&lang=ru"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            throw IOException("Unexpected code $response")
                        }

                        val responseData = response.body!!.string()
                        val json = JSONObject(responseData)
                        val list = json.getJSONArray("list")

                        val currentWeatherJson = list.getJSONObject(0)
                        val currentTemp = currentWeatherJson.getJSONObject("main").getDouble("temp").toInt()

                        val lastKnownTemp = sharedPreferences.getInt("LAST_KNOWN_TEMP", Int.MIN_VALUE)

                        if (lastKnownTemp != Int.MIN_VALUE && lastKnownTemp != currentTemp) {
                            sendNotification()
                            sharedPreferences.edit().putInt("LAST_KNOWN_TEMP", currentTemp).apply()
                        } else if (lastKnownTemp == Int.MIN_VALUE) {
                            sharedPreferences.edit().putInt("LAST_KNOWN_TEMP", currentTemp).apply()
                        }
                    }
                }
            })
        }
    }

    private fun sendNotification() {
        val context = applicationContext
        val intent = Intent(context, WeatherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "weather_channel_id"
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Изменение погоды")
            .setContentText("Температура изменилась")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Канал для обновлений погоды",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val JOB_ID = 1000

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, WeatherUpdateService::class.java, JOB_ID, work)
        }
    }
}