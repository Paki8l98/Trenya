package space.khay.praktika

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
