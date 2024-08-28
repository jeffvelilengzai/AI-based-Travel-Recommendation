package my.edu.tarc.travel1.api

import my.edu.tarc.travel1.models.RecommendationsRequest
import my.edu.tarc.travel1.models.RecommendationsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/recommendations")
    fun getRecommendations(@Body request: RecommendationsRequest): Call<RecommendationsResponse>
}
