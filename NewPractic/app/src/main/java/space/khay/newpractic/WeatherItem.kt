package space.khay.newpractic

data class WeatherItem(
    val dateTime: String,
    val temp: Int,
    val icon: String,
    val dayOfWeek: String, // День недели
    val dayOfMonth: String, // Число месяца
    val weatherDescription: String, // Описание погоды на основе основного прогноза
    var weatherHourlyDescription: String = "" // Описание погоды на каждый час
)