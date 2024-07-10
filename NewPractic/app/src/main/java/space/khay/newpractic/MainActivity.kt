package space.khay.newpractic

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var cityTextView: TextView
    private lateinit var btnWeather: Button
    private lateinit var btnMap: Button
    private lateinit var btnLocation: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedCities: MutableList<String> = mutableListOf()

    private var cityName: String? = null
    private var locationButtonClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("MainActivityPrefs", MODE_PRIVATE)

        cityTextView = findViewById(R.id.city_text_view)
        btnWeather = findViewById(R.id.btn_weather)
        btnMap = findViewById(R.id.btn_map)
        btnLocation = findViewById(R.id.btn_location)

        cityName = sharedPreferences.getString("CITY_NAME", null)
        cityName?.let {
            cityTextView.text = "Населенный пункт: $it"
            btnWeather.isEnabled = true
        }

        val savedCities = sharedPreferences.getString("SELECTED_CITIES", "")
        if (!savedCities.isNullOrEmpty()) {
            selectedCities.addAll(savedCities.split(","))
        }

        val btnShowCities: Button = findViewById(R.id.btn_show_cities)
        btnShowCities.setOnClickListener {
            showSelectedCities()
        }

        btnLocation.setOnClickListener {
            locationButtonClicked = true
            checkLocationPermissionAndGetLocation()
        }

        btnWeather.setOnClickListener {
            cityName?.let { city ->
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("CITY_NAME", city)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Сначала определите населенный пункт", Toast.LENGTH_SHORT).show()
            }
        }

        btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    handleLocation(location)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        setupPeriodicWeatherUpdate()

        checkNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        if (locationButtonClicked) {
            checkLocationPermissionAndGetLocation()
            locationButtonClicked = false
        }
        checkNotificationPermission()
    }

    private fun showSelectedCities() {
        val intent = Intent(this, SelectedCitiesActivity::class.java).apply {
            putExtra("SELECTED_CITIES", ArrayList(selectedCities))
        }
        startActivity(intent)
    }

    private fun saveCityName(city: String) {
        with(sharedPreferences.edit()) {
            putString("CITY_NAME", city)
            apply()
        }

        if (!selectedCities.contains(city)) {
            selectedCities.add(city)
        }

        val citiesString = selectedCities.joinToString(",")
        with(sharedPreferences.edit()) {
            putString("SELECTED_CITIES", citiesString)
            apply()
        }
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            if (isLocationEnabled()) {
                getLocation()
            } else {
                requestEnableLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestEnableLocation() {
        Toast.makeText(this, "Пожалуйста, включите геолокацию", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Разрешение на доступ к геопозиции не предоставлено", Toast.LENGTH_LONG).show()
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun handleLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val city = addresses?.firstOrNull()?.locality
        if (city != null && locationButtonClicked) {
            cityName = city
            cityTextView.text = "Населенный пункт: $city"
            btnWeather.isEnabled = true
            saveCityName(city)
        } else if (locationButtonClicked) {
            Toast.makeText(this, "Не удалось определить населенный пункт", Toast.LENGTH_LONG).show()
        }
        locationButtonClicked = false
    }

    private fun setupPeriodicWeatherUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_update",
            ExistingPeriodicWorkPolicy.REPLACE,
            weatherWorkRequest
        )
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        } else {
        }
    }





    private fun runWeatherWorkerManually() {
        val weatherWorkRequest = OneTimeWorkRequestBuilder<WeatherWorker>().build()
        WorkManager.getInstance(this).enqueue(weatherWorkRequest)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (isLocationEnabled()) {
                    getLocation()
                } else {
                    requestEnableLocation()
                }
            } else {
                Toast.makeText(this, "Разрешение на доступ к геопозиции не предоставлено", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Разрешение на отправку уведомлений не предоставлено", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedCity = data?.getStringExtra("SELECTED_CITY")
            selectedCity?.let {
                cityName = it
                cityTextView.text = "Населенный пункт: $it"
                btnWeather.isEnabled = true
                saveCityName(it)
            }
        }
    }

    companion object {
        private const val MAP_ACTIVITY_REQUEST_CODE = 1001
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
    }
}
