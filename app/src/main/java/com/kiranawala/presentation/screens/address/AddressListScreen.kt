package com.kiranawala.presentation.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranawala.R
import com.kiranawala.domain.models.Address
import com.kiranawala.domain.models.AddressType
import com.kiranawala.presentation.components.modern.ModernActionButton
import com.kiranawala.presentation.components.modern.ModernCard
import com.kiranawala.presentation.components.modern.ModernEmptyState
import com.kiranawala.presentation.components.modern.ModernOutlinedButton
import com.kiranawala.presentation.components.modern.ModernSectionHeader
import com.kiranawala.presentation.components.modern.ModernStatusChip
import com.kiranawala.presentation.viewmodels.AddressViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    viewModel: AddressViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAddAddress: (String?) -> Unit,
    onEditAddress: (Address) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacingDefault = dimensionResource(id = R.dimen.spacing_default)
    val spacingSmall = dimensionResource(id = R.dimen.spacing_small)
    val cardSpacing = dimensionResource(id = R.dimen.card_spacing)
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<Address?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Delivery Addresses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAddresses() }) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = spacingDefault),
            verticalArrangement = Arrangement.spacedBy(spacingDefault)
        ) {
            if (uiState.recentSearches.isNotEmpty()) {
                ModernSectionHeader(title = "Recent searches")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall)
                ) {
                    items(uiState.recentSearches) { search ->
                        AssistChip(
                            onClick = { onAddAddress(search) },
                            label = { Text(search) },
                            leadingIcon = {
                                Icon(Icons.Default.History, contentDescription = null)
                            }
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.addresses.isEmpty() -> {
                    ModernEmptyState(
                        icon = Icons.Default.LocationOn,
                        title = "No saved addresses",
                        description = "Add your favourite delivery locations to speed up checkout.",
                        modifier = Modifier.weight(1f),
                        actionText = "Add address",
                        onActionClick = { onAddAddress(null) }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(cardSpacing),
                        contentPadding = PaddingValues(bottom = spacingDefault)
                    ) {
                        items(uiState.addresses, key = { it.id }) { address ->
                            AddressCardItem(
                                address = address,
                                distanceKm = uiState.distanceByAddressId[address.id],
                                onSetDefault = { viewModel.setDefaultAddress(address.id) },
                                onEdit = { onEditAddress(address) },
                                onDelete = { pendingDelete = address }
                            )
                        }
                    }
                }
            }

            ModernActionButton(
                text = "Add new address",
                icon = Icons.Default.Add,
                onClick = { onAddAddress(null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    pendingDelete?.let { address ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete address") },
            text = { Text("Are you sure you want to delete ${address.formattedAddress}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAddress(address.id)
                        pendingDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddressCardItem(
    address: Address,
    distanceKm: Double?,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val spacingSmall = dimensionResource(id = R.dimen.spacing_small)
    val spacingInternal = dimensionResource(id = R.dimen.card_internal_spacing)

    ModernCard(onClick = onEdit) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacingInternal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = address.addressType.toIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = address.addressType.toDisplayName(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${address.city}, ${address.state} • ${address.pincode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (address.isDefault) {
                    ModernStatusChip(
                        text = "Default",
                        icon = Icons.Default.Star,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = address.formattedAddress,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            if (address.addressLine1.isNotBlank()) {
                Text(
                    text = address.addressLine1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            address.addressLine2?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Lat: ${"%.5f".format(address.latitude)}, Lng: ${"%.5f".format(address.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = address.receiverName.ifBlank { "Receiver name not provided" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = address.receiverPhone.ifBlank { "Phone not provided" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            distanceKm?.let { km ->
                Text(
                    text = "Nearest store • ${String.format(Locale.getDefault(), "%.1f km", km)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!address.isDefault) {
                    ModernOutlinedButton(
                        text = "Set default",
                        onClick = onSetDefault,
                        modifier = Modifier.weight(1f)
                    )
                }
                ModernOutlinedButton(
                    text = "Edit",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete address")
                }
            }
        }
    }
}

private fun AddressType.toDisplayName(): String = name.lowercase(Locale.getDefault())
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

private fun AddressType.toIcon(): ImageVector = when (this) {
    AddressType.HOME -> Icons.Default.Home
    AddressType.WORK -> Icons.Default.Work
    AddressType.OTHER -> Icons.Default.Place
}
