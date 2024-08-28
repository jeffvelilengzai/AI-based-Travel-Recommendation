package my.edu.tarc.travel1.hotel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.R
import my.edu.tarc.travel1.restaurant.Restaurant

class HotelAdapter(
    private val hotels: List<Hotel>,
    private val allHotels: MutableList<Hotel>,
    private val onRemoveClick: (Hotel) -> Unit,
    private val onChangeClick: (Int) -> Unit  // Pass the index for change
) : RecyclerView.Adapter<HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotels[position]
        holder.textViewHotelName.text = hotel.name
        holder.textViewHotelPrice.text = "Price per night: ${hotel.pricePerNight}"
        holder.textViewHotelTotalPrice.text = "Total price: ${hotel.totalPrice}"
        holder.textViewHotelRating.text = "Rating: ${hotel.rating}"
        holder.textViewHotelWebsite.text = "Website: ${hotel.website}"
        holder.textViewHotelCheckinOut.text = hotel.checkinCheckout
        holder.textViewHotelAmenities.text = "Amenities: ${hotel.amenities.joinToString(", ")}"

        holder.buttonRemove.setOnClickListener {
            onRemoveClick(hotel)
        }
        holder.buttonChange.setOnClickListener {
            onChangeClick(position)
        }
    }

    override fun getItemCount(): Int {
        return hotels.size
    }
}
