package space.khay.trenya

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class NutritionActivity : AppCompatActivity() {
    private val nutritionData = NutritionData
    private var calorieNorm: Int = 0
    private var isBreakfastSelected = false
    private var isLunchSelected = false
    private var isDinnerSelected = false
    private var issecondBreakfastSelected = false
    private var isSnackSelected = false
    //   private val selectedDishes = mutableListOf<String>()
    private val selectedBreakfastDishes = mutableListOf<String>()
    private val selectedDishes = mutableMapOf<Int, String>()
    private lateinit var nutritionDatabase: NutritionDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)

        nutritionDatabase = NutritionDatabase(this)

        savedInstanceState?.let {
            selectedDishes.putAll(it.getSerializable("selectedDishes") as Map<Int, String>)
            updateSelectedDishesTextView()
        }
        val intent = intent
        val weight = intent.getStringExtra("weight")?.toDoubleOrNull()
        val height = intent.getStringExtra("height")?.toDoubleOrNull()
        val gender = intent.getStringExtra("gender")
        val ageString = intent.getStringExtra("vozrast")
        val age = ageString?.toIntOrNull() ?: 0 // Преобразование строки в Int, в случае ошибки возвращается 0

        val exerciseSharedPreferences = getSharedPreferences("exercise_data", MODE_PRIVATE)
        val caloriesBurned = exerciseSharedPreferences.getInt("caloriesBurned", 0)

        val activityLevels = arrayOf(
            "Уровни активности",
            "Физ. нагрузка отсутствует или минимальная",
            "Умеренная активность 3 раза в неделю",
            "Тренировки средней интенсивности 5 раз в неделю",
            "Интенсивные тренировки 5 раз в неделю",
            "Каждодневные тренировки",
            "Интенсивные тренировки каждый день",
            "Ежедневная физ.нагрузка + физическая работа"
        )

        val activityLevelSpinner: Spinner = findViewById(R.id.activityLevelSpinner)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, activityLevels)

        activityLevelSpinner.adapter = adapter

        activityLevelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedActivityLevel = getActivityLevel(position)
                calorieNorm = calculateBMR(weight, height, gender, age, selectedActivityLevel)
                val calorieTextView: TextView = findViewById(R.id.calorieTextView)
                calorieTextView.text = "${calorieNorm + caloriesBurned} ккал"

                updateNutrition()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val breakfastSpinner: Spinner = findViewById(R.id.breakfastSpinner)
        val lunchSpinner: Spinner = findViewById(R.id.lunchSpinner)
        val dinnerSpinner: Spinner = findViewById(R.id.dinnerSpinner)
        val secondBreakfastSpinner: Spinner = findViewById(R.id.secondBreakfastSpinner)
        val snackSpinner: Spinner = findViewById(R.id.snackSpinner)

        val iconResId = R.drawable.ic_plus
        val iconResId1 = R.drawable.soup
        val iconResId2 = R.drawable.dinner
        val iconResId3 = R.drawable.egg
        val iconResId4 = R.drawable.skillet
        val iconResId5 = R.drawable.breakfasts

        val breakfastFoods = mutableListOf(SpinnerItem(iconResId5, "Завтрак"))
        val lunchFoods = mutableListOf(SpinnerItem(iconResId1, "Обед"))
        val dinnerFoods = mutableListOf(SpinnerItem(iconResId2, "Ужин"))
        val secondBreakfastFoods = mutableListOf(SpinnerItem(iconResId3, "Второй завтрак"))
        val snackFoods = mutableListOf(SpinnerItem(iconResId4, "Полдник"))

        breakfastFoods.addAll(nutritionDatabase.getBreakfastFoodsNames().map { SpinnerItem(iconResId, it) })
        lunchFoods.addAll(nutritionDatabase.getLunchFoodsNames().map { SpinnerItem(iconResId, it) })
        dinnerFoods.addAll(nutritionDatabase.getDinnerFoodsNames().map { SpinnerItem(iconResId, it) })
        secondBreakfastFoods.addAll(nutritionDatabase.getSecondBreakfastFoodsNames().map { SpinnerItem(iconResId, it) })
        snackFoods.addAll(nutritionDatabase.getSnackFoodsNames().map { SpinnerItem(iconResId, it) })

        val breakfastAdapter = SpinnerAdapter(this, breakfastFoods)
        val lunchAdapter = SpinnerAdapter(this, lunchFoods)
        val dinnerAdapter = SpinnerAdapter(this, dinnerFoods)
        val secondBreakfastAdapter = SpinnerAdapter(this, secondBreakfastFoods)
        val snackAdapter = SpinnerAdapter(this, snackFoods)

        breakfastSpinner.adapter = breakfastAdapter
        lunchSpinner.adapter = lunchAdapter
        dinnerSpinner.adapter = dinnerAdapter
        secondBreakfastSpinner.adapter = secondBreakfastAdapter
        snackSpinner.adapter = snackAdapter

        breakfastSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isBreakfastSelected) {
                    val selectedDish = nutritionDatabase.getBreakfastFoodsNames()[position]
                    addSelectedDish(selectedDish, R.id.breakfastSpinner)
                } else {
                    isBreakfastSelected = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        lunchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isLunchSelected) {
                    val selectedDish = nutritionDatabase.getLunchFoodsNames()[position]
                    addSelectedDish(selectedDish, R.id.lunchSpinner)
                } else {
                    isLunchSelected = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        dinnerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isDinnerSelected) {
                    val selectedDish = nutritionDatabase.getDinnerFoodsNames()[position]
                    addSelectedDish(selectedDish, R.id.dinnerSpinner)
                } else {
                    isDinnerSelected = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        secondBreakfastSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (issecondBreakfastSelected) {
                    val selectedDish = nutritionDatabase.getSnackFoodsNames()[position]
                    addSelectedDish(selectedDish, R.id.secondBreakfastSpinner)
                } else {
                    issecondBreakfastSelected = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        snackSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isSnackSelected) {
                    val selectedDish = nutritionDatabase.getSnackFoodsNames()[position]
                    addSelectedDish(selectedDish, R.id.snackSpinner)
                } else {
                    isSnackSelected = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("selectedDishes", HashMap(selectedDishes))
        super.onSaveInstanceState(outState)
    }

    private fun getActivityLevel(position: Int): Double {
        return when (position) {
            0 -> 1.0
            1 -> 1.2 // Физ. нагрузка отсутствует или минимальная
            2 -> 1.38 // Умеренная активность 3 раза в неделю
            3 -> 1.46 // Тренировки средней интенсивности 5 раз в неделю
            4 -> 1.55 // Интенсивные тренировки 5 раз в неделю
            5 -> 1.64 // Каждодневные тренировки
            6 -> 1.73 // Интенсивные тренировки каждый день
            7 -> 1.9 // Ежедневная физ.нагрузка + физическая работа
            else -> 1.0 // Если позиция не найдена, используйте значение по умолчанию
        }
    }

    private fun addSelectedDish(selectedDish: String, spinnerId: Int) {
        selectedDishes[spinnerId] = selectedDish
        //     selectedDishes[spinnerId] = selectedDish
        updateNutrition()
        updateSelectedDishesTextView()
        //      updateNutrition()
        val recipe = getRecipeByName(selectedDish)
        if (recipe != null) {
            showRecipeInfo(recipe)
        }
    }

    private fun getRecipeByName(dishName: String): NutritionData.Recipe? {
        val breakfastRecipe = nutritionData.breakfastFoods.find { it.name == dishName }
        val lunchRecipe = nutritionData.lunchFoods.find { it.name == dishName }
        val dinnerRecipe = nutritionData.dinnerFoods.find { it.name == dishName }
        val secondBreakfastRecipe = nutritionData.secondBreakfastFoods.find { it.name == dishName }
        val snackRecipe = nutritionData.snackFoods.find { it.name == dishName }

        return breakfastRecipe ?: lunchRecipe ?: dinnerRecipe ?: secondBreakfastRecipe ?: snackRecipe
    }


    private fun updateSelectedDishesTextView() {
        val selectedDishesButton: Button = findViewById(R.id.selectedDishesButton)

        selectedDishesButton.setOnClickListener {
            val selectedBreakfast = getSelectedDishesFromSpinner(R.id.breakfastSpinner)
            val selectedLunch = getSelectedDishesFromSpinner(R.id.lunchSpinner)
            val selectedDinner = getSelectedDishesFromSpinner(R.id.dinnerSpinner)
            val selectedSecondBreakfast = getSelectedDishesFromSpinner(R.id.secondBreakfastSpinner)
            val selectedSnack = getSelectedDishesFromSpinner(R.id.snackSpinner)

            val accentColor = "#FFA726"

            val selectedDishesText =
                "<b><font color='$accentColor'>Завтрак:</font></b><br>${selectedBreakfast.joinToString("<br>") { "$it (${getCaloriesForDish(it)} ккал)" }}<br><br>" +
                        "<b><font color='$accentColor'>Второй Завтрак:</font></b><br>${selectedSecondBreakfast.joinToString("<br>") { "$it (${getCaloriesForDish(it)} ккал)" }}<br><br>" +
                        "<b><font color='$accentColor'>Обед:</font></b><br>${selectedLunch.joinToString("<br>") { "$it (${getCaloriesForDish(it)} ккал)" }}<br><br>" +
                        "<b><font color='$accentColor'>Полдник:</font></b><br>${selectedSnack.joinToString("<br>") { "$it (${getCaloriesForDish(it)} ккал)" }}<br><br>" +
                        "<b><font color='$accentColor'>Ужин:</font></b><br>${selectedDinner.joinToString("<br>") { "$it (${getCaloriesForDish(it)} ккал)" }}"

            val alertDialogBuilder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_alert_dialog, null)
            val alertDialogTitle = dialogView.findViewById<TextView>(R.id.alertTitle)
            val alertDialogMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
            alertDialogTitle.text = "Выбранные блюда"
            alertDialogMessage.text = HtmlCompat.fromHtml(selectedDishesText, HtmlCompat.FROM_HTML_MODE_LEGACY)

            alertDialogBuilder.setView(dialogView)
            alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialogBuilder.setNegativeButton("Поделиться") { dialog, _ ->
                shareSelectedDishes(selectedDishesText)
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }


    private fun getCaloriesForDish(dishName: String): Int {
        val breakfastDish = nutritionData.breakfastFoods.find { it.name == dishName }
        val lunchDish = nutritionData.lunchFoods.find { it.name == dishName }
        val dinnerDish = nutritionData.dinnerFoods.find { it.name == dishName }
        val secondBreakfastDish = nutritionData.secondBreakfastFoods.find { it.name == dishName }
        val snackDish = nutritionData.snackFoods.find { it.name == dishName }

        return breakfastDish?.calories ?: lunchDish?.calories ?: dinnerDish?.calories ?: secondBreakfastDish?.calories ?: snackDish?.calories ?: 0
    }

    private fun shareSelectedDishes(selectedDishesText: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, selectedDishesText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
    }

    private fun getSelectedDishesFromSpinner(spinnerId: Int): List<String> {
        val spinner: Spinner = findViewById(spinnerId)
        val selectedDishes = mutableListOf<String>()

        val selectedPosition = spinner.selectedItemPosition

        if (selectedPosition != AdapterView.INVALID_POSITION) {
            val selectedDish = spinner.getItemAtPosition(selectedPosition) as SpinnerItem
            selectedDishes.add(selectedDish.text)
        }

        return selectedDishes
    }


    private fun calculateBMR(
        weight: Double?,
        height: Double?,
        gender: String?,
        age: Int?,
        activityLevel: Double?
    ): Int {
        if (weight == null || height == null || gender == null || age == null || activityLevel == null) {
            return 0
        }

        val bmr: Double = when (gender) {
            "Мужской" -> (66 + (13.7 * weight) + (5 * height) - (6.8 * age)) * activityLevel
            "Женский" -> (655 + (9.6 * weight) + (1.8 * height) - (4.7 * age)) * activityLevel
            else -> 0.0
        }

        return bmr.toInt()
    }

    private fun generateNutritionText(foods: List<NutritionData.Recipe>): SpannableStringBuilder {
        val clickableNutrition = SpannableStringBuilder()
        foods.forEachIndexed { index, recipe ->
            val clickableText = "${index + 1}. ${recipe.name}\n"
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    showRecipeInfo(recipe)
                }
            }

            clickableNutrition.append(clickableText)
            clickableNutrition.setSpan(
                clickableSpan,
                0,
                clickableText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return clickableNutrition
    }


    private fun showRecipeInfo(recipe: NutritionData.Recipe) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle(recipe.name)
            setMessage("Ингредиенты: ${recipe.ingredients.joinToString { "${it.first} - ${it.second} грамм" }}\n\n" +
                    "Калории: ${recipe.calories}\n" +
                    "Белки: ${recipe.protein}\n" +
                    "Жиры: ${recipe.fats}\n" +
                    "Углеводы: ${recipe.carbohydrates}\n\n" +
                    "Рецепт приготовления:\n${recipe.instructions}")
            setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun updateNutrition() {
        val breakfast = generateNutritionText(nutritionData.breakfastFoods)
        val lunch = generateNutritionText(nutritionData.lunchFoods)
        val dinner = generateNutritionText(nutritionData.dinnerFoods)
        val secondBreakfast = generateNutritionText(nutritionData.secondBreakfastFoods)
        val snack = generateNutritionText(nutritionData.snackFoods)

        val selectedBreakfast = getSelectedDishesFromSpinner(R.id.breakfastSpinner)
        val selectedLunch = getSelectedDishesFromSpinner(R.id.lunchSpinner)
        val selectedDinner = getSelectedDishesFromSpinner(R.id.dinnerSpinner)
        val selectedSecondBreakfast = getSelectedDishesFromSpinner(R.id.secondBreakfastSpinner)
        val selectedSnack = getSelectedDishesFromSpinner(R.id.snackSpinner)

        val totalCaloriesBreakfast = calculateTotalCalories(selectedBreakfast)
        val totalCaloriesLunch = calculateTotalCalories(selectedLunch)
        val totalCaloriesDinner = calculateTotalCalories(selectedDinner)
        val totalCaloriesSecondBreakfast = calculateTotalCalories(selectedSecondBreakfast)
        val totalCaloriesSnack = calculateTotalCalories(selectedSnack)



        val totalCalories = totalCaloriesBreakfast + totalCaloriesLunch + totalCaloriesDinner + totalCaloriesSecondBreakfast + totalCaloriesSnack

        val totalCaloriesTextView: TextView = findViewById(R.id.totalCaloriesTextView)
        totalCaloriesTextView.text = "$totalCalories ккал"

        val totalNutrition = StringBuilder()
        totalNutrition.append("Завтрак:\n")
        totalNutrition.append(breakfast)
        totalNutrition.append("\n\nВторой завтрак:\n")
        totalNutrition.append(secondBreakfast)
        totalNutrition.append("\n\nОбед:\n")
        totalNutrition.append(lunch)
        totalNutrition.append("\n\nПолдник:\n")
        totalNutrition.append(snack)
        totalNutrition.append("\n\nУжин:\n")
        totalNutrition.append(dinner)
        if (totalCalories > calorieNorm) {
            totalNutrition.append("\n\nОбщая калорийность превышает установленную норму.")
        }

        //  val selectedDishesTextView: TextView = findViewById(R.id.selectedDishesTextView)
        //   selectedDishesTextView.text = totalNutrition.toString()
    }

    private fun calculateTotalCalories(selectedDishes: List<String>): Int {
        var totalCalories = 0
        selectedDishes.forEach { dish ->
            val breakfastDish = nutritionData.breakfastFoods.find { it.name == dish }
            val lunchDish = nutritionData.lunchFoods.find { it.name == dish }
            val dinnerDish = nutritionData.dinnerFoods.find { it.name == dish }
            val secondBreakfastDish = nutritionData.secondBreakfastFoods.find { it.name == dish }
            val snackDish = nutritionData.snackFoods.find { it.name == dish }

            if (breakfastDish != null) {
                totalCalories += breakfastDish.calories
            }
            else if (secondBreakfastDish != null) {
                totalCalories += secondBreakfastDish.calories
            } else if (lunchDish != null) {
                totalCalories += lunchDish.calories
            } else if (snackDish != null) {
                totalCalories += snackDish.calories }
            else if (dinnerDish != null) {
                totalCalories += dinnerDish.calories
            }
        }
        return totalCalories
    }

}
