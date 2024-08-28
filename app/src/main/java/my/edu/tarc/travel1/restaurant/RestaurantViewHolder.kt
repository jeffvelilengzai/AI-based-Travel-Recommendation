package my.edu.tarc.travel1.restaurant

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.R

class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
    val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)
    val textViewCuisines: TextView = itemView.findViewById(R.id.textViewCuisines)
    val textViewWebUrl: TextView = itemView.findViewById(R.id.textViewWebUrl)

    val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)
    val buttonChange: Button = itemView.findViewById(R.id.buttonChange)
}
