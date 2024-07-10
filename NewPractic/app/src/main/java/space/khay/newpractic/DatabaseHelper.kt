package space.khay.newpractic

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Weather.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "cities"
        const val COLUMN_ID = "id"
        const val COLUMN_CITY = "city"
        const val COLUMN_DATE = "date"
        const val COLUMN_TEMPERATURE = "temperature"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_CITY TEXT,"
                + "$COLUMN_DATE TEXT,"
                + "$COLUMN_TEMPERATURE REAL)")
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertCityData(city: String, date: String, temperature: Double) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_CITY, city)
        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_TEMPERATURE, temperature)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }
}
