package space.khay.trenya

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class TrainingActivity : AppCompatActivity() {

    private lateinit var recyclerViewDays: RecyclerView
    private var totalProgress: Int = 0
    private lateinit var adapter: DayAdapter
    private lateinit var databaseReference: DatabaseReference
    private var activityType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        activityType = intent.getIntExtra("activity_type", 0)

        recyclerViewDays = findViewById(R.id.recyclerViewDays)
        recyclerViewDays.layoutManager = LinearLayoutManager(this)

        adapter = DayAdapter(activityType)
        recyclerViewDays.adapter = adapter

        // Инициализация базы данных Firebase
        databaseReference = FirebaseDatabase.getInstance().reference.child("exercises")

        // Получение упражнений из Firebase Realtime Database
        getExercisesFromFirebase()
    }

    private fun getExercisesFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val exercises = mutableListOf<Exercise>()
                    for (exerciseSnapshot in snapshot.children) {
                        val exercise = exerciseSnapshot.getValue(Exercise::class.java)
                        exercise?.let { exercises.add(it) }
                    }
                    // Обновление адаптера RecyclerView с новым списком упражнений
                    adapter.updateExercises(exercises)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getCurrentMuscleGroupAndDay(dayOfMonth: Int): Triple<String, String, String> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
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

        val muscleGroup = when (activityType) {
            0 -> {
                when (currentDayOfWeek) {
                    Calendar.MONDAY -> "Грудь и бицепс"
                    Calendar.TUESDAY -> "Отдых"
                    Calendar.WEDNESDAY -> "Спина и трицепс"
                    Calendar.THURSDAY -> "Отдых"
                    Calendar.FRIDAY -> "Ноги и плечи"
                    else -> "Отдых"
                }
            }
            1 -> {
                when (currentDayOfWeek) {
                    Calendar.MONDAY -> "Новичок"
                    Calendar.TUESDAY -> "Отдых"
                    Calendar.WEDNESDAY -> "Новичок"
                    Calendar.THURSDAY -> "Отдых"
                    Calendar.FRIDAY -> "Новичок"
                    else -> "Отдых"
                }
            }
            2 -> {
                when (currentDayOfWeek) {
                    Calendar.MONDAY -> "Похудение"
                    Calendar.TUESDAY -> "Отдых"
                    Calendar.WEDNESDAY -> "Похудение"
                    Calendar.THURSDAY -> "Отдых"
                    Calendar.FRIDAY -> "Похудение"
                    else -> "Отдых"
                }
            }
            3 -> {
                when (currentDayOfWeek) {
                    Calendar.MONDAY -> "Кинезоитерапия"
                    Calendar.TUESDAY -> "Отдых"
                    Calendar.WEDNESDAY -> "Кинезоитерапия"
                    Calendar.THURSDAY -> "Отдых"
                    Calendar.FRIDAY -> "Кинезоитерапия"
                    else -> "Отдых"
                }
            }
            else -> "Отдых"
        }

        return Triple(muscleGroup, dayOfWeekString, "")
    }

    private fun startExerciseActivity() {
        val intent = Intent(this, ExerciseActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_EXERCISE)
    }

    inner class DayAdapter(private val activityType: Int) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

        private var exercisesList = mutableListOf<Exercise>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            val dayOfMonth = position + 1
            val (muscleGroup, dayOfWeek) = getCurrentMuscleGroupAndDay(dayOfMonth)
            holder.bind(dayOfMonth, muscleGroup, dayOfWeek, totalProgress)
        }

        override fun getItemCount(): Int {
            return 31 // Число дней в месяце
        }

        inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayButton: Button = itemView.findViewById(R.id.dayButton)
            private val muscleGroupTextView: TextView = itemView.findViewById(R.id.muscleGroupTextView)
            private val dayOfWeekTextView: TextView = itemView.findViewById(R.id.dayOfWeekTextView)

            init {
                dayButton.setOnClickListener {
                    val day = adapterPosition + 1
                    val (_, _, dayOfWeekString) = getCurrentMuscleGroupAndDay(day)
                    startTrainingActivity(day, dayOfWeekString, activityType)
                }
            }

            fun bind(dayOfMonth: Int, muscleGroup: String, dayOfWeek: String, totalProgress: Int) {
                dayButton.text = "День $dayOfMonth"
                muscleGroupTextView.text = "Группа мышц: $muscleGroup"
                dayOfWeekTextView.text = "День недели: $dayOfWeek"
            }
        }

        // Метод для обновления списка упражнений
        fun updateExercises(exercises: List<
                Exercise>) {
            exercisesList.clear()
            exercisesList.addAll(exercises)
            notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EXERCISE && resultCode == Activity.RESULT_OK) {
            val progress = data?.getIntExtra("progress", 0) ?: 0
            totalProgress += progress
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveSelectedDay(day: Int) {
        val sharedPreferences = getSharedPreferences("training_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("selected_day", day)
        editor.apply()
    }

    private fun startTrainingActivity(day: Int, dayOfWeekString: String, activityType: Int) {
        saveSelectedDay(day)
        val intent = Intent(this, when (activityType) {
            0 -> DenTreniActivity::class.java
            1 -> DenTreniActivity::class.java
            2 -> DenTreniActivity::class.java
            3 -> DenTreniActivity::class.java
            else -> DenTreniActivity::class.java
        })
        intent.putExtra("selected_day", day.toString())
        intent.putExtra("day_of_week_string", dayOfWeekString)
        intent.putExtra("activity_type", activityType.toString())
        startActivityForResult(intent, REQUEST_CODE_TRAINING)
    }


    companion object {
        private const val REQUEST_CODE_TRAINING = 1001
        private const val REQUEST_CODE_EXERCISE = 1002
    }
}
