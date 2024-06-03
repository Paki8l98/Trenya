package space.khay.trenya

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlanDetailsActivity : AppCompatActivity() {

    private var userWeight: String? = null
    private var userHeight: String? = null
    private var userGender: String? = null
    private var userVozrast: String? = null
    private lateinit var startTrainingButton: Button
    private lateinit var muscleGroupSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_details)

        val intent = intent
        val wristCircumference = intent.getStringExtra("wristCircumference")
        val level = intent.getStringExtra("level")
        val muscleGroup = intent.getStringExtra("muscleGroup")
        val trainingGoal = intent.getStringExtra("trainingGoal")
        //   val userGender = intent.getStringExtra("gender")

        val planDetailsText: TextView = findViewById(R.id.planDetailsText)
        val bmiText: TextView = findViewById(R.id.bmiText)
        val bodyTypeText: TextView = findViewById(R.id.bodyTypeText)
        val wristBodyTypeText: TextView = findViewById(R.id.wristBodyTypeText)
        startTrainingButton = findViewById(R.id.startTrainingButton)
        val accountButton: Button = findViewById(R.id.accountButton)
        val nutritionButton: Button = findViewById(R.id.nutritionButton)
        muscleGroupSpinner = findViewById(R.id.muscleGroupSpinner)

        // Установить адаптер для списка
        val muscleGroupAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.muscle_groups_array,
            android.R.layout.simple_spinner_item
        )
        muscleGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        muscleGroupSpinner.adapter = muscleGroupAdapter

        // Восстановить выбранный элемент спиннера из SharedPreferences
        val sharedPreferences = getSharedPreferences("training_prefs", MODE_PRIVATE)
        val savedPosition = sharedPreferences.getInt("selected_muscle_group_position", 0)
        muscleGroupSpinner.setSelection(savedPosition)

        // Установить слушатель для обработки выбора элемента из списка
        muscleGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                updateStartButton(selectedItem)

                // Сохранить выбранный элемент спиннера в SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putInt("selected_muscle_group_position", position)
                editor.apply()

                // Обновить план в соответствии с выбранным типом тренировки
                updatePlanDetails(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Обработка случая, когда ничего не выбрано
            }
        }

        // Установить обработчики нажатий на кнопки
        accountButton.setOnClickListener {
            startActivity(Intent(this, UserDetailsActivity::class.java))
        }

        nutritionButton.setOnClickListener {
            openNutritionActivity()
        }

        // Обновление данных пользователя и планов
        updatePlanDetails(muscleGroupSpinner.selectedItem.toString())
    }

    private fun updateStartButton(selectedItem: String) {
        startTrainingButton.setOnClickListener {
            val trainingIntent = when (selectedItem) {
                "Мезоморф(Силовая)" -> Intent(this, TrainingActivity::class.java)
                "Эктоморф(Базовая)" -> Intent(this, Training1Activity::class.java)
                "Эндоморф(Похудение)" -> Intent(this, Training2Activity::class.java)
                "Кинезоитерапия" -> Intent(this, Training3Activity::class.java)
                else -> null
            }
            trainingIntent?.let { startActivity(it) }
        }
    }

    private fun calculateBMI(weight: Double?, height: Double?): Double {
        return if (weight != null && height != null && height > 0) {
            weight / (0.01 * height * height * 0.01)
        } else {
            0.0
        }
    }

    private fun determineBodyType(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Недостаточный вес (дефицит массы тела)"
            bmi < 24.9 -> "Нормальный вес (здоровый вес)"
            bmi < 29.9 -> "Избыточный вес (предожирение)"
            bmi < 34.9 -> "Ожирение I степени"
            bmi < 39.9 -> "Ожирение II степени"
            else -> "Очень высокий уровень ожирения (III степень)"
        }
    }

    private fun determineBodyTypeByWristCircumference(wristCircumference: Double): String {
        return when {
            wristCircumference < 15.0 -> "Эктоморф"
            wristCircumference < 18.0 -> "Мезоморф"
            else -> "Эндоморф"
        }
    }

    private fun updatePlanDetails(muscleGroup: String) {
        val planDetailsText: TextView = findViewById(R.id.planDetailsText)
        val dayText: TextView = findViewById(R.id.finishedCount)
        val progressText: TextView = findViewById(R.id.inProgressCount)
        val bmiText: TextView = findViewById(R.id.bmiText)
        val bodyTypeText: TextView = findViewById(R.id.bodyTypeText)
        val wristBodyTypeText: EditText = findViewById(R.id.wristBodyTypeText)
        val trainingGoalText: TextView = findViewById(R.id.trainingGoalText)
        val timeSpentCount: TextView = findViewById(R.id.timeSpentCount)

        val sharedPreferences = getSharedPreferences("training_prefs", MODE_PRIVATE)
        val selectedDay = sharedPreferences.getInt("selected_day", 0)

        // Выбор правильного SharedPreferences в зависимости от muscleGroup
        val preferencesName = when (muscleGroup) {
            "Мезоморф(Силовая)" -> "ExercisePreferences"
            "Эктоморф(Базовая)" -> "ExercisePreferences_DenTreni1"
            "Эндоморф(Похудение)" -> "ExercisePreferences_DenTreni2"
            "Кинезоитерапия" -> "ExercisePreferences_DenTreni3"
            else -> "ExercisePreferences"
        }

        val sharedPreferences2 = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val totalProgress = sharedPreferences2.getInt("total_progress", 0)

        val userPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        userWeight = userPreferences.getString("weight", "")
        userHeight = userPreferences.getString("height", "")
        userGender = userPreferences.getString("gender", "")
        userVozrast = userPreferences.getString("vozrast", "")
        val wristCircumference = userPreferences.getString("wristCircumference", "")
        val trainingGoal = userPreferences.getString("trainingGoal", "")

        dayText.text = "$selectedDay/31"
        progressText.text = "$totalProgress%"

        val planDetails = "Вес: $userWeight кг \nРост: $userHeight см \nПол: $userGender \nВозраст: $userVozrast лет"
        planDetailsText.text = planDetails

        val bmi = calculateBMI(userWeight?.toDoubleOrNull(), userHeight?.toDoubleOrNull())
        val formattedBmi = String.format("%.2f", bmi)
        bmiText.text = "$formattedBmi"

        val wristBodyType = determineBodyTypeByWristCircumference(wristCircumference?.toDoubleOrNull() ?: 0.0)
        val bodyType = determineBodyType(bmi)
        bodyTypeText.text = "$bodyType"
        timeSpentCount.text = "$wristBodyType"

        // Отображаем выбранную цель тренировки
        trainingGoalText.text = "$trainingGoal"
    }

    private fun openNutritionActivity() {
        val nutritionIntent = Intent(this, NutritionActivity::class.java)
        nutritionIntent.putExtra("weight", userWeight)
        nutritionIntent.putExtra("height", userHeight)
        nutritionIntent.putExtra("gender", userGender)
        nutritionIntent.putExtra("vozrast", userVozrast)
        startActivity(nutritionIntent)
    }

    private fun saveProgress(day: Int, progress: Int, muscleGroup: String) {
        val sharedPreferences = getSharedPreferences("training_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("selected_day", day)
        editor.apply()

        // Выбор правильного SharedPreferences в зависимости от muscleGroup
        val preferencesName = when (muscleGroup) {
            "Мезоморф(Силовая)" -> "ExercisePreferences"
            "Эктоморф(Базовая)" -> "ExercisePreferences_DenTreni1"
            "Эндоморф(Похудение)" -> "ExercisePreferences_DenTreni2"
            "Кинезоитерапия" -> "ExercisePreferences_DenTreni3"
            else -> "ExercisePreferences"
        }

        val sharedPreferences2 = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val editor2 = sharedPreferences2.edit()
        editor2.putInt("total_progress", progress)
        editor2.apply()
    }

    private fun incrementProgress(muscleGroup: String) {
        val sharedPreferences = getSharedPreferences("training_prefs", MODE_PRIVATE)
        var day = sharedPreferences.getInt("selected_day", 0)
        day++

        val preferencesName = when (muscleGroup) {
            "Мезоморф(Силовая)" -> "ExercisePreferences"
            "Эктоморф(Базовая)" -> "ExercisePreferences_DenTreni1"
            "Эндоморф(Похудение)" -> "ExercisePreferences_DenTreni2"
            "Кинезоитерапия" -> "ExercisePreferences_DenTreni3"
            else -> "ExercisePreferences"
        }

        val sharedPreferences2 = getSharedPreferences(preferencesName, MODE_PRIVATE)
        var progress = sharedPreferences2.getInt("total_progress", 0)
        progress++

        saveProgress(day, progress, muscleGroup)
        updatePlanDetails(muscleGroup)
    }

    override fun onResume() {
        super.onResume()
        updatePlanDetails(muscleGroupSpinner.selectedItem.toString())
    }
}
