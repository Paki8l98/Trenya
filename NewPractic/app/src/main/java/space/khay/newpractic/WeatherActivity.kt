package space.khay.newpractic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class WeatherActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var weatherCityTextView: TextView
    private lateinit var weatherTempTextView: TextView
    private lateinit var weatherMinMaxTextView: TextView
    private lateinit var weatherIconImageView: ImageView
    private lateinit var weatherRecyclerView: RecyclerView
    private lateinit var weekRecyclerView: RecyclerView
    private lateinit var dayOfWeekTextView: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private var currentWeatherDescription: String = ""
    private val CHANNEL_ID = "weather_channel_id"
    private val CHANNEL_NAME = "Обновления погоды"
    private val NOTIFICATION_ID = 101
    private val PERMISSION_REQUEST_CODE = 1
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var lastKnownTemp: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        weatherCityTextView = findViewById(R.id.weather_city_text_view)
        weatherTempTextView = findViewById(R.id.weather_temp_text_view)
        weatherMinMaxTextView = findViewById(R.id.weather_min_max_text_view)
        weatherRecyclerView = findViewById(R.id.weather_recycler_view)
        weekRecyclerView = findViewById(R.id.week_recycler_view)
        dayOfWeekTextView = findViewById(R.id.day_of_week_text_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        sharedPreferences = getSharedPreferences("MainActivityPrefs", MODE_PRIVATE)

        val cityName = sharedPreferences.getString("CITY_NAME", null)
        if (cityName != null) {
            weatherCityTextView.text = cityName
            getWeather(cityName)
            showNotification("Погода в $cityName", "Загрузка данных о погоде...")
        } else {
            Toast.makeText(this, "Имя города не найдено", Toast.LENGTH_SHORT).show()
        }

        checkNotificationPermission()

        weatherRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        weekRecyclerView.layoutManager = LinearLayoutManager(this)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            true
        }

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                getWeather(cityName ?: "")
                handler.postDelayed(this, 60 * 60 * 1000) // Интервал уведомлений
            }
        }

        handler.postDelayed(runnable, 60 * 60 * 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_weather -> {
            }
            R.id.nav_account -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_analytics -> {
                val sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                val selectedCitiesSet = sharedPreferences.getStringSet("SELECTED_CITIES", emptySet()) ?: emptySet()
                val selectedCities = selectedCitiesSet.toList()

                val intent = Intent(this, SelectedCitiesActivity::class.java)
                intent.putStringArrayListExtra("SELECTED_CITIES", ArrayList(selectedCities))
                startActivity(intent)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            } else {
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeather(city: String) {
        val apiKey = "e0b9f80d7c8e8eef6bbe438a8b201830" // API ключ
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$city&appid=$apiKey&units=metric&lang=ru"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@WeatherActivity, "Не удалось получить данные о погоде", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@WeatherActivity, "Ошибка сервера: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                        throw IOException("Unexpected code $response")
                    }

                    val responseData = response.body!!.string()
                    val json = JSONObject(responseData)
                    val list = json.getJSONArray("list")

                    val cityJson = json.getJSONObject("city")
                    val cityName = cityJson.getString("name")

                    val currentWeatherJson = list.getJSONObject(0)
                    val currentTemp = currentWeatherJson.getJSONObject("main").getDouble("temp").toInt()

                    val todayWeatherList = mutableListOf<WeatherItem>()
                    val weekWeatherList = mutableListOf<WeatherItem>()

                    var currentMinTemp = currentTemp
                    var currentMaxTemp = currentTemp

                    var currentDate = ""
                    val calendar = Calendar.getInstance()

                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)
                        val dateTime = item.getString("dt_txt")
                        val temp = item.getJSONObject("main").getDouble("temp").toInt()
                        val icon = item.getJSONArray("weather").getJSONObject(0).getString("icon")
                        val weatherDescription = item.getJSONArray("weather").getJSONObject(0).getString("description")

                        if (dateTime.startsWith(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))) {
                            if (temp < currentMinTemp) {
                                currentMinTemp = temp
                            }
                            if (temp > currentMaxTemp) {
                                currentMaxTemp = temp
                            }
                        }

                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTime)
                        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date!!)
                        val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(date)
                        val hourOfDay = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

                        if (dateTime.endsWith("12:00:00") && currentDate != dateTime.substring(0, 10) && weekWeatherList.size < 7) {
                            weekWeatherList.add(WeatherItem(dateTime, temp, icon, dayOfWeek, dayOfMonth, weatherDescription, ""))
                            currentDate = dateTime.substring(0, 10)
                            calendar.add(Calendar.DATE, 1)
                        }

                        if (dateTime.startsWith(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))) {
                            todayWeatherList.add(WeatherItem(dateTime, temp, icon, "", "", weatherDescription, "$hourOfDay: $weatherDescription"))
                        }
                    }

                    runOnUiThread {
                        weatherCityTextView.text = cityName
                        weatherTempTextView.text = "$currentTemp°C"
                        weatherMinMaxTextView.text = "$currentMaxTemp°C/$currentMinTemp°C"
                        dayOfWeekTextView.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

                        weatherRecyclerView.adapter = WeatherAdapter(todayWeatherList)
                        weekRecyclerView.adapter = WeekWeatherAdapter(weekWeatherList)

                        if (lastKnownTemp != null && lastKnownTemp != currentTemp) {
                            updateNotification("Погода в $cityName", "$currentTemp°C, $currentWeatherDescription")
                        }
                        lastKnownTemp = currentTemp
                    }

                }
            }
        })
    }

    private fun showNotification(title: String, message: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            createNotificationChannel()

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun updateNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Канал для обновлений погоды"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
