package com.csci448.danielsaelens.Geolocatr.data

import androidx.datastore.preferences.core.booleanPreferencesKey

data class LocationPreferences(
    val isTrafficEnabled: Boolean = false,
    val isZoomControlEnabled: Boolean = false

) {
    companion object {
        internal val IS_TRAFFIC_ENABLED_DATA_KEY = booleanPreferencesKey("is_traffic_enabled")
        internal val IS_ZOOM_CONTROL_ENABLED_KEY = booleanPreferencesKey("is_zoom_control_enabled")

    }
}
