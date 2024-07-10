package space.khay.newpractic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class TemperatureGraphActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var weatherService: WeatherService
    private lateinit var selectedCity: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_graph)

        dbHelper = DatabaseHelper(this)

        val apiKey = "e0b9f80d7c8e8eef6bbe438a8b201830"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)

        selectedCity = intent.getStringExtra("SELECTED_CITY") ?: ""
        val startDate = intent.getStringExtra("START_DATE") ?: ""
        val endDate = intent.getStringExtra("END_DATE") ?: ""

        Log.d("TemperatureGraphActivity", "Selected City: $selectedCity, Start Date: $startDate, End Date: $endDate")

        if (selectedCity.isNotEmpty()) {
            getTemperatureData(selectedCity, apiKey)
        }
    }

    private fun getTemperatureData(city: String, apiKey: String) {
        val call = weatherService.getWeatherForecast(city, apiKey)
        call.enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val temperatureData = extractDailyTemperatures(it)
                        if (temperatureData.isNotEmpty()) {
                            showTemperatureGraph(temperatureData)
                        } else {
                            Log.d("TemperatureGraphActivity", "No temperature data available")
                        }
                    }
                } else {
                    Log.d("TemperatureGraphActivity", "Failed to get weather data: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                Log.e("TemperatureGraphActivity", "Error fetching weather data", t)
            }
        })
    }

    private fun extractDailyTemperatures(weatherData: WeatherData): List<Triple<String, String, Double>> {
        val temperatureData = mutableListOf<Triple<String, String, Double>>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatOutput = SimpleDateFormat("HH:mm", Locale.getDefault())

        for (item in weatherData.list) {
            try {
                val date = dateFormat.parse(item.dt_txt)
                val calendar = Calendar.getInstance()
                calendar.time = date

                if (calendar.get(Calendar.HOUR_OF_DAY) == 6 || calendar.get(Calendar.HOUR_OF_DAY) == 12 || calendar.get(Calendar.HOUR_OF_DAY) == 18) {
                    val formattedDate = dateFormatOutput.format(date)
                    val formattedTime = timeFormatOutput.format(date)
                    val temperature = item.main.temp
                    temperatureData.add(Triple(formattedDate, formattedTime, temperature))
                }
            } catch (e: Exception) {
                Log.e("TemperatureGraphActivity", "Error parsing date", e)
            }
        }
        return temperatureData
    }

    private fun showTemperatureGraph(temperatureData: List<Triple<String, String, Double>>) {
        val cityAndDatesTextView: TextView = findViewById(R.id.city_and_dates_text)
        cityAndDatesTextView.text = "$selectedCity"
        val dataText = temperatureData.joinToString("\n") { "${it.first} ${it.second}: ${it.third}°C" }
        val graphContainer: FrameLayout = findViewById(R.id.graph_container)
        val temperatureGraphView = TemperatureGraphView(this, temperatureData)
        graphContainer.addView(temperatureGraphView)
    }

    class TemperatureGraphView(context: Context, private val data: List<Triple<String, String, Double>>) : View(context) {

        private val linePaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 2f
            isAntiAlias = true
        }

        private val textPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 20f
            isAntiAlias = true
        }

        private val axisPaint = Paint().apply {
            color = Color.MAGENTA
            strokeWidth = 5f
            isAntiAlias = true
        }

        private val gridPaint = Paint().apply {
            color = Color.YELLOW
            strokeWidth = 1f
            isAntiAlias = true
        }

        private val pointPaint = Paint().apply {
            color = Color.MAGENTA
            isAntiAlias = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (data.isEmpty()) {
                Log.d("TemperatureGraphView", "No data to display")
                return
            }

            val width = width.toFloat()
            val height = height.toFloat()
            val padding = 80f
            val textPadding = 40f

            val graphHeight = height - 2 * padding - textPadding * 3

            canvas.drawColor(Color.parseColor("#ADD8E6"))

            val yearMonth = data.first().first.substring(0, 7)
            textPaint.textSize = 30f
            textPaint.color = Color.BLACK
            canvas.drawText(yearMonth, width / 2, padding - textPadding, textPaint)

            val numHorizontalLines = 8
            val numVerticalLines = data.size - 1
            for (i in 0..numHorizontalLines) {
                val y = padding + i * (graphHeight / numHorizontalLines)
                canvas.drawLine(padding, y, width - padding, y, gridPaint)
            }
            for (i in 0..numVerticalLines) {
                val x = padding + i * ((width - 2 * padding) / numVerticalLines)
                canvas.drawLine(x, padding, x, height - padding - textPadding, gridPaint)
            }

            canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)
            canvas.drawLine(padding, height - padding, padding, padding, axisPaint)

            textPaint.textSize = 25f
            textPaint.color = Color.BLACK
            canvas.drawText("t,ч", width - padding / 2, height - padding / 2, textPaint)

            val yLabel = "t°C"
            textPaint.textSize = 30f
            textPaint.color = Color.BLACK

            canvas.save()
            canvas.rotate(-90f, padding / 2, height / 2)
            canvas.drawText(yLabel, -height / 2, padding - 20f, textPaint)
            canvas.restore()

            val maxTemperature = data.maxOf { it.third }
            val minTemperature = data.minOf { it.third }

            if (maxTemperature == minTemperature) {
                Log.d("TemperatureGraphView", "All temperatures are the same")
                return
            }

            val tempStep = (maxTemperature - minTemperature) / numHorizontalLines
            for (i in 0..numHorizontalLines) {
                val temp = minTemperature + i * tempStep
                val y = (height - padding - textPadding - (temp - minTemperature) * graphHeight / (maxTemperature - minTemperature)).toFloat()
                canvas.drawText(String.format("%.1f°C", temp), padding / 4, y, textPaint)
            }

            val timeLabels = data.map { it.first.substring(8) }
            val labelInterval = maxOf(1, data.size / 5)
            for (i in timeLabels.indices step labelInterval) {
                val x = (padding + i * ((width - 2 * padding) / numVerticalLines)).toFloat()
                canvas.drawText(timeLabels[i], x, height - padding + textPadding / 2, textPaint)
            }

            val points = data.mapIndexed { index, triple ->
                val x = padding + index * (width - 2 * padding) / (data.size - 1)
                val y = height - padding - textPadding - (triple.third - minTemperature) * graphHeight / (maxTemperature - minTemperature)
                x.toFloat() to y.toFloat()
            }

            for ((x, y) in points) {
                canvas.drawCircle(x, y, 8f, pointPaint)

                val textDate = data[points.indexOf(x to y)].first.substring(8) + " " + data[points.indexOf(x to y)].second
                val textTemperature = "${data[points.indexOf(x to y)].third}°C"

                val dateTextWidth = textPaint.measureText(textDate)
                val tempTextWidth = textPaint.measureText(textTemperature)

                val textDateX = x + 20f
                val textTempX = x + 20f

                canvas.drawText(textDate, textDateX.coerceIn(padding, width - padding - dateTextWidth), y - textPadding, textPaint)
                canvas.drawText(textTemperature, textTempX.coerceIn(padding, width - padding - tempTextWidth), y, textPaint)
            }

            for (i in 0 until points.size - 1) {
                val (x1, y1) = points[i]
                val (x2, y2) = points[i + 1]
                Log.d("TemperatureGraphView", "Drawing line from ($x1, $y1) to ($x2, $y2)")
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
        }
    }

}
