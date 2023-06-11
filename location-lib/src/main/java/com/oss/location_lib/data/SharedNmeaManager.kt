package com.oss.location_lib.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.oss.location_lib.NmeaWithTime
import com.oss.location_lib.hasPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class SharedNmeaManager(context: Context, globalScope: GlobalScope) {
    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _nmeaUpdates = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val callback = OnNmeaMessageListener { message: String, timestamp: Long ->
            /*PreferenceUtils.saveInt(
                context.getString(R.string.capability_key_nmea),
                PreferenceUtils.CAPABILITY_SUPPORTED,
                prefs
            )*/
            val nmeaWithTime = NmeaWithTime(timestamp, message)
            //Log.d(TAG, "New nmea: ${nmeaWithTime}")
            // Send the new NMEA info to the Flow observers
            trySend(nmeaWithTime)
        }

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) close()

        Log.d(TAG, "Starting NMEA updates")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.addNmeaListener(ContextCompat.getMainExecutor(context), callback)
            } else {
                locationManager.addNmeaListener(callback, Handler(Looper.getMainLooper()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in location flow: $e")
            close(e) // in case of exception, close the Flow
        }

        awaitClose {
            Log.d(TAG, "Stopping NMEA updates")
            locationManager.removeNmeaListener(callback) // clean up when Flow collection ends
        }
    }.shareIn(
        globalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    @ExperimentalCoroutinesApi
    fun nmeaFlow(): Flow<NmeaWithTime> {
        return _nmeaUpdates
    }

}
