package com.csci448.danielsaelens.Geolocatr

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.csci448.danielsaelens.Geolocatr.ui.LocationScreen
import com.csci448.danielsaelens.Geolocatr.util.LocationUtility
import com.google.android.gms.location.LocationSettingsStates



class MainActivity : ComponentActivity() {
    private lateinit var locationUtility: LocationUtility
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationUtility = LocationUtility(this)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            locationUtility.checkPermissionAndGetLocation(this@MainActivity, permissionLauncher)
        }

        locationLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { data ->
                    val states = LocationSettingsStates.fromIntent(data)
                    locationUtility.verifyLocationSettingsStates(states)
                }
            }
        }

        setContent {
            val state = locationUtility.use(this@MainActivity)
            LaunchedEffect(state.location) {
                locationUtility.getAddress(state.location)
            }
            LocationScreen(
                location = state.location,
                isLocationAvailable = state.isLocationAvailable,
                onGetLocation = {
                    locationUtility.checkPermissionAndGetLocation(this@MainActivity, permissionLauncher)
                },
                address = state.address
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUtility.removeLocationRequest()
    }
    override fun onStart(){
        super.onStart()
        locationUtility.checkIfLocationCanBeRetrieved(this,locationLauncher )
    }
}

