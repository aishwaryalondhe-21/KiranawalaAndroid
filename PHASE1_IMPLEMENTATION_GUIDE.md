# Phase 1 Implementation Guide - Kiranawala v2.0

## ✅ Completed Steps

### 1. Database Schema
- ✅ Created `SUPABASE_ADDRESSES_SCHEMA.sql` with addresses table and RLS policies
- ✅ Includes triggers for auto-updating timestamps and ensuring single default address

### 2. Domain Layer
- ✅ Created `Address.kt` domain model
- ✅ Created `AddressRepository.kt` interface

### 3. Data Layer
- ✅ Created `AddressRepositoryImpl.kt` with full Supabase integration
- ✅ Updated `RepositoryModule.kt` with AddressRepository binding

### 4. Presentation Layer
- ✅ Created `AddressViewModel.kt` for state management

---

## 🔧 Remaining Implementation Steps

### Step 1: Add Google Maps Dependencies

**File:** `app/build.gradle.kts`

Add these dependencies after existing ones:

```kotlin
// Google Maps & Location
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.1.0")
implementation("com.google.maps.android:maps-compose:4.3.0")

// Geocoding (for address search)
implementation("com.google.android.gms:play-services-places:17.0.0")
```

### Step 2: Add Google Maps API Key

**File:** `app/src/main/AndroidManifest.xml`

