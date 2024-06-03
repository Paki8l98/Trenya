package space.khay.trenya

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ExerActivity : AppCompatActivity() {

    private lateinit var exerciseNameTextView: TextView
    private lateinit var exerciseImage: ImageView
    private lateinit var exerciseDurationTextView: TextView
    private lateinit var exerciseRecommendationTextView: TextView
    private lateinit var startTimerButton: Button
    private lateinit var nextExerciseButton: Button
    private var selectedExercises: ArrayList<String>? = null
    private var currentExerciseIndex: Int = 0
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exer)

        exerciseNameTextView = findViewById(R.id.exerciseNameTextView)
        exerciseImage = findViewById(R.id.exerciseImage)
        exerciseDurationTextView = findViewById(R.id.exerciseDurationTextView)
        exerciseRecommendationTextView = findViewById(R.id.exerciseRecommendationTextView)
        startTimerButton = findViewById(R.id.startTimerButton)
        nextExerciseButton = findViewById(R.id.nextExerciseButton)

        selectedExercises = intent.getStringArrayListExtra("selectedExercises")

        nextExerciseButton.setOnClickListener {
            if (selectedExercises != null) {
                showNextExercise()
            }
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("exercises")

        currentExerciseIndex = savedInstanceState?.getInt("currentExerciseIndex") ?: 0

        selectedExercises?.getOrNull(currentExerciseIndex)?.let { loadExerciseDetails(it) }

        startTimerButton.setOnClickListener {
            startTimerOnClick(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("selectedExercises", selectedExercises)
        outState.putInt("currentExerciseIndex", currentExerciseIndex)
    }

    private fun showNextExercise() {
        if (currentExerciseIndex < selectedExercises!!.size - 1) {
            currentExerciseIndex++
            loadExerciseDetails(selectedExercises!![currentExerciseIndex])
        } else {
            Toast.makeText(this, "No more exercises", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadExerciseDetails(exerciseName: String) {

        databaseReference.orderByChild("name").equalTo(exerciseName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val exercise = snapshot.getValue(Exercise::class.java)
                            if (exercise != null) {
                                exerciseNameTextView.text = exercise.name
                                exerciseDurationTextView.text = "Duration: ${exercise.duration} seconds"
                                exerciseRecommendationTextView.text = "Recommendations: ${exercise.recommendations}"
                                Glide.with(this@ExerActivity)
                                    .asGif()
                                    .load(exercise.gifPath)
                                    .into(exerciseImage)
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@ExerActivity, "Failed to load exercise: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun startTimerOnClick(view: View) {
        val exerciseDurationStr = exerciseDurationTextView.text.toString()
        val exerciseDuration = exerciseDurationStr.replace("[^\\d.]".toRegex(), "").toLong()

        val timer = object : CountDownTimer(exerciseDuration * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseDurationTextView.text = "Duration: ${(millisUntilFinished / 1000)} seconds"
            }

            override fun onFinish() {
                Toast.makeText(this@ExerActivity, "Exercise completed!", Toast.LENGTH_SHORT).show()
            }
        }

        timer.start()
    }
}
