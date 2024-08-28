package my.edu.tarc.travel1

data class TravelSelection(
    val startDate: String = "",
    val endDate: String = "",
    val destination: String = "",
    val budget: Float = 0f,
    val cuisines: List<String> = emptyList(),
    val subcategories: List<String> = emptyList()
)