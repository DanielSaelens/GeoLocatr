package com.csci448.danielsaelens.Geolocatr.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csci448.danielsaelens.Geolocatr.data.LocationPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DataStoreManager(context: Context) {
    private val _dataStore = context.dataStore
   companion object{
       private const val DATA_STORE_NAME = "geolocatr_preferences"
       private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = DATA_STORE_NAME)

   } private val _dataflow: Flow<LocationPreferences> =
        _dataStore.data.map { preferences ->
            LocationPreferences(
                isTrafficEnabled = preferences[LocationPreferences.IS_TRAFFIC_ENABLED_DATA_KEY] ?: false,
                isZoomControlEnabled = preferences[LocationPreferences.IS_ZOOM_CONTROL_ENABLED_KEY] ?: false
            )
        }
    @Composable
    fun use(lifecycleOwner: LifecycleOwner) = _dataflow
        .collectAsStateWithLifecycle(
            initialValue = LocationPreferences(),
            lifecycleOwner = lifecycleOwner
        )
        .value
    fun setIsTrafficEnabled(boolean: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            _dataStore.edit { preferences ->
                preferences[LocationPreferences.IS_TRAFFIC_ENABLED_DATA_KEY] = boolean

            }
        }
    }
    fun setIsZoomEnabled(boolean: Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            _dataStore.edit { preferences ->
                preferences[LocationPreferences.IS_ZOOM_CONTROL_ENABLED_KEY] = boolean
            }
        }
    }

}




