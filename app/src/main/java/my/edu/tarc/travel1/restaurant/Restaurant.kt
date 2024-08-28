package my.edu.tarc.travel1.restaurant

data class Restaurant(
    val name: String = "",
    val price: Float = 0.0f,
    val rating: Float = 0.0f,
    val cuisines: List<String> = emptyList(),
    val webUrl: String = "",
    val city: String = ""
)
