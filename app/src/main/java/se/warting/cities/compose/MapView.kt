package se.warting.cities.compose

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.Dash
import com.google.android.libraries.maps.model.Gap
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.PatternItem
import com.google.android.libraries.maps.model.Polygon
import com.google.android.libraries.maps.model.PolygonOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.warting.cities.R
import se.warting.cities.network.City


@Composable
fun MapView(city: City, close: () -> Unit) {
    val mapView = rememberMapViewWithLifecycle()
    val cords = city.points.split(",").map {
        val pairOfCords = it.split(" ")
        LatLng(pairOfCords[1].trim().toDouble(), pairOfCords[0].trim().toDouble())
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {


            AndroidView({ mapView }) { mapView ->
                CoroutineScope(Dispatchers.Main).launch {
                    val map = mapView.awaitMap()
                    map.uiSettings.isZoomControlsEnabled = true


                    val polygons: Polygon = map.addPolygon(
                        PolygonOptions()
                            .addAll(cords)
                    )

                    stylePolygon(polygons)

                    val bounds = getPolygonLatLngBounds(cords)

                    map.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            200
                        )
                    )
                }

            }
            Button(modifier = Modifier.padding(16.dp), onClick = close) {
                Text(text = "Close")
            }
        }
    }
}

private fun getPolygonLatLngBounds(polygon: List<LatLng>): LatLngBounds {
    val centerBuilder = LatLngBounds.builder()
    for (point in polygon) {
        centerBuilder.include(point)
    }
    return centerBuilder.build()
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }
    }

/**
 * Styles the polygon, based on type.
 * @param polygon The polygon object that needs styling.
 */
fun stylePolygon(polygon: Polygon) {

    val gap: PatternItem = Gap(20.toFloat())
    val dash: PatternItem = Dash(20.toFloat())

    polygon.strokePattern = listOf(gap, dash)
    polygon.strokeWidth = 8f
    polygon.strokeColor = -0xc771c4
    polygon.fillColor = -0x7e387c
}
