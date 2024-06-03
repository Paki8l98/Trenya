package space.khay.trenya

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NutritionDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "nutrition_db"

        // Таблица для хранения рецептов
        private const val TABLE_RECIPES = "recipes"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_INGREDIENTS = "ingredients"
        private const val KEY_CALORIES = "calories"
        private const val KEY_PROTEIN = "protein"
        private const val KEY_FATS = "fats"
        private const val KEY_CARBOHYDRATES = "carbohydrates"
        private const val KEY_INSTRUCTIONS = "instructions"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создание таблицы рецептов
        val createRecipesTable = ("CREATE TABLE $TABLE_RECIPES(" +
                "$KEY_ID INTEGER PRIMARY KEY," +
                "$KEY_NAME TEXT," +
                "$KEY_INGREDIENTS TEXT," +
                "$KEY_CALORIES INTEGER," +
                "$KEY_PROTEIN INTEGER," +
                "$KEY_FATS INTEGER," +
                "$KEY_CARBOHYDRATES INTEGER," +
                "$KEY_INSTRUCTIONS TEXT)")
        db.execSQL(createRecipesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Удаление старой таблицы при обновлении базы данных
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        // Создание новой версии базы данных
        onCreate(db)
    }

    fun addRecipe(recipe: NutritionData.Recipe) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, recipe.name)
            put(KEY_INGREDIENTS, recipe.ingredients.joinToString { "${it.first}:${it.second}" })
            put(KEY_CALORIES, recipe.calories)
            put(KEY_PROTEIN, recipe.protein)
            put(KEY_FATS, recipe.fats)
            put(KEY_CARBOHYDRATES, recipe.carbohydrates)
            put(KEY_INSTRUCTIONS, recipe.instructions)
        }
        db.insert(TABLE_RECIPES, null, values)
        db.close()
    }

    fun getBreakfastFoodsNames(): List<String> {
        return NutritionData.breakfastFoods.map { it.name }
    }

    fun getLunchFoodsNames(): List<String> {
        return NutritionData.lunchFoods.map { it.name }
    }

    fun getDinnerFoodsNames(): List<String> {
        return NutritionData.dinnerFoods.map { it.name }
    }

    fun getSecondBreakfastFoodsNames(): List<String> {
        return NutritionData.secondBreakfastFoods.map { it.name }
    }

    fun getSnackFoodsNames(): List<String> {
        return NutritionData.snackFoods.map { it.name }
    }

    fun getAllRecipes(): List<NutritionData.Recipe> {
        return NutritionData.breakfastFoods + NutritionData.lunchFoods + NutritionData.dinnerFoods +
                NutritionData.secondBreakfastFoods + NutritionData.snackFoods
    }

}



