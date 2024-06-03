package space.khay.trenya

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

data class SpinnerItem(val iconResId: Int, val text: String)

class SpinnerAdapter(private val context: Context, private val items: List<SpinnerItem>) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): SpinnerItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)

        val item = getItem(position)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val textView = view.findViewById<TextView>(R.id.text)

        icon.setImageResource(item.iconResId)
        textView.text = item.text

        return view
    }
}