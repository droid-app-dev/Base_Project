package com.oss.location_lib.data

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.oss.location_lib.NmeaWithTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject


class FusedLocation( context: Context, globalScope: GlobalScope
) {
    private val fusedLocationUpdate = callbackFlow {

        val   fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
         lateinit var locationCallback: LocationCallback

        val locationRequest= LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMinUpdateIntervalMillis(2000)
            .build()


        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return@callbackFlow
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {

                    trySend(location)

                }
            }
            // Few more things we can do here:
            // For example: Update the location of user on server
        }


        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            context.mainLooper
        )

        awaitClose {
            Log.d(ContentValues.TAG, "Stopping NMEA updates")
        }

        }.shareIn(
        globalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    @ExperimentalCoroutinesApi
    fun getfusedlocation(): Flow<Location> {
        return fusedLocationUpdate
    }

}