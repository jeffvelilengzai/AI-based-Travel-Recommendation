package my.edu.tarc.travel1.hotel

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.R

class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewHotelName: TextView = itemView.findViewById(R.id.textViewHotelName)
    val textViewHotelPrice: TextView = itemView.findViewById(R.id.textViewHotelPricePerNight)
    val textViewHotelTotalPrice: TextView = itemView.findViewById(R.id.textViewHotelTotalPrice)
    val textViewHotelRating: TextView = itemView.findViewById(R.id.textViewHotelRating)
    val textViewHotelWebsite: TextView = itemView.findViewById(R.id.textViewHotelWebsite)
    val textViewHotelCheckinOut: TextView = itemView.findViewById(R.id.textViewHotelCheckinCheckout)
    val textViewHotelAmenities: TextView = itemView.findViewById(R.id.textViewHotelAmenities)

    val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)
    val buttonChange: Button = itemView.findViewById(R.id.buttonChange)
}
