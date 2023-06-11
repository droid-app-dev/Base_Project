package com.oss.location_lib.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.oss.location_lib.hasPermission
import com.oss.location_lib.minTimeMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.TimeUnit

class SharedGnssStatusManager(
    context: Context,
    globalScope: GlobalScope
) {

    // State of GnssStatus
    private val _statusState = MutableStateFlow<GnssStatusState>(GnssStatusState.Stopped)
    val statusState: StateFlow<GnssStatusState> = _statusState

    // State of ongoing GNSS fix
    private val _fixState = MutableStateFlow<FixState>(FixState.NotAcquired)
    val fixState: StateFlow<FixState> = _fixState

    // State of first GNSS fix
    private val _firstFixState = MutableStateFlow<FirstFixState>(FirstFixState.NotAcquired)
    val firstFixState: StateFlow<FirstFixState> = _firstFixState

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _gnssStatusUpdates = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val callback: GnssStatus.Callback = @RequiresApi(Build.VERSION_CODES.N)
        object : GnssStatus.Callback() {
            override fun onStarted() {
                _statusState.value = GnssStatusState.Started
            }

            override fun onStopped() {
                _statusState.value = GnssStatusState.Stopped
            }

            override fun onFirstFix(ttffMillis: Int) {
                _firstFixState.value = FirstFixState.Acquired(ttffMillis)
                _fixState.value = FixState.Acquired
            }

            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    _fixState.value = checkHaveFix(context, location)
                } else {
                    _fixState.value = FixState.NotAcquired
                }
                //Log.d(TAG, "New gnssStatus: ${status}")
                // Send the new location to the Flow observers
                trySend(status)
            }
        }

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) close()

        Log.d(TAG, "Starting GnssStatus updates")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.registerGnssStatusCallback(
                    ContextCompat.getMainExecutor(context),
                    callback
                )
            } else {
                locationManager.registerGnssStatusCallback(
                    callback,
                    Handler(Looper.getMainLooper())
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in location flow: $e")
            close(e) // in case of exception, close the Flow
        }

        awaitClose {
            Log.d(TAG, "Stopping GnssStatus updates")
            locationManager.unregisterGnssStatusCallback(callback) // clean up when Flow collection ends
            _fixState.value = FixState.NotAcquired
            _firstFixState.value = FirstFixState.NotAcquired
        }
    }.shareIn(
        globalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    /**
     * Returns a flow of GnssStatus backed by the Android system GnssStatus API.
     *
     * Note that for other flows in this class to return up-to-date data this flow must be active.
     */
    @ExperimentalCoroutinesApi
    fun statusFlow(): Flow<GnssStatus> {
        return _gnssStatusUpdates
    }
}

private fun checkHaveFix(context: Context, location: Location): FixState {
    val threshold = if (minTimeMillis(context) >= 1000L) {
        // Use two requested update intervals (it missed two updates)
        TimeUnit.MILLISECONDS.toNanos(minTimeMillis(context) * 2)
    } else {
        // Most Android devices can't refresh faster than 1Hz, so use 1.5 seconds - see #544
        TimeUnit.MILLISECONDS.toNanos(1500)
    }
    val nanosSinceFix = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
    return if (nanosSinceFix > threshold) {
        // We lost the GNSS fix
        FixState.NotAcquired
    } else {
        // We have a GNSS fix
        FixState.Acquired
    }
}

// Started/stopped states
sealed class GnssStatusState {
    object Started : GnssStatusState()
    object Stopped : GnssStatusState()
}

// GNSS ongoing fix acquired states
sealed class FixState {
    object Acquired : FixState()
    object NotAcquired : FixState()
}

// GNSS first fix state
sealed class FirstFixState {
    /**
     * [ttffMillis] the time from start of GNSS to first fix in milliseconds.
     */
    data class Acquired(val ttffMillis: Int) : FirstFixState()
    object NotAcquired : FirstFixState()
}
