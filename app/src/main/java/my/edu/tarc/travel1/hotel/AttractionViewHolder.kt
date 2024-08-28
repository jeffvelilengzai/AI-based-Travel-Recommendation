package my.edu.tarc.travel1.hotel

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.R

class AttractionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewAttractionName: TextView = itemView.findViewById(R.id.textViewAttractionName)
    val textViewAttractionPrice: TextView = itemView.findViewById(R.id.textViewAttractionPrice)
    val textViewAttractionRating: TextView = itemView.findViewById(R.id.textViewAttractionRating)
    val textViewAttractionCategory: TextView = itemView.findViewById(R.id.textViewAttractionCategory)
    val textViewAttractionWebsite: TextView = itemView.findViewById(R.id.textViewAttractionWebsite)

    val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)
    val buttonChange: Button = itemView.findViewById(R.id.buttonChange)
}
