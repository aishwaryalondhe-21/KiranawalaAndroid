package com.kiranawala.presentation.screens.location

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.AddressType
import com.kiranawala.presentation.components.PlaceDetails
import com.kiranawala.presentation.components.PlacesAutocompleteField
import com.kiranawala.presentation.components.modern.ModernCard
import com.kiranawala.presentation.viewmodels.AddressViewModel
import com.kiranawala.utils.LocationAddress
import com.kiranawala.utils.LocationUtils
import kotlinx.coroutines.launch

/**
 * Select Location Screen - Modal for choosing delivery location
 * Features:
 * - Use Current Location button (GPS detection)
 * - Add Address button
 * - List of saved addresses
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SelectLocationScreen(
    onNavigateBack: () -> Unit,
    onAddAddress: () -> Unit,
    onLocationSelected: (LocationAddress) -> Unit,
    viewModel: AddressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isDetectingLocation by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select a Location",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Location Bar
            item {
                SearchLocationBar(
                    query = searchQuery,
                    onPlaceSelected = { placeDetails ->
                        // Convert PlaceDetails to LocationAddress
                        val locationAddress = LocationAddress(
                            formattedAddress = placeDetails.address,
                            city = extractCityFromAddress(placeDetails.address),
                            state = extractStateFromAddress(placeDetails.address),
                            country = "",
                            latitude = placeDetails.latitude,
                            longitude = placeDetails.longitude
                        )
                        
                        // Update current location
                        viewModel.setCurrentLocation(locationAddress)
                        onLocationSelected(locationAddress)
                        
                        // Show feedback and navigate back
                        scope.launch {
                            snackbarHostState.showSnackbar("Location updated to ${placeDetails.name}")
                            onNavigateBack()
                        }
                    }
                )
            }
            
            // Use Current Location Button
            item {
                UseCurrentLocationButton(
                    isLoading = isDetectingLocation,
                    onClick = {
                        if (locationPermissions.allPermissionsGranted) {
                            // Detect GPS location
                            scope.launch {
                                isDetectingLocation = true
                                try {
                                    val gpsLocation = LocationUtils.getCurrentLocation(context)
                                    if (gpsLocation != null) {
                                        val locationAddress = LocationUtils.reverseGeocode(
                                            context,
                                            gpsLocation.latitude,
                                            gpsLocation.longitude
                                        )
                                        if (locationAddress != null) {
                                            viewModel.setCurrentLocation(locationAddress)
                                            onLocationSelected(locationAddress)
                                            snackbarHostState.showSnackbar("Location updated successfully")
                                            onNavigateBack()
                                        } else {
                                            snackbarHostState.showSnackbar("Unable to get address for this location")
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Unable to detect location. Please try again.")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error detecting location: ${e.message}")
                                } finally {
                                    isDetectingLocation = false
                                }
                            }
                        } else {
                            // Request permissions
                            locationPermissions.launchMultiplePermissionRequest()
                        }
                    }
                )
            }

            // Add Address Button
            item {
                AddAddressButton(onClick = onAddAddress)
            }

            // Saved Addresses Section
            item {
                Text(
                    text = "SAVED ADDRESSES",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.addresses.isEmpty()) {
                // Empty state
                item {
                    EmptyAddressesState(onAddAddress = onAddAddress)
                }
            } else {
                // Saved addresses list
                items(
                    items = uiState.addresses,
                    key = { it.id }
                ) { address ->
                    SavedAddressCard(
                        address = address,
                        isDefault = address.isDefault,
                        onClick = {
                            // Convert Address to LocationAddress
                            val locationAddress = LocationAddress(
                                formattedAddress = address.formattedAddress,
                                city = address.city,
                                state = address.state,
                                country = "",
                                latitude = address.latitude,
                                longitude = address.longitude
                            )
                            viewModel.setCurrentLocation(locationAddress)
                            onLocationSelected(locationAddress)
                            scope.launch {
                                snackbarHostState.showSnackbar("Delivering to ${address.city}")
                            }
                            onNavigateBack()
                        }
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Use Current Location Button
 */
@Composable
fun UseCurrentLocationButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 2
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Current Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isLoading) "Detecting location..." else "Use current location",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isLoading) "Please wait" else "Using GPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow
            if (!isLoading) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Add Address Button
 */
@Composable
fun AddAddressButton(onClick: () -> Unit) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 2
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Address",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Add Address",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add a new delivery address",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Saved Address Card
 */
@Composable
fun SavedAddressCard(
    address: Address,
    isDefault: Boolean,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row with icon and label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon based on address type
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (address.addressType) {
                                AddressType.HOME -> MaterialTheme.colorScheme.primaryContainer
                                AddressType.WORK -> MaterialTheme.colorScheme.tertiaryContainer
                                AddressType.OTHER -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (address.addressType) {
                            AddressType.HOME -> Icons.Default.Home
                            AddressType.WORK -> Icons.Default.Work
                            AddressType.OTHER -> Icons.Default.LocationOn
                        },
                        contentDescription = null,
                        tint = when (address.addressType) {
                            AddressType.HOME -> MaterialTheme.colorScheme.primary
                            AddressType.WORK -> MaterialTheme.colorScheme.tertiary
                            AddressType.OTHER -> MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Label and default badge
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (address.addressType) {
                                AddressType.HOME -> "Home"
                                AddressType.WORK -> "Work"
                                AddressType.OTHER -> "Other"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isDefault) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "DEFAULT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Check icon for selection affordance
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Address details
            Text(
                text = address.formattedAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Receiver info
            if (address.receiverName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = address.receiverName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (address.receiverPhone.isNotBlank()) {
                        Text(
                            text = " • ${address.receiverPhone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty Addresses State
 */
@Composable
fun EmptyAddressesState(onAddAddress: () -> Unit) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Text(
                text = "No saved addresses",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Add your first delivery address to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddAddress,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Address")
            }
        }
    }
}

/**
 * Search Location Bar Component
 * Allows users to search for a location using Google Places Autocomplete
 */
@Composable
fun SearchLocationBar(
    query: String,
    onPlaceSelected: (PlaceDetails) -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            PlacesAutocompleteField(
                value = query,
                onPlaceSelected = onPlaceSelected,
                label = "Search location",
                placeholder = "Search for area, street name...",
                supportingText = "Tap to search any location",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Helper function to extract city from formatted address
 * Format: "Street, Area, City, State Pincode, Country"
 */
private fun extractCityFromAddress(address: String): String {
    val parts = address.split(",").map { it.trim() }
    // Typically city is the 3rd or 2nd to last component
    return when {
        parts.size >= 3 -> parts[parts.size - 3].split(" ").firstOrNull() ?: ""
        parts.size >= 2 -> parts[parts.size - 2].split(" ").firstOrNull() ?: ""
        else -> ""
    }
}

/**
 * Helper function to extract state from formatted address
 * Format: "Street, Area, City, State Pincode, Country"
 */
private fun extractStateFromAddress(address: String): String {
    val parts = address.split(",").map { it.trim() }
    // State is typically the 2nd to last component, before country
    return when {
        parts.size >= 2 -> {
            val stateAndPin = parts[parts.size - 2]
            // Extract state name (everything except digits and after)
            stateAndPin.split(" ").filter { !it.all { char -> char.isDigit() } }
                .joinToString(" ")
        }
        else -> ""
    }
}
