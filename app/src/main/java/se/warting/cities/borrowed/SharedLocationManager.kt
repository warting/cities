package se.warting.cities.borrowed;

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


// Borrowed with small modifications from https://medium.com/androiddevelopers/simplifying-apis-with-coroutines-and-flow-a6fb65338765
@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
    // 1. Create callback and add elements into the flow
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result ?: return // Ignore null responses
            for (location in result.locations) {
                try {
                    trySend(location) // Send location to the flow
                } catch (t: Throwable) {
                    // Location couldn't be sent to the flow
                }
            }
        }
    }

    // 2. Register the callback to get location updates by calling requestLocationUpdates
    requestLocationUpdates(
        createLocationRequest(),
        callback,
        Looper.getMainLooper()
    ).addOnFailureListener { e ->
        close(e) // in case of error, close the Flow
    }

    // 3. Wait for the consumer to cancel the coroutine and unregister
    // the callback. This suspends the coroutine until the Flow is closed.
    awaitClose {
        // Clean up code goes here
        removeLocationUpdates(callback)
    }
}

fun createLocationRequest(): LocationRequest {
    val locationRequest = LocationRequest.create()
    locationRequest.interval = 10_000
    locationRequest.fastestInterval = 5_000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    return locationRequest
}