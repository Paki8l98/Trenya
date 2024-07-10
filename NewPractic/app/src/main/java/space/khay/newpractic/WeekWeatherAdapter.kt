package space.khay.newpractic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class WeekWeatherAdapter(private val weatherList: List<WeatherItem>) : RecyclerView.Adapter<WeekWeatherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weekDayTextView: TextView = view.findViewById(R.id.week_day_text_view)
        val weekTempTextView: TextView = view.findViewById(R.id.week_temp_text_view)
        val weekIconImageView: ImageView = view.findViewById(R.id.week_icon_image_view)
        val weekDateTextView: TextView = view.findViewById(R.id.week_date_text_view)
        val weekWeatherStateTextView: TextView = view.findViewById(R.id.week_weather_state_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.week_weather_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weatherItem = weatherList[position]
        holder.weekDayTextView.text = weatherItem.dayOfWeek
        holder.weekTempTextView.text = "${weatherItem.temp}°C"
        val formattedDate = "${weatherItem.dayOfMonth} ${getMonthName(weatherItem.dateTime)}"
        holder.weekDateTextView.text = formattedDate
        val iconResId = getWeatherIconResource(weatherItem.weatherDescription, holder.itemView.context)
        holder.weekIconImageView.setImageResource(iconResId)
        holder.weekWeatherStateTextView.text = weatherItem.weatherDescription
    }

    override fun getItemCount() = weatherList.size

    private fun getMonthName(dateTime: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTime)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(date!!)
    }

    private fun getWeatherIconResource(weatherDescription: String, context: Context): Int {
        return when (weatherDescription.toLowerCase(Locale.getDefault())) {
            "ясно" -> R.drawable.ic_clear_sky
            "малооблачно" -> R.drawable.overcast
            "облачно с прояснениями" -> R.drawable.middle
            "пасмурно" -> R.drawable.pasm
            "переменная облачность" -> R.drawable.perobl
//            "дождь" -> R.drawable.ic_rain
//            "ливень" -> R.drawable.ic_shower_rain
//            "снег" -> R.drawable.ic_snow
//            "гроза" -> R.drawable.ic_thunderstorm
//            "туман" -> R.drawable.ic_mist
            "небольшой дождь" -> R.drawable.cloud
            "небольшая облачность" -> R.drawable.perobl
            else -> R.drawable.ic_launcher_background
        }
    }

}