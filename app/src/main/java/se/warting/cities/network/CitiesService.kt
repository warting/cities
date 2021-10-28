package se.warting.cities.network

import androidx.annotation.Keep
import retrofit2.Response
import retrofit2.http.GET

interface CitiesService {
    @GET("cities")
    suspend fun listCities(): Response<CitiesResponse>
}

@Keep
sealed class BaseResponse() {
    abstract val status: String
    abstract val message: String?
}

@Keep
data class CitiesResponse(
    override val status: String,
    override val message: String?,
    val cities: List<City>
) : BaseResponse()

@Keep
data class City(val name: String, val lat: Double, val lon: Double, val r: Int, val points: String)