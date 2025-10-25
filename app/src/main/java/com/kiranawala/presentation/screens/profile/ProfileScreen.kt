package com.kiranawala.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranawala.domain.models.UserProfile
import com.kiranawala.presentation.components.modern.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAddressManagementClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    onSecuritySettingsClick: () -> Unit,
    onOrderHistoryClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                ModernProfileHeader(
                    profile = uiState.userProfile,
                    onEditClick = onEditProfileClick
                )
            }
            
            // Quick Actions Grid
            item {
                ModernSectionHeader(title = "Quick Access")
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernQuickActionCard(
                        icon = Icons.Default.History,
                        title = "Orders",
                        subtitle = "View history",
                        onClick = onOrderHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                    ModernQuickActionCard(
                        icon = Icons.Default.LocationOn,
                        title = "Addresses",
                        subtitle = "Manage",
                        onClick = onAddressManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Settings Section
            item {
                ModernSectionHeader(title = "Preferences")
            }
            
            item {
                ModernSettingsCard(
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Push, email & SMS alerts",
                            onClick = onNotificationSettingsClick
                        ),
                        SettingsItem(
                            icon = Icons.Default.Settings,
                            title = "App Settings",
                            subtitle = "Language, theme & more",
                            onClick = onAppSettingsClick
                        ),
                        SettingsItem(
                            icon = Icons.Default.Security,
                            title = "Security",
                            subtitle = "Privacy & account security",
                            onClick = onSecuritySettingsClick
                        )
                    )
                )
            }
            
            // Support Section
            item {
                ModernSectionHeader(title = "Support")
            }
            
            item {
                ModernSettingsCard(
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.Help,
                            title = "Help & Support",
                            subtitle = "FAQs & customer support",
                            onClick = { /* TODO: Implement help */ }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "About",
                            subtitle = "Version & app info",
                            onClick = onAboutClick
                        )
                    )
                )
            }
            
            // Logout Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ModernActionButton(
                    text = "Logout",
                    icon = Icons.Default.Logout,
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ModernProfileHeader(
    profile: UserProfile?,
    onEditClick: () -> Unit
) {
    ModernCard(elevation = 3) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar with initials
            val initials = profile?.name?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }
                ?.take(2)?.joinToString("") ?: "?"
            
            ModernAvatar(
                initials = initials.uppercase(),
                size = 80
            )
            
            // Profile Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = profile?.name ?: "Loading...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = profile?.phone ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Edit Button
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ModernQuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 2
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModernSettingsCard(items: List<SettingsItem>) {
    ModernCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(vertical = 16.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon with background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Text content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Chevron icon
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Divider between items
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)