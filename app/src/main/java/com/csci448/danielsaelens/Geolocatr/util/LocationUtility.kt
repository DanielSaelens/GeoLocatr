package com.csci448.danielsaelens.Geolocatr.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.security.Permission


data class LocationState(
    val location: Location? = null,
    val address: String = "",
    val isLocationAvailable: Boolean = false
)

class LocationUtility (context: Context) {

    private val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 0)
        .setMaxUpdates(1)
        .build()
     private val locationCallback = object : LocationCallback(){
         override fun onLocationResult(locationResult: LocationResult){
             _locationStateFlow.update {
                 it.copy(location = locationResult.lastLocation)
             }

         }
     }
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)







    companion object {
        private const val LOG_TAG = "448.LocationUtility"
    }


    private val _locationStateFlow = MutableStateFlow(LocationState())

    @Composable
    fun use(lifecycleOwner: LifecycleOwner) = _locationStateFlow
        .collectAsStateWithLifecycle(lifecycleOwner)
        .value

    fun checkPermissionAndGetLocation(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        if (activity.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
            activity.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
        ) {
            Log.d(LOG_TAG, "We have permission")
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    ACCESS_COARSE_LOCATION
                )
            ) {

                Log.d(LOG_TAG, "Permission was Denied")
                Toast.makeText(
                    activity,
                    "We must access your location to plot where you are",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Log.d(LOG_TAG, "Asking for permissions")
                permissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
            }


        }
    }
    fun removeLocationRequest(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    fun getAddress(location: Location?) {
        if (location != null) {
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            ) { addresses ->

                val addressTextBuilder = StringBuilder()

                if (addresses.isNotEmpty()) {
                    val address = addresses[0]

                    for (i in 0..address.maxAddressLineIndex) {
                        if (i > 0) {
                            addressTextBuilder.append("\n")
                        }

                        addressTextBuilder.append(
                            address.getAddressLine(i)
                        )
                    }
                }

                _locationStateFlow.update {
                    _locationStateFlow.value.copy(
                        address = addressTextBuilder.toString()
                    )
                }
            }
        } else {
            _locationStateFlow.update {
                _locationStateFlow.value.copy(
                    address = ""
                )
            }
        }
    }

    fun verifyLocationSettingsStates(states: LocationSettingsStates?) {
        _locationStateFlow.update {
            _locationStateFlow.value.copy(
                isLocationAvailable = states?.isLocationUsable ?: false
            )
        }
    }
    fun checkIfLocationCanBeRetrieved(
        activity: Activity,
        locationLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        client.checkLocationSettings(builder.build()).apply {
            addOnSuccessListener { response ->
                verifyLocationSettingsStates(response.locationSettingsStates)
            }
            addOnFailureListener { exc ->
                _locationStateFlow.update {
                    _locationStateFlow.value.copy(
                        isLocationAvailable = false
                    )
                }
                if (exc is ResolvableApiException) {
                    locationLauncher
                        .launch(IntentSenderRequest.Builder(exc.resolution).build())
                }
            }
        }
    }



}









