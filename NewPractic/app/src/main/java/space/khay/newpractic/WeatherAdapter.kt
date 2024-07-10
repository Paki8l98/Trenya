package space.khay.newpractic

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class WeatherAdapter(private val weatherList: List<WeatherItem>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weatherTimeTextView: TextView = view.findViewById(R.id.weather_time_text_view)
        val weatherTempTextView: TextView = view.findViewById(R.id.weather_temp_text_view)
        val weatherIconImageView: ImageView = view.findViewById(R.id.weather_icon_image_view)
        //    val weatherHourlyDescriptionTextView: TextView = view.findViewById(R.id.weather_hourly_description_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weatherItem = weatherList[position]
        holder.weatherTimeTextView.text = weatherItem.dateTime.split(" ")[1]
        holder.weatherTempTextView.text = "${weatherItem.temp}°C"
        val iconResId = getWeatherIconResourceDay(weatherItem.weatherHourlyDescription, holder.itemView.context)

        Log.d("WeatherAdapter", "Description: ${weatherItem.weatherHourlyDescription}, IconResId: $iconResId")

        holder.weatherIconImageView.setImageResource(iconResId)
    }


    private fun getWeatherIconResourceDay(weatherHourlyDescription: String, context: Context): Int {
        val weatherCondition = weatherHourlyDescription.substringAfter(": ").toLowerCase(Locale.getDefault())
        Log.d("WeatherAdapter", "Описание погоды: $weatherCondition")

        return when (weatherCondition) {
            "ясно" -> R.drawable.ic_clear_sky
            "малооблачно" -> R.drawable.overcast
            "облачно с прояснениями" -> R.drawable.middle
            "пасмурно" -> R.drawable.pasm
            "переменная облачность" -> R.drawable.perobl
            "небольшой дождь" -> R.drawable.cloud
            "небольшая облачность" -> R.drawable.perobl
            else -> {
                Log.d("WeatherAdapter", "Не найдена иконка для: $weatherCondition")
                R.drawable.ic_launcher_background
            }
        }
    }



    override fun getItemCount() = weatherList.size
}