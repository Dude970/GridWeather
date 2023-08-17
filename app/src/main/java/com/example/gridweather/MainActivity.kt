package com.example.gridweather

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.gridweather.data.WeatherData
import com.example.gridweather.databinding.ActivityMainBinding
import com.example.gridweather.model.ApiInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchWeatherData("delhi")
        searchCity()
    }

    private fun searchCity() {
        val searchview = binding.searchs
        searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean {
                if (q != null) {
                    fetchWeatherData(q)
                }
                return true

            }

            override fun onQueryTextChange(q: String?): Boolean {
                return true
            }
        })

    }

    fun fetchWeatherData(Cityname: String) {

        CoroutineScope(Dispatchers.Main).launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiInterface::class.java)
            val response = retrofit.getWeatherData(
                "delhi",
                "26f541de30f503b7cac1ae2f4d3ffb7c",
                "metric"
            )
            response.enqueue(object : Callback<WeatherData> {

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<WeatherData>,
                    response: Response<WeatherData>
                ) {

                    val responseBody = response.body()
                    if (response.isSuccessful && response.body() != null) {

                        val temperature = responseBody?.main?.temp.toString()
                        val city = responseBody?.sys?.country
                        val humidity = responseBody?.main?.humidity
                        val winSpeed = responseBody?.wind?.speed
                        val sunRise = responseBody?.sys?.sunrise?.toLong()
                        val sunSet = responseBody?.sys?.sunset?.toLong()
                        val seaLevel = responseBody?.main?.pressure
                        val condition = responseBody?.weather?.firstOrNull()?.main ?: "unknown"
                        val maxTemp = responseBody?.main?.temp_max
                        val minTemp = responseBody?.main?.temp_min

                        binding.cityname.text = city
                        binding.temp.text = "$temperature °C"
                        binding.condit.text = condition
                        binding.maxTemp.text = "Max Temp : $maxTemp °C"
                        binding.minTemp.text = "Max Temp : $minTemp °C"
                        binding.speed.text = "$winSpeed m/s"
                        binding.humidity.text = "$humidity %"
                        binding.sunrise.text = sunRise.let { it?.let { it1 -> time(it1) } }
                        binding.sunset.text = sunSet.let { it?.let { it1 -> time(it1) } }
                        binding.sea.text = "$seaLevel hpa"
                        binding.textview4.text = condition
                        binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.cityname.text = Cityname
                        changeImageAccordingToWeather(condition)
                    }

                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.e("API_CALL", "API call failed: ${t.message}")
                }
            })
        }
    }

    private fun changeImageAccordingToWeather(conditions: String) {
        when (conditions) {
            "Clearsky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottie.setAnimation(R.raw.cloud)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottie.setAnimation(R.raw.sun)
            }

            "Light Rain", "Dizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottie.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottie.setAnimation(R.raw.snow)
            }
        }
        binding.lottie.playAnimation()
    }

    @SuppressLint("WeekBasedYear")
    private fun date(): CharSequence? {
        val sdf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return sdf.format(Date())

    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))

    }

    fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))

    }
}