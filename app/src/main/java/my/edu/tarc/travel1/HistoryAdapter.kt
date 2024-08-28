package my.edu.tarc.travel1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.databinding.ItemTravelHistoryBinding

class HistoryAdapter(
    private val travelHistoryList: List<TravelHistory>,
    private val onDeleteClick: (String) -> Unit // Pass the delete callback
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemTravelHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val travelHistory = travelHistoryList[position]
        holder.bind(travelHistory)
    }

    override fun getItemCount(): Int = travelHistoryList.size

    inner class HistoryViewHolder(private val binding: ItemTravelHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(travelHistory: TravelHistory) {
            binding.textViewTravelSelection.text = "Destination: ${travelHistory.travelSelection?.destination}\n" +
                    "Start Date: ${travelHistory.travelSelection?.startDate}\n" +
                    "End Date: ${travelHistory.travelSelection?.endDate}\n" +
                    "Budget(RM): ${travelHistory.travelSelection?.budget}\n"+
                    "Selected Cuisines: ${travelHistory.travelSelection?.cuisines}\n"+
                    "Selected Subcategory: ${travelHistory.travelSelection?.subcategories}\n"

            binding.textViewHotels.text = "Hotels:\n" +
                    travelHistory.hotels.joinToString("\n") { hotel ->
                        "Hotel Name: ${hotel.name}\n" +
                                "Price Per Night(RM): ${hotel.pricePerNight}\n" +
                                "Total Price(RM): ${hotel.totalPrice}\n" +
                                "Rating: ${hotel.rating}\n" +
                                "Website: ${hotel.website}\n" +
                                "Check-in/Checkout: ${hotel.checkinCheckout}\n" +
                                "Amenities: ${hotel.amenities.joinToString(", ")}\n"
                    }

            binding.textViewRestaurants.text = "Restaurants:\n" +
                    travelHistory.restaurants.joinToString("\n") { restaurant ->
                        "Restaurant Name: ${restaurant.name}\n" +
                                "Price(RM): ${restaurant.price}\n" +
                                "Rating: ${restaurant.rating}\n" +
                                "Cuisines: ${restaurant.cuisines.joinToString(", ")}\n" +
                                "Website: ${restaurant.webUrl}\n"
                    }

            binding.textViewAttractions.text = "Attractions:\n" +
                    travelHistory.attractions.joinToString("\n") { attraction ->
                        "Attraction Name: ${attraction.name}\n" +
                                "Price(RM): ${attraction.price}\n" +
                                "Rating: ${attraction.rating}\n" +
                                "Subcategory: ${attraction.subcategory}\n" +
                                "Website: ${attraction.website}\n"
                    }

            // Set up the delete button click listener
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(travelHistory.id ?: "")
            }
        }
    }
}

