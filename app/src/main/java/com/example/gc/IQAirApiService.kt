import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IQAirApiService {
    @GET("nearest_city")
    fun getAirQualityByCity(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("key") apiKey: String
    ): Call<AirQualityResponse>
}
