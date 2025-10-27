package com.kiranawala.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Utility object for GPS location detection and reverse geocoding
 */
object LocationUtils {

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current GPS location
     * @return LatLng or null if unable to get location
     */
    suspend fun getCurrentLocation(context: Context): LatLng? {
        if (!hasLocationPermission(context)) {
            return null
        }

        return try {
            withTimeout(10000L) { // 10 second timeout
                val fusedLocationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)

                val cancellationTokenSource = CancellationTokenSource()

                suspendCancellableCoroutine { continuation ->
                    val currentLocationTask = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    )

                    currentLocationTask.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(LatLng(location.latitude, location.longitude))
                        } else {
                            // Fallback to last known location
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                                if (lastLocation != null) {
                                    continuation.resume(LatLng(lastLocation.latitude, lastLocation.longitude))
                                } else {
                                    continuation.resume(null)
                                }
                            }.addOnFailureListener {
                                continuation.resume(null)
                            }
                        }
                    }

                    currentLocationTask.addOnFailureListener {
                        continuation.resume(null)
                    }

                    continuation.invokeOnCancellation {
                        cancellationTokenSource.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reverse geocode coordinates to human-readable address
     * @return formatted address string or null
     */
    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double
    ): LocationAddress? {
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

            address?.let {
                LocationAddress(
                    formattedAddress = it.getAddressLine(0) ?: "",
                    city = it.locality ?: it.subAdminArea ?: "",
                    state = it.adminArea ?: "",
                    country = it.countryName ?: "",
                    latitude = latitude,
                    longitude = longitude
                )
            }
        } catch (e: IOException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Get shortened display address (City, State)
     */
    fun getShortAddress(locationAddress: LocationAddress?): String {
        if (locationAddress == null) return "Location not available"
        
        return buildString {
            if (locationAddress.city.isNotBlank()) {
                append(locationAddress.city)
            }
            if (locationAddress.state.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append(locationAddress.state)
            }
            if (isEmpty()) {
                append("Unknown location")
            }
        }
    }

    /**
     * Get medium-length display address with street
     */
    fun getMediumAddress(locationAddress: LocationAddress?): String {
        if (locationAddress == null) return "Location not available"
        
        val address = locationAddress.formattedAddress
        // Extract first line (street address) from formatted address
        val firstLine = address.split(",").firstOrNull()?.trim() ?: ""
        
        return if (firstLine.isNotBlank() && firstLine.length < 50) {
            firstLine
        } else {
            getShortAddress(locationAddress)
        }
    }
}

/**
 * Data class to hold location address information
 */
data class LocationAddress(
    val formattedAddress: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
