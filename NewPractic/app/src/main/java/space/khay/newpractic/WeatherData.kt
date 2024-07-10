package space.khay.newpractic

data class WeatherData(
    val list: List<WeatherItem1>
)

data class WeatherItem1(
    val dt_txt: String,
    val main: Main
)

data class Main(
    val temp: Double
)

data class DailyWeatherData(
    val list: List<DailyWeatherItem>
)

data class DailyWeatherItem(
    val dt: Long,
    val temp: Temp
)

data class Temp(
    val day: Double
)

