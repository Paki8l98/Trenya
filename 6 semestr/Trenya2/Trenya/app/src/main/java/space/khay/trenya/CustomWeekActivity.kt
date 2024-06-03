package space.khay.trenya

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class CustomWeekActivity : AppCompatActivity() {

    private lateinit var chestAdapter: ArrayAdapter<String>
    private lateinit var bicepsAdapter: ArrayAdapter<String>
    private lateinit var backAdapter: ArrayAdapter<String>
    private lateinit var tricepsAdapter: ArrayAdapter<String>
    private lateinit var legsAdapter: ArrayAdapter<String>
    private lateinit var shouldersAdapter: ArrayAdapter<String>

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_week)

        val spinnerChest: Spinner = findViewById(R.id.spinnerChest)
        val spinnerBiceps: Spinner = findViewById(R.id.spinnerBiceps)
        val spinnerBack: Spinner = findViewById(R.id.spinnerBack)
        val spinnerTriceps: Spinner = findViewById(R.id.spinnerTriceps)
        val spinnerLegs: Spinner = findViewById(R.id.spinnerLegs)
        val spinnerShoulders: Spinner = findViewById(R.id.spinnerShoulders)
        val listSelectedExercises: ListView = findViewById(R.id.listSelectedExercises)
        val buttonCreateWeek: Button = findViewById(R.id.buttonCreateWeek)

        database = FirebaseDatabase.getInstance().getReference("exercises")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chestExercises = mutableListOf<String>()
                val bicepsExercises = mutableListOf<String>()
                val backExercises = mutableListOf<String>()
                val tricepsExercises = mutableListOf<String>()
                val legsExercises = mutableListOf<String>()
                val shouldersExercises = mutableListOf<String>()

                for (snapshot in dataSnapshot.children) {
                    val exercise = snapshot.getValue(Exercise::class.java)
                    exercise?.let {
                        when (it.muscleGroup) {
                            "Грудные мышцы" -> chestExercises.add(it.name)
                            "Бицепс" -> bicepsExercises.add(it.name)
                            "Спина" -> backExercises.add(it.name)
                            "Трицепс" -> tricepsExercises.add(it.name)
                            "Ноги" -> legsExercises.add(it.name)
                            "Плечи" -> shouldersExercises.add(it.name)
                            else -> {

                            }
                        }
                    }
                }

                chestAdapter = ArrayAdapter(this@CustomWeekActivity, android.R.layout.simple_spinner_item, chestExercises)
                bicepsAdapter = ArrayAdapter(this@CustomWeekActivity, android.R.layout.simple_spinner_item, bicepsExercises)
                backAdapter = ArrayAdapter(this@CustomWeekActivity, android.R.layout.simple_spinner_item, backExercises)
                tricepsAdapter = ArrayAdapter(this@CustomWeekActivity, android.R.layout.simple_spinner_item, tricepsExercises)
                legsAdapter = ArrayAdapter(this@CustomWeekActivity, android.R.layout.simple_spinner_item, legsExercises)
                shouldersAdapter = ArrayAdapter(this@CustomWeekActivity, android.R.layout.simple_spinner_item, shouldersExercises)

                spinnerChest.adapter = chestAdapter
                spinnerBiceps.adapter = bicepsAdapter
                spinnerBack.adapter = backAdapter
                spinnerTriceps.adapter = tricepsAdapter
                spinnerLegs.adapter = legsAdapter
                spinnerShoulders.adapter = shouldersAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@CustomWeekActivity, "Ошибка загрузки данных: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

        val selectedExercisesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
        listSelectedExercises.adapter = selectedExercisesAdapter

        buttonCreateWeek.setOnClickListener {
            val selectedExercisesList = mutableListOf<String>()

            selectedExercisesList.addAll(getSelectedItems(spinnerChest))
            selectedExercisesList.addAll(getSelectedItems(spinnerBiceps))
            selectedExercisesList.addAll(getSelectedItems(spinnerBack))
            selectedExercisesList.addAll(getSelectedItems(spinnerTriceps))
            selectedExercisesList.addAll(getSelectedItems(spinnerLegs))
            selectedExercisesList.addAll(getSelectedItems(spinnerShoulders))

            selectedExercisesAdapter.clear()
            selectedExercisesAdapter.addAll(selectedExercisesList)
            selectedExercisesAdapter.notifyDataSetChanged()
        }

        val buttonExerActivity: Button = findViewById(R.id.buttonExerActivity)

        buttonExerActivity.setOnClickListener {
            val intent = Intent(this, ExerActivity::class.java)
            val selectedExercises = mutableListOf<String>()

            for (i in 0 until selectedExercisesAdapter.count) {
                val exercise = selectedExercisesAdapter.getItem(i)
                if (exercise != null) {
                    selectedExercises.add(exercise)
                }
            }

            intent.putStringArrayListExtra("selectedExercises", ArrayList(selectedExercises))
            startActivity(intent)
        }
    }

    private fun getSelectedItems(spinner: Spinner?): List<String> {
        val selectedItems = mutableListOf<String>()
        val selectedItem = spinner?.selectedItem as? String
        if (selectedItem != null && selectedItem != "Выберите упражнение") {
            selectedItems.add(selectedItem)
        }
        return selectedItems
    }
}
