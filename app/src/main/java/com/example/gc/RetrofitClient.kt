import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val AQI_BASE_URL = "https://api.airvisual.com/v2/"
    private const val MOON_BASE_URL = "https://moon-phase.p.rapidapi.com/"

    val iqAirApi: IQAirApiService by lazy { // เปลี่ยนชื่อเป็น iqAirApi
        Retrofit.Builder()
            .baseUrl(AQI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IQAirApiService::class.java)
    }

    val moonApi: MoonApiService by lazy { // เปลี่ยนชื่อเป็น moonApi
        Retrofit.Builder()
            .baseUrl(MOON_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoonApiService::class.java)
    }
}