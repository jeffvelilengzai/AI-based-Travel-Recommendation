package my.edu.tarc.travel1.restaurant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.R

class RestaurantAdapter(
    private var restaurants: MutableList<Restaurant>,
    private val allRestaurants: MutableList<Restaurant>,
    private val onRemoveClick: (Restaurant) -> Unit,
    private val onChangeClick: (Int) -> Unit  // Pass the index for change
) : RecyclerView.Adapter<RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.textViewName.text = restaurant.name
        holder.textViewPrice.text = "Price: ${restaurant.price}"
        holder.textViewRating.text = "Rating: ${restaurant.rating}"
        holder.textViewCuisines.text = "Cuisines: ${restaurant.cuisines.joinToString(", ")}"
        holder.textViewWebUrl.text = "Website: ${restaurant.webUrl}"

        holder.buttonRemove.setOnClickListener {
            onRemoveClick(restaurant)
        }
        holder.buttonChange.setOnClickListener {
            onChangeClick(position)
        }
    }


    override fun getItemCount(): Int {
        return restaurants.size
    }
}
