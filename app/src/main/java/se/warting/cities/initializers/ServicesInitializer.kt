package se.warting.cities.initializers

import android.content.Context
import androidx.startup.Initializer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.warting.cities.network.CitiesService


@Suppress("unused")
class ServiceInitializer : Initializer<CitiesService> {

    // TODO: tried the Initializer just for fun, not sure if I would do this or something else for retrofit client in real life :-)

    companion object {
        private const val BACKEND = "https://pgroute-staging.easyparksystem.net/"
        private lateinit var instance: CitiesService
        fun getInstance(): CitiesService {
            return instance
        }
    }

    override fun create(context: Context): CitiesService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        instance = retrofit.create(CitiesService::class.java)
        return instance
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> =
        mutableListOf()
}
