package space.khay.newpractic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WeatherDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_detail)

        val previousWeatherData = intent.getStringExtra("PREVIOUS_WEATHER_DATA")
        val currentWeatherData = intent.getStringExtra("CURRENT_WEATHER_DATA")
        val cityName = intent.getStringExtra("CITY_NAME")

        val cityNameTextView: TextView = findViewById(R.id.city_name_text_view)
        val previousWeatherTextView: TextView = findViewById(R.id.previous_weather_text_view)
        val currentWeatherTextView: TextView = findViewById(R.id.current_weather_text_view)
        val goToWeatherActivityButton: Button = findViewById(R.id.go_to_weather_activity_button)

        cityNameTextView.text = cityName ?: "Нет данных"
        previousWeatherTextView.text = "Была: $previousWeatherData" ?: "Была: Нет данных"
        currentWeatherTextView.text = "Стала: $currentWeatherData" ?: "Стала: Нет данных"

        goToWeatherActivityButton.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
        }
    }
}
