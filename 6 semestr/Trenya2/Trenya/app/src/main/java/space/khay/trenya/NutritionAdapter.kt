package space.khay.trenya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NutritionAdapter(private val foods: List<NutritionData.Recipe>) :
    RecyclerView.Adapter<NutritionAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foods[position]
        holder.bind(food)
    }

    override fun getItemCount(): Int {
        return foods.size
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodTextView: TextView = itemView.findViewById(R.id.foodTextView)

        fun bind(food: NutritionData.Recipe) {
            foodTextView.text = "${adapterPosition + 1}. ${food.name}"
            foodTextView.setOnClickListener {
                showRecipeInfo(food)
            }
        }
    }

    private fun showRecipeInfo(recipe: NutritionData.Recipe) {
    }
}
