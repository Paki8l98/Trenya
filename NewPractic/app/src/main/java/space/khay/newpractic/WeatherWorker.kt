package space.khay.newpractic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class WeatherWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private val sharedPreferences: SharedPreferences = appContext.getSharedPreferences("MainActivityPrefs", Context.MODE_PRIVATE)

    override fun doWork(): Result {
        Log.d("WeatherWorker", "WeatherWorker is running")

        val cityName = sharedPreferences.getString("CITY_NAME", null)
        if (cityName.isNullOrEmpty()) {
            return Result.failure()
        }

        val previousWeatherData = sharedPreferences.getString("WEATHER_DATA", null)
        val currentWeatherData = fetchWeatherData(cityName)

        if (currentWeatherData.isNotEmpty() && currentWeatherData != previousWeatherData) {
            sharedPreferences.edit().putString("WEATHER_DATA", currentWeatherData).apply()
            val previousTemp = extractTemperature(previousWeatherData)
            val currentTemp = extractTemperature(currentWeatherData)
            createNotification("Погода в $cityName изменилась", "Температура изменилась с $previousTemp°C на $currentTemp°C", previousWeatherData, currentWeatherData)
        }

        return Result.success()
    }


    private fun fetchWeatherData(cityName: String): String {
        val apiKey = "e0b9f80d7c8e8eef6bbe438a8b201830"
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey&units=metric&lang=ru"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        try {
            val response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (response.isSuccessful && responseData != null) {
                val json = JSONObject(responseData)
                val list = json.getJSONArray("list")
                val currentHour = list.getJSONObject(0)
                val main = currentHour.getJSONObject("main")
                val temp = main.getDouble("temp")
                val feelsLike = main.getDouble("feels_like")
                val humidity = main.getInt("humidity")
                val weatherArray = currentHour.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val description = weatherObject.getString("description")
                val wind = currentHour.getJSONObject("wind")
                val windSpeed = wind.getDouble("speed")

                val weatherInfo = StringBuilder()

                weatherInfo.append("Температура: $temp°C\n")
                weatherInfo.append("Ощущается как: $feelsLike°C\n")
                weatherInfo.append("Описание: $description\n")
                weatherInfo.append("Влажность: $humidity%\n")
                weatherInfo.append("Скорость ветра: $windSpeed м/с\n")

                return weatherInfo.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }


    private fun extractTemperature(weatherData: String?): String {
        return weatherData?.substringAfter("Температура: ")?.substringBefore("°C") ?: "N/A"
    }

    private fun createNotification(title: String, text: String, previousWeatherData: String?, currentWeatherData: String) {
        val channelId = "weather_channel_id"
        val channelName = "Обновления погоды"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Канал для обновлений погоды"
            }

            val notificationManager: NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, WeatherDetailActivity::class.java).apply {
            putExtra("CURRENT_WEATHER_DATA", currentWeatherData)
            putExtra("PREVIOUS_WEATHER_DATA", previousWeatherData)
            putExtra("CITY_NAME", sharedPreferences.getString("CITY_NAME", ""))
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(applicationContext.getColor(R.color.black))

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }

}
