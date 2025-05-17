import com.example.gc.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface MoonApiService {
    @GET("advanced")
    @Headers("X-RapidAPI-Key: ${BuildConfig.MOON_API_KEY}", "X-RapidAPI-Host: moon-phase.p.rapidapi.com")
    fun getMoonData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<MoonDataResponse>
}
