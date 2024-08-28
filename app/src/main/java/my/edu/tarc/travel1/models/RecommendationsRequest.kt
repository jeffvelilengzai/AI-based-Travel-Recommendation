package my.edu.tarc.travel1.models

data class RecommendationsRequest(
    val start_date: String,
    val end_date: String,
    val destination: String,
    val budget: Double
)
