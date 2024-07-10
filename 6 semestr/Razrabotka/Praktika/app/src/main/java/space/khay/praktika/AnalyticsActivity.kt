package space.khay.praktika

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class AnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val graph = findViewById<GraphView>(R.id.graph)
        val series = LineGraphSeries<DataPoint>()

        // Получите данные из базы данных и добавьте их в серию
        val weatherDao = (application as WeatherApp).database.weatherDao()
        val weatherEntries = weatherDao.getWeatherForCity("Selected City")

        for (entry in weatherEntries) {
            series.appendData(DataPoint(entry.timestamp.toDouble(), entry.temperature), true, weatherEntries.size)
        }

        graph.addSeries(series)
    }
}

