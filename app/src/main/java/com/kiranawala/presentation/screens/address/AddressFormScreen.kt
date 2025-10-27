package com.kiranawala.presentation.screens.address

import android.content.Context
import android.location.Address as GeoAddress
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import com.kiranawala.R
import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.AddressType
import com.kiranawala.presentation.components.PlaceDetails
import com.kiranawala.presentation.components.PlacesAutocompleteField
import com.kiranawala.presentation.components.modern.ModernActionButton
import com.kiranawala.presentation.components.modern.ModernCard
import com.kiranawala.presentation.components.modern.ModernSectionHeader
import com.kiranawala.presentation.viewmodels.AddressFormField
import com.kiranawala.presentation.viewmodels.AddressViewModel
import com.kiranawala.presentation.viewmodels.LocationSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

private const val KEY_SELECTED_FORMATTED_ADDRESS = "selected_formatted_address"
private const val KEY_SELECTED_ADDRESS_LINE1 = "selected_address_line1"
private const val KEY_SELECTED_ADDRESS_LINE2 = "selected_address_line2"
private const val KEY_SELECTED_CITY = "selected_city"
private const val KEY_SELECTED_STATE = "selected_state"
private const val KEY_SELECTED_PINCODE = "selected_pincode"
private const val KEY_SELECTED_LATITUDE = "selected_latitude"
private const val KEY_SELECTED_LONGITUDE = "selected_longitude"
private const val KEY_INITIAL_QUERY = "address_form_initial_query"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormScreen(
    existingAddress: Address? = null,
    savedStateHandle: SavedStateHandle? = null,
    autoLaunchLocation: Boolean = false,
    viewModel: AddressViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSelectLocation: (latitude: Double?, longitude: Double?, query: String?) -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val spacingDefault = dimensionResource(id = R.dimen.spacing_default)
    val spacingSmall = dimensionResource(id = R.dimen.spacing_small)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var locationPrompted by remember { mutableStateOf(false) }
    var geocodeError by remember { mutableStateOf<String?>(null) }
    var pendingQuery by remember { mutableStateOf<String?>(null) }
    var locationDataProcessed by remember { mutableStateOf(false) }

    LaunchedEffect(existingAddress?.id) {
        viewModel.initializeForm(existingAddress?.id)
    }

    LaunchedEffect(savedStateHandle) {
        val initialQuery = savedStateHandle?.get<String>(KEY_INITIAL_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            pendingQuery = initialQuery
            savedStateHandle.remove<String>(KEY_INITIAL_QUERY)
        }
    }

    // Process location selection from LocationPickerScreen (runs once per composition)
    LaunchedEffect(Unit) {
        savedStateHandle?.let { handle ->
            // Observe for location data from LocationPickerScreen
            val savedFormatted = handle.get<String>(KEY_SELECTED_FORMATTED_ADDRESS)
            val savedLat = handle.get<Double>(KEY_SELECTED_LATITUDE)
            val savedLng = handle.get<Double>(KEY_SELECTED_LONGITUDE)
            
            if (!savedFormatted.isNullOrBlank() && savedLat != null && savedLng != null && !locationDataProcessed) {
                val selection = LocationSelection(
                    formattedAddress = savedFormatted,
                    addressLine1 = handle.get<String>(KEY_SELECTED_ADDRESS_LINE1).orEmpty(),
                    addressLine2 = handle.get<String>(KEY_SELECTED_ADDRESS_LINE2),
                    city = handle.get<String>(KEY_SELECTED_CITY).orEmpty(),
                    state = handle.get<String>(KEY_SELECTED_STATE).orEmpty(),
                    pincode = handle.get<String>(KEY_SELECTED_PINCODE).orEmpty(),
                    latitude = savedLat,
                    longitude = savedLng
                )
                
                // Update ViewModel with selected location
                viewModel.onLocationSelected(selection)
                locationDataProcessed = true

                // Clear savedStateHandle after successful processing
                handle.remove<String>(KEY_SELECTED_FORMATTED_ADDRESS)
                handle.remove<String>(KEY_SELECTED_ADDRESS_LINE1)
                handle.remove<String>(KEY_SELECTED_ADDRESS_LINE2)
                handle.remove<String>(KEY_SELECTED_CITY)
                handle.remove<String>(KEY_SELECTED_STATE)
                handle.remove<String>(KEY_SELECTED_PINCODE)
                handle.remove<Double>(KEY_SELECTED_LATITUDE)
                handle.remove<Double>(KEY_SELECTED_LONGITUDE)
            }
        }
    }

    LaunchedEffect(autoLaunchLocation, formState.id) {
        if (autoLaunchLocation && formState.id == null && !locationPrompted) {
            locationPrompted = true
            onSelectLocation(formState.latitude, formState.longitude, pendingQuery ?: formState.formattedAddress.ifBlank { null })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (formState.id == null) "Add delivery address" else "Edit delivery address") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = spacingDefault, vertical = spacingSmall),
            verticalArrangement = Arrangement.spacedBy(spacingDefault)
        ) {
            if (formState.isSubmitting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            ModernCard {
                Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    ModernSectionHeader(title = "Location details")

                    PlacesAutocompleteField(
                        value = formState.formattedAddress,
                        onPlaceSelected = { place ->
                            pendingQuery = null
                            coroutineScope.launch {
                                val selection = fetchSelectionFromPlace(context, place)
                                if (selection != null) {
                                    geocodeError = null
                                    viewModel.onLocationSelected(selection)
                                } else {
                                    geocodeError = "Unable to parse this location. Please refine on the map."
                                }
                            }
                        },
                        supportingText = "Search and select your delivery location",
                        isError = formState.validationErrors.formattedAddressError != null || geocodeError != null,
                        errorMessage = formState.validationErrors.formattedAddressError ?: geocodeError
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Need precise pin?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = {
                                locationPrompted = true
                                onSelectLocation(
                                    formState.latitude,
                                    formState.longitude,
                                    pendingQuery ?: formState.formattedAddress.ifBlank { null }
                                )
                            }
                        ) {
                            Icon(Icons.Filled.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick on map")
                        }
                    }
                }
            }

            ModernCard {
                Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    ModernSectionHeader(title = "Address information")

                    OutlinedTextField(
                        value = formState.addressLine1,
                        onValueChange = { viewModel.updateField(AddressFormField.ADDRESS_LINE1, it) },
                        label = { Text("House / building name *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = formState.validationErrors.addressLine1Error != null,
                        supportingText = formState.validationErrors.addressLine1Error?.let { error ->
                            { Text(error) }
                        }
                    )

                    OutlinedTextField(
                        value = formState.addressLine2,
                        onValueChange = { viewModel.updateField(AddressFormField.ADDRESS_LINE2, it) },
                        label = { Text("Landmark or area") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Optional") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall)
                    ) {
                        OutlinedTextField(
                            value = formState.city,
                            onValueChange = { viewModel.updateField(AddressFormField.CITY, it) },
                            label = { Text("City *") },
                            modifier = Modifier.weight(1f),
                            isError = formState.validationErrors.cityError != null,
                            supportingText = formState.validationErrors.cityError?.let { error ->
                                { Text(error) }
                            }
                        )

                        OutlinedTextField(
                            value = formState.state,
                            onValueChange = { viewModel.updateField(AddressFormField.STATE, it) },
                            label = { Text("State *") },
                            modifier = Modifier.weight(1f),
                            isError = formState.validationErrors.stateError != null,
                            supportingText = formState.validationErrors.stateError?.let { error ->
                                { Text(error) }
                            }
                        )
                    }

                    OutlinedTextField(
                        value = formState.pincode,
                        onValueChange = { value ->
                            if (value.length <= 6) {
                                viewModel.updateField(
                                    AddressFormField.PINCODE,
                                    value.filter { it.isDigit() }
                                )
                            }
                        },
                        label = { Text("Pincode *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        isError = formState.validationErrors.pincodeError != null,
                        supportingText = formState.validationErrors.pincodeError?.let { error ->
                            { Text(error) }
                        }
                    )
                }
            }

            ModernCard {
                Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    ModernSectionHeader(title = "Contact details")

                    OutlinedTextField(
                        value = formState.receiverName,
                        onValueChange = { viewModel.updateField(AddressFormField.RECEIVER_NAME, it) },
                        label = { Text("Receiver name *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        isError = formState.validationErrors.receiverNameError != null,
                        supportingText = formState.validationErrors.receiverNameError?.let { error ->
                            { Text(error) }
                        }
                    )

                    OutlinedTextField(
                        value = formState.receiverPhone,
                        onValueChange = { value ->
                            if (value.length <= 10) {
                                viewModel.updateField(
                                    AddressFormField.RECEIVER_PHONE,
                                    value.filter { it.isDigit() }
                                )
                            }
                        },
                        label = { Text("Receiver phone *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        isError = formState.validationErrors.receiverPhoneError != null,
                        supportingText = formState.validationErrors.receiverPhoneError?.let { error ->
                            { Text(error) }
                        }
                    )
                }
            }

            ModernCard {
                Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    ModernSectionHeader(title = "Address type")
                    val addressTypes = AddressType.entries.toList()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall)
                    ) {
                        addressTypes.forEach { type ->
                            FilterChip(
                                selected = formState.addressType == type,
                                onClick = { viewModel.updateAddressType(type) },
                                label = { Text(typeDisplayName(type)) },
                                leadingIcon = if (formState.addressType == type) {
                                    { Icon(Icons.Filled.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(formState.isDefault) { viewModel.toggleDefault() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Use as default delivery address",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "We'll auto-select this address for quick checkout.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = formState.isDefault,
                            onCheckedChange = { viewModel.toggleDefault() }
                        )
                    }
                }
            }

            if (formState.validationErrors.formattedAddressError != null && geocodeError != null) {
                Snackbar {
                    Text(text = geocodeError ?: formState.validationErrors.formattedAddressError.orEmpty())
                }
            }

            ModernActionButton(
                text = if (formState.id == null) "Save address" else "Update address",
                icon = Icons.Filled.Check,
                enabled = !formState.isSubmitting,
                onClick = {
                    focusManager.clearFocus(force = true)
                    viewModel.saveAddress(onSuccess = onNavigateBack)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacingDefault))
        }
    }
}

private suspend fun fetchSelectionFromPlace(
    context: Context,
    place: PlaceDetails
): LocationSelection? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val resolvedAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<GeoAddress?> { continuation ->
                    geocoder.getFromLocation(place.latitude, place.longitude, 1) { addresses ->
                        continuation.resume(addresses?.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(place.latitude, place.longitude, 1)?.firstOrNull()
            }

            resolvedAddress?.toLocationSelection(place) ?: LocationSelection(
                formattedAddress = place.address,
                addressLine1 = place.name.ifBlank { place.address },
                addressLine2 = null,
                city = "",
                state = "",
                pincode = "",
                latitude = place.latitude,
                longitude = place.longitude
            )
        } catch (_: IOException) {
            null
        }
    }
}

private fun GeoAddress.toLocationSelection(place: PlaceDetails): LocationSelection {
    val line1 = listOfNotNull(subThoroughfare, thoroughfare)
        .joinToString(" ")
        .ifBlank { place.name.ifBlank { place.address } }
    val line2 = listOfNotNull(subLocality, premises, featureName)
        .firstOrNull { it.isNotBlank() }
    val city = locality ?: subAdminArea ?: adminArea ?: ""
    val state = adminArea ?: subAdminArea ?: ""
    val postalCode = postalCode ?: ""

    return LocationSelection(
        formattedAddress = place.address,
        addressLine1 = line1,
        addressLine2 = line2,
        city = city,
        state = state,
        pincode = postalCode,
        latitude = place.latitude,
        longitude = place.longitude
    )
}

private fun typeDisplayName(type: AddressType): String = when (type) {
    AddressType.HOME -> "Home"
    AddressType.WORK -> "Work"
    AddressType.OTHER -> "Other"
}
