package space.khay.trenya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExercisesAdapter(private val exercises: List<Exercise>) : RecyclerView.Adapter<ExercisesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNameText: TextView = itemView.findViewById(R.id.exerciseNameText)
        val exerciseGifImage: ImageView = itemView.findViewById(R.id.exerciseGifImage)
        val exerciseDurationText: TextView = itemView.findViewById(R.id.exerciseDurationText)
        val exerciseSetsText: TextView = itemView.findViewById(R.id.exerciseSetsText)
        val exerciseRecommendationsText: TextView = itemView.findViewById(R.id.exerciseRecommendationsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.exerciseNameText.text = exercise.name
        holder.exerciseDurationText.text = "Длительность: ${exercise.duration} секунд"
        holder.exerciseSetsText.text = "Подходы: ${exercise.sets}"
        holder.exerciseRecommendationsText.text = "Рекомендации: ${exercise.recommendations}"

        Glide.with(holder.exerciseGifImage.context)
            .asGif()
            .load(exercise.gifPath)
            .into(holder.exerciseGifImage)
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}
