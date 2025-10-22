package com.kiranawala.presentation.screens.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kiranawala.di.SessionManagerEntryPoint
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.presentation.navigation.Routes
import com.kiranawala.utils.logger.KiranaLogger
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Splash screen
 * Shows app logo and checks authentication status
 */
@Composable
fun SplashScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val sessionManager = androidx.compose.runtime.remember {
        EntryPointAccessors.fromApplication(
            context,
            SessionManagerEntryPoint::class.java
        ).sessionManager()
    }
    
    // Check authentication and navigate
    LaunchedEffect(Unit) {
        delay(1500) // 1.5 second splash
        
        try {
            KiranaLogger.d("SplashScreen", "Checking session...")
            
            // Check if user has valid session
            val userId = sessionManager.getCurrentUserId()
            val isLoggedIn = !userId.isNullOrBlank()
            
            KiranaLogger.d("SplashScreen", "Session check: isLoggedIn=$isLoggedIn, userId=$userId")
            
            if (isLoggedIn) {
                // User is logged in, go to home
                KiranaLogger.d("SplashScreen", "User logged in, navigating to Home")
                navController.navigate(Routes.HomeScreen.route) {
                    popUpTo(Routes.SplashScreen.route) { inclusive = true }
                }
            } else {
                // User not logged in, go to login
                KiranaLogger.d("SplashScreen", "User not logged in, navigating to Login")
                navController.navigate(Routes.LoginScreen.route) {
                    popUpTo(Routes.SplashScreen.route) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            KiranaLogger.e("SplashScreen", "Error checking session", e)
            // On error, navigate to login
            navController.navigate(Routes.LoginScreen.route) {
                popUpTo(Routes.SplashScreen.route) { inclusive = true }
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kiranawala",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your Local Grocery Store",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
