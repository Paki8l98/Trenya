package space.khay.newpractic

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import java.util.*

class MapActivity : AppCompatActivity(), MapEventsReceiver {

    private lateinit var map: MapView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private var markers: MutableList<Marker> = mutableListOf()
    private var selectedPoint: GeoPoint? = null
    private var selectedCity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_map)

        map = findViewById(R.id.map_view)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(9.5)
        val startPoint = GeoPoint(54.9014, 52.2978) // Альметьевск
        mapController.setCenter(startPoint)

        val mapEventsOverlay = MapEventsOverlay(this)
        map.overlays.add(0, mapEventsOverlay)

        sharedPreferences = getSharedPreferences("MapActivityPrefs", MODE_PRIVATE)

        val markerLatitude = sharedPreferences.getFloat("MARKER_LATITUDE", 0f).toDouble()
        val markerLongitude = sharedPreferences.getFloat("MARKER_LONGITUDE", 0f).toDouble()
        if (markerLatitude != 0.0 && markerLongitude != 0.0) {
            val markerPosition = GeoPoint(markerLatitude, markerLongitude)
            placeMarker(markerPosition)
        }

        searchEditText = findViewById(R.id.search_edit_text)
        searchButton = findViewById(R.id.search_button)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString()
            if (searchText.isNotEmpty()) {
                searchLocation(searchText)
            } else {
                Toast.makeText(this, "Введите название населенного пункта", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        selectedPoint?.let {
            with(sharedPreferences.edit()) {
                putFloat("MARKER_LATITUDE", it.latitude.toFloat())
                putFloat("MARKER_LONGITUDE", it.longitude.toFloat())
                apply()
            }
        }

        selectedCity?.let {
            with(sharedPreferences.edit()) {
                putString("SELECTED_CITY", it)
                apply()
            }
        }
    }

    private fun placeMarker(position: GeoPoint) {
        markers.forEach { map.overlays.remove(it) }
        markers.clear()

        val marker = Marker(map)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.setOnMarkerClickListener { clickedMarker, mapView ->
            handleMarkerClick(clickedMarker)
            true
        }
        map.overlays.add(marker)
        markers.add(marker)
        map.invalidate()

        selectedPoint = position

        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1)
        val city = addresses?.firstOrNull()?.locality

        selectedCity = city

        Toast.makeText(this, "Выбран населенный пункт: $city", Toast.LENGTH_LONG).show()
    }

    private fun handleMarkerClick(clickedMarker: Marker) {
        selectedPoint = clickedMarker.position

        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(clickedMarker.position.latitude, clickedMarker.position.longitude, 1)
        val city = addresses?.firstOrNull()?.locality

        selectedCity = city

        Toast.makeText(this, "Выбран населенный пункт: $city", Toast.LENGTH_LONG).show()
    }

    private fun searchLocation(searchText: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocationName(searchText, 10)
        if (!addresses.isNullOrEmpty()) {
            markers.forEach { map.overlays.remove(it) }
            markers.clear()

            addresses.forEach { address ->
                val position = GeoPoint(address.latitude, address.longitude)
                val marker = Marker(map)
                marker.position = position
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.setOnMarkerClickListener { clickedMarker, mapView ->
                    handleMarkerClick(clickedMarker)
                    true
                }
                map.overlays.add(marker)
                markers.add(marker)
            }

            map.controller.animateTo(markers.first().position)
            map.invalidate()
        } else {
            Toast.makeText(this, "Населенный пункт не найден", Toast.LENGTH_SHORT).show()
        }
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        p?.let {
            placeMarker(it)
        }
        return true
    }

    override fun onBackPressed() {
        if (selectedCity != null) {
            val resultIntent = Intent().apply {
                putExtra("SELECTED_CITY", selectedCity)
            }
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        super.onBackPressed()
    }
}
