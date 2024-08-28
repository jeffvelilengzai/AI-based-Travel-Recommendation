package my.edu.tarc.travel1.hotel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.travel1.R
import my.edu.tarc.travel1.restaurant.Restaurant

class AttractionAdapter(
    private val attractions: List<Attraction>,
    private val allAttractions: MutableList<Attraction>,
    private val onRemoveClick: (Attraction) -> Unit,
    private val onChangeClick: (Int) -> Unit  // Pass the index for change
) : RecyclerView.Adapter<AttractionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_attraction, parent, false)
        return AttractionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        val attraction = attractions[position]
        holder.textViewAttractionName.text = attraction.name
        holder.textViewAttractionPrice.text = "Price: ${attraction.price}"
        holder.textViewAttractionRating.text = "Rating: ${attraction.rating}"
        holder.textViewAttractionCategory.text = "Subcategory: ${attraction.subcategory}"
        holder.textViewAttractionWebsite.text = "Website: ${attraction.website}"

        holder.buttonRemove.setOnClickListener {
            onRemoveClick(attraction)
        }
        holder.buttonChange.setOnClickListener {
            onChangeClick(position)
        }
    }

    override fun getItemCount(): Int {
        return attractions.size
    }
}
