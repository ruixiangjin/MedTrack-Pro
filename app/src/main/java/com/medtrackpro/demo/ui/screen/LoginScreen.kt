package com.medtrackpro.demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrackpro.demo.R
import com.medtrackpro.demo.viewmodel.PatientViewModel

@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    onLoginSuccess: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    val context = LocalContext.current
    val patientViewModel: PatientViewModel = viewModel(
        factory = PatientViewModel.PatientViewModelFactory(context)
    )
    val patientUiState by patientViewModel.uiState.collectAsState()

    var patientId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isClaimMode by remember { mutableStateOf(false) }
    var claimPatientId by remember { mutableStateOf("") }
    var claimPhoneNumber by remember { mutableStateOf("") }
    var claimPassword by remember { mutableStateOf("") }
    var claimConfirmPassword by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(patientUiState.loginSucceeded) {
        val loggedInPatientId = patientUiState.loggedInPatientId
        if (patientUiState.loginSucceeded && loggedInPatientId != null) {
            patientViewModel.clearLoginResult()
            onLoginSuccess(loggedInPatientId)
        }
    }

    LaunchedEffect(patientUiState.claimMessage) {
        if (patientUiState.claimMessage != null) {
            isClaimMode = false
            claimPatientId = ""
            claimPhoneNumber = ""
            claimPassword = ""
            claimConfirmPassword = ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.secondary,
                                    colorScheme.secondaryContainer
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeroIcon(
                            backgroundColor = colorScheme.primaryContainer
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Welcome Back",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.onSecondary
                            )

                            Text(
                                text = "Sign in to access your medications and symptom records.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isClaimMode) "Claim Existing Account" else "Login to MedTrack",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )

                    Text(
                        text = if (isClaimMode) {
                            "Use your Patient ID and phone number to set a new password."
                        } else {
                            "Use your Patient ID and password to continue."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )

                    if (isClaimMode) {
                        OutlinedTextField(
                            value = claimPatientId,
                            onValueChange = {
                                claimPatientId = it
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            },
                            label = {
                                Text("Patient ID")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = claimPhoneNumber,
                            onValueChange = {
                                claimPhoneNumber = it
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            },
                            label = {
                                Text("Phone Number")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = claimPassword,
                            onValueChange = {
                                claimPassword = it
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            },
                            label = {
                                Text("New Password")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = claimConfirmPassword,
                            onValueChange = {
                                claimConfirmPassword = it
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            },
                            label = {
                                Text("Confirm Password")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )

                        if (patientUiState.claimError != null) {
                            Text(
                                text = patientUiState.claimError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                patientViewModel.claimAccount(
                                    patientId = claimPatientId,
                                    phoneNumber = claimPhoneNumber,
                                    newPassword = claimPassword,
                                    confirmPassword = claimConfirmPassword
                                )
                            },
                            enabled = !patientUiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            if (patientUiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Text("Claim Account")
                            }
                        }

                        TextButton(
                            onClick = {
                                isClaimMode = false
                                claimPatientId = ""
                                claimPhoneNumber = ""
                                claimPassword = ""
                                claimConfirmPassword = ""
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            }
                        ) {
                            Text("Back to Login")
                        }
                    } else {
                        OutlinedTextField(
                            value = patientId,
                            onValueChange = {
                                patientId = it
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            },
                            label = {
                                Text("Patient ID")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            },
                            label = {
                                Text("Password")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )

                        if (patientUiState.loginError != null) {
                            Text(
                                text = patientUiState.loginError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        if (patientUiState.claimMessage != null) {
                            Text(
                                text = patientUiState.claimMessage!!,
                                color = colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                patientViewModel.login(
                                    patientId = patientId,
                                    password = password
                                )
                            },
                            enabled = !patientUiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            if (patientUiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Text("Login")
                            }
                        }

                        TextButton(
                            onClick = {
                                isClaimMode = true
                                patientId = ""
                                password = ""
                                claimPatientId = ""
                                claimPhoneNumber = ""
                                claimPassword = ""
                                claimConfirmPassword = ""
                                patientViewModel.clearErrors()
                                patientViewModel.clearClaimMessage()
                            }
                        ) {
                            Text("Claim existing account")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )

                        TextButton(
                            onClick = {
                                onSignUpClick()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Sign Up")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroIcon(
    backgroundColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.medtrack_logo_hero),
            contentDescription = "MedTrack Logo",
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
        )
    }
}
