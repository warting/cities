package se.warting.cities

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.flowOf
import se.warting.cities.borrowed.locationFlow
import se.warting.cities.compose.ErrorView
import se.warting.cities.compose.LoadingView
import se.warting.cities.compose.MapView
import se.warting.cities.compose.RequestPermissionsButton
import se.warting.cities.network.City
import se.warting.cities.ui.theme.CitiesTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CitiesTheme {
                Main()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {

    val scaffoldState = rememberScaffoldState()

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val selectedMap = rememberSaveable {
        mutableStateOf<City?>(null)
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .navigationBarsWithImePadding(),
        scaffoldState = scaffoldState,
        topBar = {
            SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        Box(
            Modifier
                .padding(contentPadding)
        ) {
            MainContent(openMap = {
                selectedMap.value = it
            })
        }
    }

    // TODO: use https://developer.android.com/jetpack/compose/navigation to navigate around instead of using this map state
    // Show map on top of scaffold if selected
    selectedMap.value?.let {
        BackHandler(onBack = {
            selectedMap.value = null
        })
        MapView(it) {
            selectedMap.value = null
        }
    }

}


@Composable
fun MainContent(openMap: (City) -> Unit) {
    val mainViewModel: MainViewModel = viewModel()

    val context = LocalContext.current


    val uiState = mainViewModel.uiState.collectAsState()


    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)

    }

    val gpsState: State<Location?> = when (uiState.value.gps) {
        is GpsState.Granted -> remember {
            fusedLocationClient.locationFlow()
        }.collectAsState(
            initial = null
        )
        GpsState.Loading -> flowOf(null).collectAsState(initial = null)
        is GpsState.Revoked -> flowOf(null).collectAsState(initial = null)
    }

    Column {
        when (uiState.value.gps) {
            GpsState.Granted -> {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "My location - lat:" + gpsState.value?.latitude.toString() + " lon: " + gpsState.value?.longitude.toString()
                )
            }
            GpsState.Loading -> {
                // TODO: Some sort of loader
            }
            is GpsState.Revoked -> {
                RequestPermissionsButton()
            }
        }

        when (val citiesState = uiState.value.cities) {
            is CitiesState.Failed -> ErrorView(message = citiesState.message)
            CitiesState.Loading -> LoadingView()
            is CitiesState.Success -> CitiesList(citiesState.cities, gpsState.value, openMap)
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CitiesList(cities: List<City>, userLocation: Location?, openMap: (City) -> Unit) {
    LazyColumn {

        // TODO: make a UI object of List<City> and don't use BE entities directly
        val sortedCities = if (userLocation != null) {
            cities.sortedBy {
                // TODO: map City to something else in viewmodel that already have Location
                val cityLocation = Location("City")
                cityLocation.latitude = it.lat
                cityLocation.longitude = it.lon
                userLocation.distanceTo(cityLocation)
            }
        } else {
            cities
        }

        items(items = sortedCities) { city ->
            val cityLocation = Location("City")
            cityLocation.latitude = city.lat
            cityLocation.longitude = city.lon

            val distanceOrCords: String = if (userLocation != null) {
                userLocation.distanceTo(cityLocation).roundToInt().toString() + "m"
            } else {
                "Lat: " + city.lat + " Lon: " + city.lon
            }
            ListItem(
                Modifier.clickable {
                    openMap(city)
                },
                text = { Text(text = city.name) },
                secondaryText = { Text(text = distanceOrCords) },
            )
        }
    }
}
