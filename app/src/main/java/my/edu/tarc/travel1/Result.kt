package my.edu.tarc.travel1

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import my.edu.tarc.travel1.databinding.FragmentResultBinding
import my.edu.tarc.travel1.history.History
import my.edu.tarc.travel1.hotel.Hotel
import my.edu.tarc.travel1.hotel.HotelAdapter
import my.edu.tarc.travel1.restaurant.Restaurant
import my.edu.tarc.travel1.restaurant.RestaurantAdapter
import my.edu.tarc.travel1.hotel.Attraction
import my.edu.tarc.travel1.hotel.AttractionAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Result : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!


    // Define adapters as class-level properties
    private lateinit var hotelAdapter: HotelAdapter
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var attractionAdapter: AttractionAdapter

    // Hold the list of remaining restaurants, attractions, and hotels
    private val allRestaurants = mutableListOf<Restaurant>()
    private val allAttractions = mutableListOf<Attraction>()
    private val allHotels = mutableListOf<Hotel>()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        val recommendations = arguments?.getString("recommendations") ?: return

        try {
            val json = JSONObject(recommendations)
            val hotels = mutableListOf<Hotel>()
            val restaurants = mutableListOf<Restaurant>()
            val attractions = mutableListOf<Attraction>()

            // Initialize Adapters
            hotelAdapter = HotelAdapter(
                hotels,
                allHotels,
                onRemoveClick = { hotel ->
                    hotels.remove(hotel)
                    hotelAdapter.notifyDataSetChanged()
                },
                onChangeClick = { index ->
                    if (allHotels.isNotEmpty()) {
                        // Log the hotel being changed
                        Log.d("HotelChange", "Changing hotel at index $index with: ${allHotels[0]}")
                        hotels[index] = allHotels.removeAt(0)
                        hotelAdapter.notifyDataSetChanged()
                    }
                }
            )

            restaurantAdapter = RestaurantAdapter(
                restaurants,
                allRestaurants,
                onRemoveClick = { restaurant ->
                    restaurants.remove(restaurant)
                    restaurantAdapter.notifyDataSetChanged()
                },
                onChangeClick = { index ->
                    if (allRestaurants.isNotEmpty()) {
                        restaurants[index] = allRestaurants.removeAt(0)
                        restaurantAdapter.notifyDataSetChanged()
                    }
                }
            )

            attractionAdapter = AttractionAdapter(
                attractions,
                allAttractions,
                onRemoveClick = { attraction ->
                    attractions.remove(attraction)
                    attractionAdapter.notifyDataSetChanged()
                },
                onChangeClick = { index ->
                    if (allAttractions.isNotEmpty()) {
                        attractions[index] = allAttractions.removeAt(0)
                        attractionAdapter.notifyDataSetChanged()
                    }
                }
            )

            // Attach Adapters to RecyclerViews
            binding.recyclerViewHotels.adapter = hotelAdapter
            binding.recyclerViewRestaurants.adapter = restaurantAdapter
            binding.recyclerViewAttractions.adapter = attractionAdapter

            // Set up LayoutManagers
            binding.recyclerViewHotels.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewRestaurants.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewAttractions.layoutManager = LinearLayoutManager(context)

            // Iterate through the dates in the JSON object
            val dates = json.getJSONObject("recommendations").keys()
            while (dates.hasNext()) {
                val date = dates.next()
                val dayRecommendations = json.getJSONObject("recommendations").getJSONObject(date)

                // Parsing Hotels
                dayRecommendations.optJSONObject("Hotel")?.let {
                    val hotel = Hotel(
                        name = it.optString("name", ""),
                        pricePerNight = it.optDouble("price_per_night").toFloat(),
                        totalPrice = it.optDouble("total_price").toFloat(),
                        rating = it.optDouble("rating").toFloat(),
                        website = it.optString("website", ""),
                        checkinCheckout = it.optString("checkin_checkout", ""),
                        amenities = it.optJSONArray("amenities")?.let { amenitiesArray ->
                            List(amenitiesArray.length()) { index -> amenitiesArray.optString(index) }
                        } ?: emptyList()
                    )
                    hotels.add(hotel)
                }

                // Parsing Restaurants
                dayRecommendations.optJSONArray("Restaurants")?.let { restaurantsArray ->
                    for (i in 0 until restaurantsArray.length()) {
                        val restaurantJson = restaurantsArray.getJSONObject(i)
                        val restaurant = Restaurant(
                            name = restaurantJson.optString("name", ""),
                            price = restaurantJson.optDouble("price").toFloat(),
                            rating = restaurantJson.optDouble("rating").toFloat(),
                            cuisines = restaurantJson.optJSONArray("cuisines")?.let {
                                List(it.length()) { index -> it.optString(index) }
                            } ?: emptyList(),
                            webUrl = restaurantJson.optString("webUrl", "")
                        )
                        restaurants.add(restaurant)
                    }
                }

                // Parsing Attractions
                dayRecommendations.optJSONArray("Attractions")?.let { attractionsArray ->
                    for (i in 0 until attractionsArray.length()) {
                        val attractionJson = attractionsArray.getJSONObject(i)
                        val attraction = Attraction(
                            name = attractionJson.optString("name", ""),
                            price = attractionJson.optDouble("price").toFloat(),
                            rating = attractionJson.optDouble("rating").toFloat(),
                            subcategory = attractionJson.optString("subcategory", ""),
                            website = attractionJson.optString("website", "")
                        )
                        attractions.add(attraction)
                    }
                }
            }

            // Load all remaining restaurants for future changes
            json.optJSONArray("all_restaurants")?.let { allRestaurantsArray ->
                for (i in 0 until allRestaurantsArray.length()) {
                    val restaurantJson = allRestaurantsArray.getJSONObject(i)
                    val restaurant = Restaurant(
                        name = restaurantJson.optString("name", ""),
                        price = restaurantJson.optDouble("price").toFloat(),
                        rating = restaurantJson.optDouble("rating").toFloat(),
                        cuisines = restaurantJson.optJSONArray("cuisines")?.let {
                            List(it.length()) { index -> it.optString(index) }
                        } ?: emptyList(),
                        webUrl = restaurantJson.optString("webUrl", "")
                    )
                    allRestaurants.add(restaurant)
                }
            }

            // Load all remaining attractions for future changes
            json.optJSONArray("all_attractions")?.let { allAttractionsArray ->
                for (i in 0 until allAttractionsArray.length()) {
                    val attractionJson = allAttractionsArray.getJSONObject(i)
                    val attraction = Attraction(
                        name = attractionJson.optString("name", ""),
                        price = attractionJson.optDouble("price").toFloat(),
                        rating = attractionJson.optDouble("rating").toFloat(),
                        subcategory = attractionJson.optString("subcategory", ""),
                        website = attractionJson.optString("website", "")
                    )
                    allAttractions.add(attraction)
                }
            }

            // Load all remaining hotels for future changes
            json.optJSONArray("all_hotels")?.let { allHotelsArray ->
                for (i in 0 until allHotelsArray.length()) {
                    val hotelJson = allHotelsArray.getJSONObject(i)
                    val hotel = Hotel(
                        name = hotelJson.optString("name", ""),
                        pricePerNight = hotelJson.optDouble("price_per_night").toFloat(),
                        totalPrice = hotelJson.optDouble("total_price").toFloat(),
                        rating = hotelJson.optDouble("rating").toFloat(),
                        website = hotelJson.optString("website", ""),
                        checkinCheckout = hotelJson.optString("checkin_checkout", ""),
                        amenities = hotelJson.optJSONArray("amenities")?.let { amenitiesArray ->
                            List(amenitiesArray.length()) { index -> amenitiesArray.optString(index) }
                        } ?: emptyList()
                    )
                    Log.d("HotelParsing", "Parsed hotel: $hotelJson")
                    allHotels.add(hotel)
                }
            }

            // Set up "Add" button for restaurants
            binding.buttonAddRestaurant.setOnClickListener {
                if (allRestaurants.isNotEmpty()) {
                    val newRestaurant = allRestaurants.removeAt(0)
                    restaurants.add(newRestaurant)
                    restaurantAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "No more restaurants to add", Toast.LENGTH_SHORT).show()
                }
            }

            // Set up "Add" button for attractions
            binding.buttonAddAttraction.setOnClickListener {
                if (allAttractions.isNotEmpty()) {
                    val newAttraction = allAttractions.removeAt(0)
                    attractions.add(newAttraction)
                    attractionAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "No more attractions to add", Toast.LENGTH_SHORT).show()
                }
            }

            // Set up "Add" button for hotels
            binding.buttonAddHotel.setOnClickListener {
                if (allHotels.isNotEmpty()) {
                    val newHotel = allHotels.removeAt(0)
                    // Log the hotel being added
                    Log.d("HotelAddition", "Adding hotel: $newHotel")
                    hotels.add(newHotel)
                    hotelAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "No more hotels to add", Toast.LENGTH_SHORT).show()
                }
            }

            // Add a button or automatically save the data
            binding.buttonSave.setOnClickListener {
                saveRecommendationsToFirebase(hotels, restaurants, attractions)
            }



            // Notify adapters of data changes
            hotelAdapter.notifyDataSetChanged()
            restaurantAdapter.notifyDataSetChanged()
            attractionAdapter.notifyDataSetChanged()

        } catch (e: JSONException) {
            Log.e("ResultFragment", "Error parsing JSON", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Call a function to set up the menu based on user role asynchronously
        setupMenuBasedOnRole(menu, inflater)
    }

    private fun setupMenuBasedOnRole(menu: Menu, inflater: MenuInflater) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").getValue(String::class.java) ?: "user"
            when (role) {
                "admin" -> inflater.inflate(R.menu.drawer_menu, menu)
                "user" -> inflater.inflate(R.menu.menu_user, menu)
                else -> super.onCreateOptionsMenu(menu, inflater)
            }
        }.addOnFailureListener {
            inflater.inflate(R.menu.menu_user, menu)  // Default to user menu on failure
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_profile -> navigateToFragment(UserProfile())
            R.id.nav_logout -> {
                auth.signOut()
                Toast.makeText(requireActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show() // Toast on logout
                navigateToFragment(activity_login())
                true
            }
            R.id.nav_travel -> navigateToFragment(TravelRec())
            R.id.nav_travel_history -> navigateToFragment(History())
            R.id.nav_report -> navigateToFragment(Report())
            R.id.nav_manage_user -> navigateToFragment(AdminProfile())
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToFragment(fragment: Fragment): Boolean {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
        return true
    }

    private fun saveRecommendationsToFirebase(
        hotels: List<Hotel>,
        restaurants: List<Restaurant>,
        attractions: List<Attraction>
    ) {
        val userId = auth.currentUser?.uid ?: return
        val travelRef = database.child("users").child(userId).child("travels")

        // Generate a new travel ID starting from 1 and incrementing
        travelRef.get().addOnSuccessListener { dataSnapshot ->
            val travelId = (dataSnapshot.childrenCount + 1).toString()

            // Retrieve travel selection from arguments
            val travelSelection = arguments?.getParcelable<TravelRec.TravelSelection>("travel_selection") ?: return@addOnSuccessListener

            val travelData = mapOf(
                "travel_selection" to mapOf(
                    "startDate" to travelSelection.startDate,
                    "endDate" to travelSelection.endDate,
                    "destination" to travelSelection.destination,
                    "budget" to travelSelection.budget,
                    "cuisines" to travelSelection.cuisines,
                    "subcategories" to travelSelection.subcategories
                ),
                "recommendations" to mapOf(
                    "hotels" to hotels.map { hotel ->
                        mapOf(
                            "name" to hotel.name,
                            "pricePerNight" to hotel.pricePerNight,
                            "totalPrice" to hotel.totalPrice,
                            "rating" to hotel.rating,
                            "website" to hotel.website,
                            "checkinCheckout" to hotel.checkinCheckout,
                            "amenities" to hotel.amenities
                        )
                    },
                    "restaurants" to restaurants.map { restaurant ->
                        mapOf(
                            "name" to restaurant.name,
                            "price" to restaurant.price,
                            "rating" to restaurant.rating,
                            "cuisines" to restaurant.cuisines,
                            "webUrl" to restaurant.webUrl
                        )
                    },
                    "attractions" to attractions.map { attraction ->
                        mapOf(
                            "name" to attraction.name,
                            "price" to attraction.price,
                            "rating" to attraction.rating,
                            "subcategory" to attraction.subcategory,
                            "website" to attraction.website
                        )
                    }
                )
            )

            travelRef.child(travelId).setValue(travelData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Recommendations saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ResultFragment", "Error saving recommendations", e)
                    Toast.makeText(context, "Failed to save recommendations", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
