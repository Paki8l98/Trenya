package space.khay.newpractic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SelectedCitiesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var showGraphButton: Button
    private lateinit var citiesRecyclerView: RecyclerView
    private lateinit var citiesAdapter: CitiesAdapter

    private val apiKey = "e0b9f80d7c8e8eef6bbe438a8b201830" // API ключ
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_cities)

        showGraphButton = findViewById(R.id.show_graph_button)
        citiesRecyclerView = findViewById(R.id.cities_recycler_view)
        dbHelper = DatabaseHelper(this)

        val selectedCities = intent.getStringArrayListExtra("SELECTED_CITIES")
        if (!selectedCities.isNullOrEmpty()) {
            val sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putStringSet("SELECTED_CITIES", selectedCities.toSet())
            editor.apply()

            citiesRecyclerView.layoutManager = LinearLayoutManager(this)
            citiesAdapter = CitiesAdapter(selectedCities) { city ->
                val intent = Intent(this, TemperatureGraphActivity::class.java)
                intent.putExtra("SELECTED_CITY", city)
                intent.putExtra("START_DATE", "2023-06-01")
                intent.putExtra("END_DATE", "2023-06-30")
                startActivity(intent)
            }
            citiesRecyclerView.adapter = citiesAdapter

            for (city in selectedCities) {
                fetchWeatherData(city)
            }

            showGraphButton.setOnClickListener {
                if (selectedCities.isNotEmpty()) {
                    val selectedCity = selectedCities[0]
                    val intent = Intent(this, TemperatureGraphActivity::class.java)
                    intent.putExtra("SELECTED_CITY", selectedCity)
                    intent.putExtra("START_DATE", "2023-06-01")
                    intent.putExtra("END_DATE", "2023-06-30")
                    startActivity(intent)
                }
            }
        }
    }

    private fun fetchWeatherData(city: String) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$city&appid=$apiKey&units=metric&lang=ru"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SelectedCitiesActivity", "Error fetching weather data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonResponse ->
                    val jsonObject = Gson().fromJson(jsonResponse, JsonObject::class.java)
                    val forecastList = jsonObject.getAsJsonArray("list")

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    for (forecast in forecastList) {
                        val date = forecast.asJsonObject.get("dt_txt").asString
                        val temp = forecast.asJsonObject.getAsJsonObject("main").get("temp").asDouble
                        dbHelper.insertCityData(city, date, temp)
                    }
                }
            }
        })
    }
}
