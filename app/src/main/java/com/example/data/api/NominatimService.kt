package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class NominatimMicroclimateResponse(
    val place_id: Long? = null,
    val lat: String? = null,
    val lon: String? = null,
    @Json(name = "display_name") val displayName: String? = null,
    @Json(name = "class") val placeClass: String? = null,
    val type: String? = null,
    val extratags: Map<String, String>? = null,
    val address: Map<String, String>? = null
)

interface NominatimMicroclimateApi {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("extratags") extratags: Int = 1,
        @Query("addressdetails") addressdetails: Int = 1,
        @Query("accept-language") language: String = "es"
    ): NominatimMicroclimateResponse

    @GET("search")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("extratags") extratags: Int = 1,
        @Query("addressdetails") addressdetails: Int = 1,
        @Query("limit") limit: Int = 5,
        @Query("accept-language") language: String = "es"
    ): List<NominatimMicroclimateResponse>
}

object NominatimClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val USER_AGENT_HEADER = "EasyClimateApp/1.0 (contacto@easyclimate.local)"
    private const val TIMEOUT_MS = 1500L

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .writeTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .callTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT_HEADER)
                .build()
            chain.proceed(request)
        }
        .build()

    val api: NominatimMicroclimateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NominatimMicroclimateApi::class.java)
    }
}
