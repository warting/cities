package se.warting.cities

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionRational
import dev.marcelpinto.permissionktx.PermissionStatus
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import se.warting.cities.initializers.ServiceInitializer
import se.warting.cities.network.City


data class MainState(
    val cities: CitiesState = CitiesState.Loading,
    val gps: GpsState = GpsState.Loading,
)

sealed class CitiesState {
    object Loading : CitiesState()
    data class Failed(val message: String) : CitiesState()
    data class Success(val cities: List<City>) : CitiesState()
}

sealed class GpsState {
    object Loading : GpsState()
    data class Revoked(val showRational: Boolean) : GpsState()
    object Granted : GpsState()
}

class MainViewModel : ViewModel() {

    private val _sessionState: MutableStateFlow<MainState> =
        MutableStateFlow(MainState())

    val uiState: StateFlow<MainState>
        get() = _sessionState

    private val finePermission = Permission(Manifest.permission.ACCESS_FINE_LOCATION).statusFlow

    init {

        viewModelScope.launch {
            finePermission.collect { status ->
                when (status) {
                    is PermissionStatus.Granted -> {
                        _sessionState.value = _sessionState.value.copy(
                            gps = GpsState.Granted
                        )
                    }
                    is PermissionStatus.Revoked -> {
                        _sessionState.value = _sessionState.value.copy(
                            gps = GpsState.Revoked((status.rationale == PermissionRational.REQUIRED))
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            try {
                val response = ServiceInitializer.getInstance().listCities()
                if (response.isSuccessful) {
                    val body = response.body()!! // Assume having a body if successful
                    if (body.status == "success") {
                        _sessionState.value = _sessionState.value.copy(
                            cities = CitiesState.Success(body.cities)
                        )
                    } else {
                        _sessionState.value = _sessionState.value.copy(
                            cities = CitiesState.Failed(body.message ?: "Unknown Error")
                        )
                    }
                } else {
                    _sessionState.value = _sessionState.value.copy(
                        cities = CitiesState.Failed("Unknown Error")
                    )
                }
            } catch (e: IOException) {
                _sessionState.value = _sessionState.value.copy(
                    cities = CitiesState.Failed("Unknown Error")
                )
            }

        }


    }
}