Add inside `<application>` tag:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY"/>
```

### Step 3: Add Location Permissions

**File:** `app/src/main/AndroidManifest.xml`

Add before `<application>` tag:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 4: Setup Supabase Table

Execute the SQL in `SUPABASE_ADDRESSES_SCHEMA.sql` in your Supabase SQL Editor.

---

## 📱 UI Screens Implementation

### AddressListScreen.kt

Create: `app/src/main/java/com/kiranawala/presentation/screens/address/AddressListScreen.kt`

```kotlin
package com.kiranawala.presentation.screens.address

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranawala.domain.models.Address
import com.kiranawala.presentation.viewmodels.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    viewModel: AddressViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (Address) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAddress) {
                Icon(Icons.Default.Add, "Add Address")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.addresses.isEmpty() -> {
                    EmptyAddressesView(
                        onAddAddress = onAddAddress,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.addresses) { address ->
                            AddressCard(
                                address = address,
                                onSetDefault = { viewModel.setDefaultAddress(address.id) },
                                onEdit = { onEditAddress(address) },
                                onDelete = { viewModel.deleteAddress(address.id) }
                            )
                        }
                    }
                }
            }
            
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun EmptyAddressesView(
    onAddAddress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No addresses yet",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your delivery address to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddAddress) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Address")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressCard(
    address: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (address.isDefault) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (address.label) {
                            "Home" -> Icons.Default.Home
                            "Work" -> Icons.Default.Work
                            else -> Icons.Default.LocationOn
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                if (address.isDefault) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Default") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            address.buildingName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            address.flatNumber?.let {
                Text(
                    text = "Flat/Unit: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = address.addressLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!address.isDefault) {
                    OutlinedButton(
                        onClick = onSetDefault,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set as Default")
                    }
                }
                
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Address") },
            text = { Text("Are you sure you want to delete this address?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

---

### AddressFormScreen.kt (Simplified - Without Google Maps)

Create: `app/src/main/java/com/kiranawala/presentation/screens/address/AddressFormScreen.kt`

```kotlin
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
    
    // For now, use default coordinates (can be replaced with actual location picker)
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )
            
            OutlinedTextField(
                value = buildingName,
                onValueChange = { buildingName = it },
                label = { Text("Building/Society Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = flatNumber,
                onValueChange = { flatNumber = it },
                label = { Text("Flat/Unit Number") },
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
                Text("Set as default address")
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
```

---

## 🔐 Persistent Login Enhancement

### Update PreferencesManager

**File:** `app/src/main/java/com/kiranawala/data/local/preferences/PreferencesManager.kt`

Add theme preference keys:

```kotlin
val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"

val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
    preferences[THEME_MODE] ?: "system"
}

suspend fun saveThemeMode(mode: String) {
    context.dataStore.edit { preferences ->
        preferences[THEME_MODE] = mode
    }
}
```

### Update AuthRepositoryImpl for Session Restore

The current implementation already handles persistent login via `isLoggedIn()` method. Ensure the app checks session on startup in `MainActivity`:

**File:** `app/src/main/java/com/kiranawala/MainActivity.kt`

In the onCreate or initial composable, add:

```kotlin
val authViewModel: AuthViewModel = hiltViewModel()

LaunchedEffect(Unit) {
    authViewModel.checkSession() // Verify Supabase session
}
```

---

## 🎨 Theme Toggle Implementation

### Update Theme File

**File:** `app/src/main/java/com/kiranawala/presentation/theme/Theme.kt`

Make sure the theme supports dark mode toggle and reads from PreferencesManager.

### Add Theme Settings to Settings Screen

**File:** `app/src/main/java/com/kiranawala/presentation/screens/settings/SettingsScreen.kt`

Add theme selector:

```kotlin
@Composable
fun ThemeSettingsSection(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Column {
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = currentTheme == "light",
                onClick = { onThemeChange("light") },
                label = { Text("Light") }
            )
            FilterChip(
                selected = currentTheme == "dark",
                onClick = { onThemeChange("dark") },
                label = { Text("Dark") }
            )
            FilterChip(
                selected = currentTheme == "system",
                onClick = { onThemeChange("system") },
                label = { Text("System") }
            )
        }
    }
}
```

---

## 🧭 Navigation Integration

### Update Routes.kt

**File:** `app/src/main/java/com/kiranawala/presentation/navigation/Routes.kt`

Add:

```kotlin
object AddressList : Route("address_list")
object AddressForm : Route("address_form?addressId={addressId}") {
    fun createRoute(addressId: String? = null) =
        if (addressId != null) "address_form?addressId=$addressId" else "address_form"
}
```

### Update NavigationGraph.kt

**File:** `app/src/main/java/com/kiranawala/presentation/navigation/NavigationGraph.kt`

Add composable routes:

```kotlin
composable(Routes.AddressList.route) {
    AddressListScreen(
        onNavigateBack = { navController.navigateUp() },
        onAddAddress = { navController.navigate(Routes.AddressForm.createRoute()) },
        onEditAddress = { address ->
            navController.navigate(Routes.AddressForm.createRoute(address.id))
        }
    )
}

composable(
    route = Routes.AddressForm.route,
    arguments = listOf(navArgument("addressId") { 
        type = NavType.StringType
        nullable = true
    })
) { backStackEntry ->
    val addressId = backStackEntry.arguments?.getString("addressId")
    val viewModel: AddressViewModel = hiltViewModel()
    val address = addressId?.let { id ->
        viewModel.uiState.value.addresses.find { it.id == id }
    }
    
    AddressFormScreen(
        existingAddress = address,
        onNavigateBack = { navController.navigateUp() }
    )
}
```

---

## ✅ Testing Checklist

1. **Supabase Setup**
   - [ ] Execute `SUPABASE_ADDRESSES_SCHEMA.sql` in Supabase SQL Editor
   - [ ] Verify table created with proper RLS policies

2. **Build & Compile**
   - [ ] Sync Gradle after adding dependencies
   - [ ] Fix any compilation errors
   - [ ] Build succeeds

3. **Address Management**
   - [ ] Open Address List screen
   - [ ] Add new address with all fields
   - [ ] Edit existing address
   - [ ] Set address as default
   - [ ] Delete address
   - [ ] Verify data persists in Supabase

4. **Persistent Login**
   - [ ] Login with OTP
   - [ ] Close app completely
   - [ ] Reopen app - should remain logged in
   - [ ] Logout - should clear session

5. **Theme Toggle**
   - [ ] Switch to Dark mode - UI updates
   - [ ] Switch to Light mode - UI updates
   - [ ] Switch to System - follows system theme
   - [ ] Restart app - theme persists

---

## 🚀 Next Steps (Phase 2)

Once Phase 1 is complete and tested:
- Implement "Thank You" order acknowledgement screen
- Add itemized bills in order history
- Add "Call Store" functionality
- Create "About Us" page

---

## 📝 Notes

- Google Maps integration can be enhanced later with actual map picker
- Current implementation uses simplified address form without map
- Location permissions are added but not enforced yet (add runtime permission requests)
- Theme implementation assumes you have Material3 theming setup

For any issues, check logs with tag `AddressViewModel` and `AddressRepository`.
