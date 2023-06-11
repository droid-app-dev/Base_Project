package com.oss.location_lib.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.oss.location_lib.MIN_DISTANCE
import com.oss.location_lib.hasPermission
import com.oss.location_lib.minTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class SharedLocationManager constructor(
    private val context: Context,
    externalScope: CoroutineScope,
) {


    private val _receivingLocationUpdates: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val receivingLocationUpdates: StateFlow<Boolean>
    get()= _receivingLocationUpdates

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _locationUpdates = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val callback = LocationListener { location ->
            //Log.d(TAG, "New location: ${location.toNotificationTitle()}")
            // Send the new location to the Flow observers
            trySend(location)
        }

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) close()

        //Log.d(TAG, "Starting location updates with minTime=${minTimeMillis(context, prefs)}ms and minDistance=${minDistance(context, prefs)}m")
        _receivingLocationUpdates.value = true

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTimeMillis(context),
                MIN_DISTANCE,
                callback,
                context.mainLooper
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in location flow: $e")
            close(e) // in case of exception, close the Flow
        }

        awaitClose {
            Log.d(TAG, "Stopping location updates")
            _receivingLocationUpdates.value = false
            locationManager.removeUpdates(callback) // clean up when Flow collection ends
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )


    @ExperimentalCoroutinesApi
    fun locationFlow(): Flow<Location> {
        return _locationUpdates
    }


}