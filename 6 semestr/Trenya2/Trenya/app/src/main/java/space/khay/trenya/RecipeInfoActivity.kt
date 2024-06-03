package space.khay.trenya

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_info)

        val intent = intent
        val recipeName = intent.getStringExtra("recipe_name")
        val recipeCalories = intent.getIntExtra("recipe_calories", 0)
        val recipeProtein = intent.getIntExtra("recipe_protein", 0)
        val recipeFats = intent.getIntExtra("recipe_fats", 0)
        val recipeCarbohydrates = intent.getIntExtra("recipe_carbohydrates", 0)
        val recipeInstructions = intent.getStringExtra("recipe_instructions")

        val nameTextView: TextView = findViewById(R.id.recipeNameTextView)
        val caloriesTextView: TextView = findViewById(R.id.recipeCaloriesTextView)
        val proteinTextView: TextView = findViewById(R.id.recipeProteinTextView)
        val fatsTextView: TextView = findViewById(R.id.recipeFatsTextView)
        val carbohydratesTextView: TextView = findViewById(R.id.recipeCarbohydratesTextView)
        val instructionsTextView: TextView = findViewById(R.id.recipeInstructionsTextView)

        nameTextView.text = recipeName
        caloriesTextView.text = "Калории: $recipeCalories ккал"
        proteinTextView.text = "Белки: $recipeProtein г"
        fatsTextView.text = "Жиры: $recipeFats г"
        carbohydratesTextView.text = "Углеводы: $recipeCarbohydrates г"
        instructionsTextView.text = recipeInstructions
    }
}

