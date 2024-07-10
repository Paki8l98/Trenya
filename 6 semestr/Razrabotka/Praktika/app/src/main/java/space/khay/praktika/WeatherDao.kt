package space.khay.praktika

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weatherentry WHERE city = :city")
    fun getWeatherForCity(city: String): List<WeatherEntry>

    @Insert
    fun insertAll(vararg weatherEntries: WeatherEntry)
}
