package my.edu.tarc.travel1

import my.edu.tarc.travel1.hotel.Attraction
import my.edu.tarc.travel1.hotel.Hotel
import my.edu.tarc.travel1.restaurant.Restaurant

data class Recommendation(
    val date: String,
    val hotels: List<Hotel>,
    val restaurants: List<Restaurant>,
    val attractions: List<Attraction>
)

