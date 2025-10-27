package com.kiranawala.presentation.screens.address

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.kiranawala.presentation.components.PlaceDetails
import com.kiranawala.presentation.components.PlacesAutocompleteField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

data class LocationPickerResult(
    val formattedAddress: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val pincode: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    initialQuery: String? = null,
    onNavigateBack: () -> Unit,
    onLocationConfirmed: (LocationPickerResult) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultLatLng = LatLng(initialLatitude ?: 19.0760, initialLongitude ?: 72.8777)
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 16f)
    }

    var selectedResult by remember { mutableStateOf<LocationPickerResult?>(null) }
    var displayedAddress by remember { mutableStateOf(initialQuery.orEmpty()) }
    var isGeocoding by remember { mutableStateOf(false) }
    var geocodingError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .filter { moving: Boolean -> !moving }
            .map { _: Boolean -> cameraPositionState.position.target }
            .collectLatest { target ->
                isGeocoding = true
                geocodingError = null
                val geocoded = withContext(Dispatchers.IO) {
                    reverseGeocode(context, target.latitude, target.longitude)
                }
                if (geocoded != null) {
                    selectedResult = geocoded
                    displayedAddress = geocoded.formattedAddress
                } else {
                    geocodingError = "Unable to fetch address for selected location"
                }
                isGeocoding = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select delivery location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    PlacesAutocompleteField(
                        value = displayedAddress,
                        onPlaceSelected = { place ->
                            displayedAddress = place.address
                            selectedResult = place.toResult()
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        LatLng(place.latitude, place.longitude),
                                        cameraPositionState.position.zoom
                                    )
                                )
                            }
                        },
                        supportingText = "Search any landmark, society or street",
                        label = "Search for a location",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selected address",
                    style = MaterialTheme.typography.titleMedium
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = displayedAddress.ifBlank { "Move the map or search to select a spot" },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isGeocoding) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                geocodingError?.let { error ->
                    Snackbar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                Button(
                    onClick = {
                        selectedResult?.let { onLocationConfirmed(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedResult != null && !isGeocoding
                ) {
                    Text("Confirm location")
                }
            }
        }
    }
}

private suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
): LocationPickerResult? {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine<Address?> { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    continuation.resume(addresses?.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        }

        address?.toResult(latitude, longitude)
    } catch (_: IOException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun Address.toResult(lat: Double, lng: Double): LocationPickerResult {
    val line1 = listOfNotNull(subThoroughfare, thoroughfare)
        .joinToString(" ")
        .ifBlank { getAddressLine(0) ?: "" }
    val line2 = listOfNotNull(subLocality, premises, featureName)
        .firstOrNull { it.isNotBlank() }
    val cityValue = locality ?: subAdminArea ?: adminArea ?: ""
    val stateValue = adminArea ?: subAdminArea ?: ""
    val pinValue = postalCode ?: ""

    return LocationPickerResult(
        formattedAddress = getAddressLine(0) ?: "$line1 ${line2.orEmpty()}".trim(),
        addressLine1 = line1,
        addressLine2 = line2,
        city = cityValue,
        state = stateValue,
        pincode = pinValue,
        latitude = lat,
        longitude = lng
    )
}

private fun PlaceDetails.toResult(): LocationPickerResult {
    return LocationPickerResult(
        formattedAddress = address,
        addressLine1 = name.ifBlank { address },
        addressLine2 = null,
        city = "",
        state = "",
        pincode = "",
        latitude = latitude,
        longitude = longitude
    )
}
