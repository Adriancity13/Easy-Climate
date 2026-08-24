package com.example.data.api

import com.example.data.models.GeocodingSearchResponse
import com.example.data.models.OpenMeteoWeatherResponse
import com.example.data.models.ReverseGeocodeResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,wind_speed_10m,is_day",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,is_day,precipitation_probability,wind_speed_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,apparent_temperature_max,sunrise,sunset",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoWeatherResponse
}

interface GeocodingApi {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 6,
        @Query("language") language: String = "es",
        @Query("format") format: String = "json"
    ): GeocodingSearchResponse
}

interface ReverseGeocodeApi {
    @GET("data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") localityLanguage: String = "es"
    ): ReverseGeocodeResponse
}

object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val openMeteoApi: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApi::class.java)
    }

    val reverseGeocodeApi: ReverseGeocodeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.bigdatacloud.net/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ReverseGeocodeApi::class.java)
    }
}
