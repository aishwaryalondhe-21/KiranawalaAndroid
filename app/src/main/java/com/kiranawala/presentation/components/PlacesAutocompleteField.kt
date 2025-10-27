package com.kiranawala.presentation.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

data class PlaceDetails(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesAutocompleteField(
    value: String,
    onPlaceSelected: (PlaceDetails) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Address",
    placeholder: String = "Click to search address",
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }
    var currentErrorMessage by remember { mutableStateOf("") }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { data ->
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val placeDetails = PlaceDetails(
                        id = place.id ?: "",
                        name = place.name ?: "",
                        address = place.address ?: "",
                        latitude = place.latLng?.latitude ?: 0.0,
                        longitude = place.latLng?.longitude ?: 0.0
                    )
                    onPlaceSelected(placeDetails)
                    showError = false
                }
            }
            AutocompleteActivity.RESULT_ERROR -> {
                result.data?.let { data ->
                    val status: Status = Autocomplete.getStatusFromIntent(data)
                    currentErrorMessage = status.statusMessage ?: "Unknown error occurred"
                    showError = true
                }
            }
            Activity.RESULT_CANCELED -> {
                // User cancelled, do nothing
            }
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location"
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                    )
                    
                    val intent = Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(context)
                    
                    launcher.launch(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        supportingText = if (showError && currentErrorMessage.isNotEmpty()) {
            { Text(currentErrorMessage) }
        } else if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else if (supportingText != null) {
            { Text(supportingText) }
        } else null,
        isError = showError || isError,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
                
                val intent = Autocomplete
                    .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(context)
                
                launcher.launch(intent)
            },
        readOnly = true,
        singleLine = false,
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors()
    )
}
