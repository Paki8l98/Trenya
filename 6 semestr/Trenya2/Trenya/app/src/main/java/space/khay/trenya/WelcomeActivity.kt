package space.khay.trenya

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Проверка наличия сохраненных данных в SharedPreferences
        val preferences = getSharedPreferences("user_data", MODE_PRIVATE)
        if (preferences.contains("weight") && preferences.contains("height") &&
            preferences.contains("wristCircumference") && preferences.contains("level") &&
            preferences.contains("muscleGroup")) {

            // Если данные уже есть, переходим к PlanDetailsActivity
            val intent = Intent(this, PlanDetailsActivity::class.java)
            startActivity(intent)
            finish()  // Завершаем текущую активность, чтобы не возвращаться назад
        }

        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}




