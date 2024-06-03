package space.khay.trenya

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.*

class DenTreni1Activity : AppCompatActivity() {

    private lateinit var dayTitleTextView: TextView
    private lateinit var exerciseCountTextView: TextView
    private lateinit var recyclerViewExercises: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference
    private var selectedDay: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_den_treni)

        dayTitleTextView = findViewById(R.id.dayTitleTextView)
        exerciseCountTextView = findViewById(R.id.exerciseCountTextView)
        recyclerViewExercises = findViewById(R.id.recyclerViewExercises)

        sharedPreferences = getSharedPreferences("ExercisePreferences_DenTreni1", MODE_PRIVATE)

        val selectedDay = intent.getStringExtra("selected_day")
        val dayOfWeekString = intent.getStringExtra("day_of_week_string")

        if (!selectedDay.isNullOrEmpty()) {
            dayTitleTextView.text = "День $selectedDay"

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay.toInt())
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayOfWeekString = when (currentDayOfWeek) {
                Calendar.MONDAY -> "Понедельник"
                Calendar.TUESDAY -> "Вторник"
                Calendar.WEDNESDAY -> "Среда"
                Calendar.THURSDAY -> "Четверг"
                Calendar.FRIDAY -> "Пятница"
                Calendar.SATURDAY -> "Суббота"
                Calendar.SUNDAY -> "Воскресенье"
                else -> "Неизвестно"
            }

            databaseReference = FirebaseDatabase.getInstance().reference.child("exercises")
            getExercisesForDay(selectedDay, dayOfWeekString)
        }
    }

    private fun startExerciseActivity(exercises: List<Exercise>, currentIndex: Int) {
        val intent = Intent(this, ExerciseActivity1::class.java)
        intent.putParcelableArrayListExtra("exercises", ArrayList(exercises))
        intent.putExtra("current_index", currentIndex)
        startActivity(intent)
    }


    private fun navigateToCustomWeekActivity() {
        val intent = Intent(this, CustomWeekActivity::class.java)
        startActivity(intent)
    }

    private fun getExercisesForDay(selectedDay: String, dayOfWeekString: String) {
        val savedExercisesJson = sharedPreferences.getString("exercises_$selectedDay", null)
        if (savedExercisesJson != null) {
            val savedExercises = Gson().fromJson(savedExercisesJson, Array<Exercise>::class.java).toList()
            displayExercises(savedExercises)
        } else {
            val muscleGroups = mapOf(
                "Понедельник" to listOf("Новичок"),
                "Среда" to listOf("Новичок"),
                "Пятница" to listOf("Новичок")
            )
            val selectedMuscleGroups = muscleGroups[dayOfWeekString]
            if (selectedMuscleGroups != null) {
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val exercises = mutableListOf<Exercise>()
                            val random = Random()
                            for (group in selectedMuscleGroups) {
                                val groupExercises = mutableListOf<Exercise>()
                                for (exerciseSnapshot in snapshot.children) {
                                    val exercise = exerciseSnapshot.getValue(Exercise::class.java)
                                    if (exercise != null && exercise.muscleGroup == group) {
                                        groupExercises.add(exercise)
                                    }
                                }
                                groupExercises.shuffle(random)
                                exercises.addAll(groupExercises.take(2))
                            }
                            val editor = sharedPreferences.edit()
                            val exercisesJson = Gson().toJson(exercises)
                            editor.putString("exercises_$selectedDay", exercisesJson)
                            editor.apply()
                            displayExercises(exercises)
                        } else {
                            Log.d("TAG", "No exercises found for selected day: $selectedDay")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TAG", "Failed to read exercises for selected day: $selectedDay", error.toException())
                    }
                })
            } else {
                Log.e("TAG", "No muscle groups found for selected day: $selectedDay")
            }
        }
    }

    private fun displayExercises(exercises: List<Exercise>) {
        exerciseCountTextView.text = "Количество упражнений: ${exercises.size}"
        recyclerViewExercises.layoutManager = LinearLayoutManager(this@DenTreni1Activity)
        recyclerViewExercises.adapter = ExercisesAdapter(exercises)

        val createCustomWeekButton: Button = findViewById(R.id.createCustomWeekButton)
        createCustomWeekButton.setOnClickListener {
            navigateToCustomWeekActivity()
        }

        val restartButton: Button = findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            startExerciseActivity(exercises, 0)
        }

        val continueButton: Button = findViewById(R.id.continueButton)
        continueButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("ExercisePreferences_DenTreni1", MODE_PRIVATE)
            val currentIndex = sharedPreferences.getInt("current_index", 0)

            startExerciseActivity(exercises, currentIndex)
        }
    }

}
