package my.edu.tarc.travel1

data class TravelRecord(
    val id: String = "",
    val travelSelection: TravelSelection = TravelSelection(),
    val recommendations: Recommendations = Recommendations()
) {
    data class TravelSelection(
        val startDate: String = "",
        val endDate: String = "",
        val budget: String = "",
        val destination: String = "",
        val cuisines: List<String> = emptyList(),
        val subcategories: List<String> = emptyList()
    ) {
        val formattedCuisines: String
            get() = cuisines.joinToString(separator = ", ")

        val formattedSubcategories: String
            get() = subcategories.joinToString(separator = ", ")
    }

    data class Recommendations(
        val hotels: List<Hotel> = emptyList(),
        val restaurants: List<Restaurant> = emptyList(),
        val attractions: List<Attraction> = emptyList()
    )

    data class Hotel(val name: String = "", val details: String = "")
    data class Restaurant(val name: String = "", val details: String = "")
    data class Attraction(val name: String = "", val details: String = "")
}
