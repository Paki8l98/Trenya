package space.khay.trenya

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExerciseActivity1 : AppCompatActivity() {

    private lateinit var exerciseImage: ImageView
    private lateinit var exerciseNameTextView: TextView
    private lateinit var exerciseDurationTextView: TextView
    private lateinit var exerciseRecommendationTextView: TextView
    private lateinit var exerciseSetsTextView: TextView
    private lateinit var exerciseNumberTextView: TextView
    private lateinit var currentSetTextView: TextView
    private lateinit var exerciseRepetitionsTextView: TextView
    private lateinit var nextExerciseButton: Button
    private lateinit var startTimerButton: Button
    private lateinit var nextSetButton: Button
    private lateinit var exerciseWeightTextView: TextView
    private var currentSet = 1

    private var maxSets: Int = 0

    private lateinit var exercises: Array<Exercise>
    private var currentIndex = 0
    private lateinit var currentExercise: Exercise
    private lateinit var timer: CountDownTimer

    private var repsLow: Int = 0
    private var repsHigh: Int = 0
    private var initialWeight: Double = 0.0
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        exerciseImage = findViewById(R.id.exerciseImage)
        exerciseNameTextView = findViewById(R.id.exerciseNameTextView)
        exerciseDurationTextView = findViewById(R.id.exerciseDurationTextView)
        exerciseRecommendationTextView = findViewById(R.id.exerciseRecommendationTextView)
        exerciseSetsTextView = findViewById(R.id.exerciseSetsTextView)
        exerciseNumberTextView = findViewById(R.id.exerciseNumberTextView)
        currentSetTextView = findViewById(R.id.currentSetTextView)
        exerciseRepetitionsTextView = findViewById(R.id.exerciseRepetitionsTextView)
        nextExerciseButton = findViewById(R.id.nextExerciseButton)
        startTimerButton = findViewById(R.id.startTimerButton)
        nextSetButton = findViewById(R.id.nextSetButton)
        exerciseWeightTextView = findViewById(R.id.exerciseWeightTextView)

        val exercisesList: ArrayList<Exercise>? = intent.getParcelableArrayListExtra("exercises")
        exercises = exercisesList?.toTypedArray() ?: emptyArray()

        currentIndex = intent.getIntExtra("current_index", 0)

        if (exercises.isNotEmpty()) {
            currentExercise = exercises[currentIndex]
            showExercise(currentIndex)
        } else {
            finish()
        }

        val sharedPreferences: SharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val trainingGoal: String? = sharedPreferences.getString("trainingGoal", "")

        val repsText = when (trainingGoal) {
            "Наращивание мышечной массы" -> {
                repsLow = 4
                repsHigh = 12
                "Количество повторений: $repsHigh"
            }
            "Для придания рельефа мышцам" -> {
                repsLow = 8
                repsHigh = 20
                "Количество повторений: $repsHigh"
            }
            "Для сжигания жира" -> {
                repsLow = 20
                repsHigh = 25
                "Количество повторений: $repsHigh"
            }
            "Для выносливости" -> {
                repsLow = 25
                repsHigh = 30
                "Количество повторений: $repsHigh"
            }
            else -> "нету"
        }

        exerciseRepetitionsTextView.text = repsText

        val exerciseId = intent.getStringExtra("exercise_id") ?: ""
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("exercises").child(exerciseId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exercise: Exercise? = dataSnapshot.getValue(Exercise::class.java)
                exercise?.let {
                    initialWeight = it.weight
                    //       exerciseWeightTextView.text = "Вес упражнения: $initialWeight кг"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ExerciseActivity1, "Ошибка загрузки данных: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

        startTimerButton.setOnClickListener {
            if (!isTimerRunning) { // Проверка, не запущен ли уже таймер
                startExerciseTimer(currentExercise.durationSeconds)
                isTimerRunning = true // Установка флага запуска таймера
            }
        }


        nextExerciseButton.setOnClickListener {
            currentIndex++
            currentSet = 1
            if (currentIndex < exercises.size) {
                if (::timer.isInitialized) {
                    timer.cancel()
                    isTimerRunning = false
                }
                showExercise(currentIndex)
            } else {
                finishActivity()
            }
        }


        nextSetButton.setOnClickListener {
            if (currentSet < currentExercise.sets) {
                currentSet++
                if (::timer.isInitialized) {
                    timer.cancel()
                    isTimerRunning = false
                }
                showExercise(currentIndex)
            } else {
                currentSet = 1
                currentIndex++
                if (::timer.isInitialized) {
                    timer.cancel()
                    isTimerRunning = false
                }
                if (currentIndex < exercises.size) {
                    showExercise(currentIndex)
                } else {
                    finishActivity()
                }
            }
        }

    }

    private fun showExercise(index: Int) {
        maxSets = currentExercise.sets
        currentExercise = exercises[index]
        exerciseSetsTextView.text = "Количество подходов: ${currentExercise.sets}"
        exerciseNameTextView.text = currentExercise.name
        exerciseDurationTextView.text = "Длительность: ${currentExercise.durationSeconds} секунд"
        exerciseRecommendationTextView.text = "Рекомендации: ${currentExercise.recommendations}"

        initialWeight = currentExercise.weight

        val currentWeight = initialWeight + 2 * (currentSet - 1)
        exerciseWeightTextView.text = "Рекомендуемый вес: $currentWeight кг"

        val exerciseNumber = index + 1
        val totalExercises = exercises.size
        exerciseNumberTextView.text = "Упражнение $exerciseNumber/$totalExercises"

        currentSet = if (currentSet > currentExercise.sets) currentExercise.sets else currentSet
        currentSetTextView.text = "Подход $currentSet из ${currentExercise.sets}"

        val currentRepetitions = if (currentSet == 1) {
            repsHigh
        } else {
            val remainingSets = currentExercise.sets - currentSet + 1
            val targetRepetitions = repsLow + (repsHigh - repsLow) * remainingSets / currentExercise.sets
            targetRepetitions.coerceIn(repsLow, repsHigh)
        }
        exerciseRepetitionsTextView.text = "$currentRepetitions Повторений требуется"

        Glide.with(this)
            .asGif()
            .load(currentExercise.gifPath)
            .into(exerciseImage)

        val sharedPreferences: SharedPreferences = getSharedPreferences("ExercisePreferences_DenTreni1", MODE_PRIVATE)
        sharedPreferences.edit().putInt("current_index", currentIndex).apply()
        val totalProgress = ((currentIndex + 1).toFloat() / exercises.size.toFloat() * 100).toInt()
        sharedPreferences.edit().putInt("total_progress", totalProgress).apply()

        startTimerButton.isEnabled = !isTimerRunning
    }





    override fun onBackPressed() {
        finishActivity()
        super.onBackPressed()
    }

    private fun sendProgressToTrainingActivity() {
        val totalProgress = ((currentIndex + 1).toFloat() / exercises.size * 100).toInt()
        val intent = Intent()
        intent.putExtra("progress", totalProgress)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun startTimerOnClick(view: View) {
        startExerciseTimer(currentExercise.durationSeconds)
    }

    private fun startExerciseTimer(duration: Int) {
        timer = object : CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseDurationTextView.text = "Длительность: ${millisUntilFinished / 1000} секунд"
            }

            override fun onFinish() {
                isTimerRunning = false // Сброс флага запуска таймера
                if (currentSet < currentExercise.sets) {
                    currentSet++
                    showExercise(currentIndex)
                } else {
                    currentIndex++
                    currentSet = 1
                    if (currentIndex < exercises.size) {
                        showExercise(currentIndex)
                    } else {
                        finishActivity()
                    }
                }
            }
        }.start()
    }

    private fun finishActivity() {
        val totalProgress = ((currentIndex.toFloat() / exercises.size.toFloat()) * 100).toInt()
        val sharedPreferences = getSharedPreferences("ExercisePreferences_DenTreni1", MODE_PRIVATE)
        sharedPreferences.edit().putInt("current_index", currentIndex).apply() // Сохранение текущего индекса упражнения
        sharedPreferences.edit().putInt("total_progress", totalProgress).apply()
        val intent = Intent()
        intent.putExtra("progress", totalProgress)
        intent.putExtra("day_index", currentIndex)
        setResult(Activity.RESULT_OK, intent)

        if (currentIndex >= exercises.size - 1) {
            showReportAlertDialog()
        } else {
            sendProgressToTrainingActivity()
            finish()
        }
    }

    private fun showReportAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Отчет о тренировке")

        val totalSets = exercises.sumBy { it.sets }
        val totalReps = exercises.sumBy { it.sets * it.repetitions }
        val totalCaloriesBurned = calculateTotalCaloriesBurned(exercises)

        val exerciseInfoList = exercises.mapIndexed { index, exercise ->
            "Упражнение ${index + 1}: ${exercise.name}\n" +
                    "Подходы: ${exercise.sets}\n" +
                    "Повторения: ${exercise.repetitions}\n" +
                    "Вес: ${exercise.weight} кг\n" +
                    "Время выполнения: ${exercise.durationSeconds} сек\n"
        }.joinToString(separator = "\n")

        val reportMessage = "Вы выполнили в сумме:\n" +
                "$totalSets подходов\n" +
                "$totalReps повторений.\n" +
                "Расход калорий: $totalCaloriesBurned ккал.\n\n" +
                "Информация по упражнениям:\n" +
                exerciseInfoList

        builder.setMessage(reportMessage)

        builder.setPositiveButton("OK") { dialog, _ ->
            val exerciseSharedPreferences = getSharedPreferences("exercise_data", MODE_PRIVATE)
            val editor = exerciseSharedPreferences.edit()
            editor.putInt("caloriesBurned", totalCaloriesBurned)
            editor.apply()
            dialog.dismiss()
            sendProgressToTrainingActivity()
            finish()
        }

        builder.setNegativeButton("Поделиться") { dialog, _ ->
            shareReport(reportMessage)
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun shareReport(reportMessage: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
    }



    private fun calculateTotalCaloriesBurned(exercises: Array<Exercise>): Int {
        var totalCalories = 0

        for (exercise in exercises) {
            val caloriesPerSet = 5
            val caloriesPerRep = 2

            val exerciseCalories = exercise.sets * caloriesPerSet + exercise.sets * exercise.repetitions * caloriesPerRep
            totalCalories += exerciseCalories
        }

        return totalCalories
    }
}

