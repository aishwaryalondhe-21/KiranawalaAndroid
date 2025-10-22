package com.kiranawala.presentation.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranawala.domain.models.Address
import com.kiranawala.presentation.viewmodels.AddressViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormScreen(
    existingAddress: Address? = null,
    viewModel: AddressViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var addressLine by remember { mutableStateOf(existingAddress?.addressLine ?: "") }
    var buildingName by remember { mutableStateOf(existingAddress?.buildingName ?: "") }
    var flatNumber by remember { mutableStateOf(existingAddress?.flatNumber ?: "") }
    var selectedLabel by remember { mutableStateOf(existingAddress?.label ?: "Home") }
    var isDefault by remember { mutableStateOf(existingAddress?.isDefault ?: false) }
    
    // For now, use default coordinates (Mumbai) - can be replaced with actual location picker
    val latitude = existingAddress?.latitude ?: 19.0760
    val longitude = existingAddress?.longitude ?: 72.8777
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingAddress == null) "Add Address" else "Edit Address") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = addressLine,
                onValueChange = { addressLine = it },
                label = { Text("Address Line *") },
                placeholder = { Text("Enter complete address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2,
                supportingText = {
                    Text("Street, landmark, area, city, state, PIN")
                }
            )
            
            OutlinedTextField(
                value = buildingName,
                onValueChange = { buildingName = it },
                label = { Text("Building/Society Name") },
                placeholder = { Text("Enter building or society name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = flatNumber,
                onValueChange = { flatNumber = it },
                label = { Text("Flat/Unit Number") },
                placeholder = { Text("Enter flat or unit number") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Address Label",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Home", "Work", "Other").forEach { label ->
                    FilterChip(
                        selected = selectedLabel == label,
                        onClick = { selectedLabel = label },
                        label = { Text(label) }
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isDefault,
                        onClick = { isDefault = !isDefault }
                    )
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Set as default address")
                    Text(
                        "This will be used for all deliveries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
            }
            
            Button(
                onClick = {
                    if (addressLine.isNotBlank()) {
                        if (existingAddress == null) {
                            viewModel.addAddress(
                                addressLine = addressLine,
                                buildingName = buildingName.ifBlank { null },
                                flatNumber = flatNumber.ifBlank { null },
                                latitude = latitude,
                                longitude = longitude,
                                label = selectedLabel,
                                isDefault = isDefault
                            )
                        } else {
                            viewModel.updateAddress(
                                existingAddress.copy(
                                    addressLine = addressLine,
                                    buildingName = buildingName.ifBlank { null },
                                    flatNumber = flatNumber.ifBlank { null },
                                    label = selectedLabel,
                                    isDefault = isDefault,
                                    updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                )
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = addressLine.isNotBlank()
            ) {
                Text(if (existingAddress == null) "Add Address" else "Update Address")
            }
        }
    }
}
