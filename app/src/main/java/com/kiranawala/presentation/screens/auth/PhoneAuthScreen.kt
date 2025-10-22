package com.kiranawala.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kiranawala.presentation.navigation.Routes

/**
 * Phone OTP Authentication Screen
 * Handles both login and signup with phone + OTP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var phone by remember { mutableStateOf("+91") }
    var otp by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isNewUser by remember { mutableStateOf(false) }
    
    // Navigate on success
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.navigate(Routes.HomeScreen.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Show messages
    LaunchedEffect(authState.error) {
        authState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AuthEvent.ClearError)
        }
    }
    
    LaunchedEffect(authState.successMessage) {
        authState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AuthEvent.ClearSuccess)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kiranawala",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Login with Phone",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!authState.otpSent) {
                // Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+919876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // New User Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isNewUser,
                        onCheckedChange = { isNewUser = it }
                    )
                    Text("I'm a new user")
                }
                
                // Name field for new users
                if (isNewUser) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Send OTP Button
                Button(
                    onClick = {
                        viewModel.onEvent(AuthEvent.SendOTP(phone))
                    },
                    enabled = phone.length >= 13 && !authState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(if (authState.isLoading) "Sending..." else "Send OTP")
                }
            } else {
                // OTP Input
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("Enter OTP") },
                    placeholder = { Text("123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Verify Button
                Button(
                    onClick = {
                        viewModel.onEvent(
                            AuthEvent.VerifyOTP(
                                phone = phone,
                                otp = otp,
                                name = if (isNewUser) name else null
                            )
                        )
                    },
                    enabled = otp.length == 6 && !authState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(if (authState.isLoading) "Verifying..." else "Verify OTP")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Resend OTP
                TextButton(
                    onClick = {
                        otp = ""
                        viewModel.onEvent(AuthEvent.SendOTP(phone))
                    }
                ) {
                    Text("Resend OTP")
                }
            }
        }
    }
}
