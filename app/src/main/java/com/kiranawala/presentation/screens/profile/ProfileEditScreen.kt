package com.kiranawala.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import com.kiranawala.domain.models.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile
    var nameText by remember(profile?.id) { mutableStateOf(profile?.name ?: "") }
    var addressText by remember(profile?.id) { mutableStateOf(profile?.address ?: "") }
    
    // Update fields when profile changes
    LaunchedEffect(profile) {
        profile?.let {
            if (nameText.isEmpty()) nameText = it.name
            if (addressText.isEmpty()) addressText = it.address
        }
    }
    
    // Handle success message
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onSaveClick() // Navigate back on success
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d("ProfileEditScreen", "Save clicked - profile: ${uiState.userProfile?.id}, name: $nameText, address: $addressText")
                            val currentProfile = uiState.userProfile
                            if (currentProfile != null) {
                                val updatedProfile = currentProfile.copy(
                                    name = nameText.trim().ifEmpty { "Enter Name" },
                                    address = addressText.trim().ifEmpty { "Enter Address" }
                                )
                                Log.d("ProfileEditScreen", "Calling updateProfile for user: ${updatedProfile.id}")
                                viewModel.handleEvent(ProfileUiEvent.UpdateProfile(updatedProfile))
                            } else {
                                Log.e("ProfileEditScreen", "Profile is NULL - cannot save!")
                            }
                        },
                        enabled = !uiState.isLoading && uiState.userProfile != null
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phone Number (Read-only)
            OutlinedTextField(
                value = uiState.userProfile?.phone ?: "",
                onValueChange = { },
                label = { Text("Phone Number") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            // Name (Editable)
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Full Name") },
                placeholder = { Text("Enter Name") },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                supportingText = {
                    Text("Your name as it appears on orders")
                }
            )
            
            // Address (Editable)
            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Address") },
                placeholder = { Text("Enter Address") },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                minLines = 2,
                maxLines = 4,
                supportingText = {
                    Text("Your delivery address")
                }
            )
            
            // Error message
            uiState.error?.let { errorMessage ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}