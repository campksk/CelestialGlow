package com.example.gc

import AirQualityResponse
import MoonDataResponse
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton // เปลี่ยน Button เป็น ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DisplayActivity : AppCompatActivity() {

    private lateinit var AQITextView: TextView
    private lateinit var MoonTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        val displayedImage: ImageView = findViewById(R.id.displayedImage)
        val backButton: ImageButton = findViewById(R.id.backButton) // เปลี่ยน Button เป็น ImageButton
        val latitudeTextView: TextView = findViewById(R.id.latitudeTextView)
        val longitudeTextView: TextView = findViewById(R.id.longitudeTextView)
        AQITextView = findViewById(R.id.AQITextView)
        MoonTextView = findViewById(R.id.MoonTextView)

        val imageUriString = intent.getStringExtra("imageUri")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            Glide.with(this)
                .load(imageUri)
                .into(displayedImage)
        }

        latitudeTextView.text = "Latitude: $latitude"
        longitudeTextView.text = "Longitude: $longitude"

        // เรียกฟังก์ชันดึงข้อมูล AQI
        getPolution(latitude, longitude)
        getMoonData(latitude, longitude)

        backButton.setOnClickListener {
            finish()
        }
    }

    fun getPolution(lat: Double, lon: Double) {
        val apiKey = BuildConfig.IQAIR_API_KEY

        val call = RetrofitClient.iqAirApi.getAirQualityByCity(lat, lon, apiKey)
        call.enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if (response.isSuccessful) {
                    val aqi = response.body()?.data?.current?.pollution?.aqius
                    AQITextView.text = "AQI (US): $aqi"
                } else {
                    AQITextView.text = "Error: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                AQITextView.text = "Failed to load AQI: ${t.message}"
                Log.e("API_Call", "Failed to load AQI", t)
            }
        })
    }

    fun getMoonData(lat: Double, lon: Double) {
        val call = RetrofitClient.moonApi.getMoonData(lat, lon)
        call.enqueue(object : Callback<MoonDataResponse> {
            override fun onResponse(
                call: Call<MoonDataResponse>,
                response: Response<MoonDataResponse>
            ) {
                if (response.isSuccessful) {
                    val moonData = response.body()
                    val phase = moonData?.moon?.phase
                    val phase_name = moonData?.moon?.phase_name
                    val emoji = moonData?.moon?.emoji
                    MoonTextView.text = "Moon: $phase $phase_name $emoji"
                } else {
                    MoonTextView.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<MoonDataResponse>, t: Throwable) {
                MoonTextView.text = "Failed to load Moon Data: ${t.message}"
                Log.e("API_Call", "Failed to load Moon Data", t)
            }
        })
    }
}