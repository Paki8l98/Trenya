package space.khay.praktika

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherEntry(
    @PrimaryKey val id: Int,
    val city: String,
    val timestamp: Long,
    val temperature: Double
)
