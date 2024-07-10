package space.khay.praktika

data class WeatherResponse(
    val list: List<WeatherData>
)

data class WeatherData(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double
)

data class Weather(
    val description: String
)
