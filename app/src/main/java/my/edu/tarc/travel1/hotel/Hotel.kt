package my.edu.tarc.travel1.hotel

data class Hotel(
    val name: String = "",
    val pricePerNight: Float = 0.0f,
    val totalPrice: Float = 0.0f,
    val rating: Float = 0.0f,
    val website: String = "",
    val checkinCheckout: String = "",
    val amenities: List<String> = emptyList(),
    val city: String = ""
)
