package space.khay.praktika

import android.app.Application
import androidx.room.Room

class WeatherApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "weather-db").build()
    }
}
