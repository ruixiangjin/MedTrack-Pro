package com.medtrackpro.demo.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medtrackpro.demo.ui.screen.AddMedicationScreen
import com.medtrackpro.demo.ui.screen.ClinicianDashboardScreen
import com.medtrackpro.demo.ui.screen.ClinicianLoginScreen
import com.medtrackpro.demo.ui.screen.HomeScreen
import com.medtrackpro.demo.ui.screen.LoginScreen
import com.medtrackpro.demo.ui.screen.MedCoachScreen
import com.medtrackpro.demo.ui.screen.SettingsScreen
import com.medtrackpro.demo.ui.screen.SignUpScreen
import com.medtrackpro.demo.ui.screen.SymptomsScreen
import com.medtrackpro.demo.ui.screen.WelcomeScreen
import kotlinx.coroutines.launch

object MedTrackRoutes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val ADD_MEDICATION = "add_medication"
    const val SYMPTOMS = "symptoms"
    const val MEDCOACH = "medcoach"
    const val SETTINGS = "settings"
    const val CLINICIAN_LOGIN = "clinician_login"
    const val CLINICIAN_DASHBOARD = "clinician_dashboard"
}

@Composable
fun MedTrackNavHost(loggedInPatientId: String?) {
    val navController = rememberNavController()
    var currentPatientId by remember { mutableStateOf(loggedInPatientId) }

    // Shared snackbars
    val messageHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    fun navigateMainTab(route: String) {
        navController.navigate(route) {
            popUpTo(MedTrackRoutes.HOME) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    // Skip login when a session is still valid
    val startDestination = if (loggedInPatientId != null) {
        MedTrackRoutes.HOME
    } else {
        MedTrackRoutes.WELCOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = messageHostState)
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            composable(MedTrackRoutes.WELCOME) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    WelcomeScreen(
                        innerPadding = innerPadding,
                        onLoginClick = {
                            navController.navigate(MedTrackRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onSignUpClick = {
                            navController.navigate(MedTrackRoutes.SIGN_UP) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.LOGIN) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    LoginScreen(
                        innerPadding = innerPadding,
                        onLoginSuccess = { patientId ->
                            currentPatientId = patientId
                            navController.navigate(MedTrackRoutes.HOME) {
                                popUpTo(MedTrackRoutes.WELCOME) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onSignUpClick = {
                            navController.navigate(MedTrackRoutes.SIGN_UP) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.SIGN_UP) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    SignUpScreen(
                        innerPadding = innerPadding,
                        onAccountCreated = { patientId ->
                            navController.navigate(MedTrackRoutes.LOGIN) {
                                popUpTo(MedTrackRoutes.SIGN_UP) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }

                            coroutineScope.launch {
                                messageHostState.showSnackbar("Account created. Patient ID: $patientId")
                            }
                        },
                        onBackToLogin = {
                            navController.navigate(MedTrackRoutes.LOGIN) {
                                popUpTo(MedTrackRoutes.SIGN_UP) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.HOME) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = MedTrackRoutes.HOME,
                            onNavigate = { route ->
                                navigateMainTab(route)
                            }
                        )
                    }
                ) { innerPadding ->

                    HomeScreen(
                        innerPadding = innerPadding,
                        patientId = currentPatientId,
                        onAddMedicationClick = {
                            navController.navigate(MedTrackRoutes.ADD_MEDICATION) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.ADD_MEDICATION) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    AddMedicationScreen(
                        innerPadding = innerPadding,
                        patientId = currentPatientId,
                        onMedicationSaved = {
                            navController.navigate(MedTrackRoutes.HOME) {
                                popUpTo(MedTrackRoutes.HOME) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }

                            coroutineScope.launch {
                                messageHostState.showSnackbar("Medication saved")
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.SYMPTOMS) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = MedTrackRoutes.SYMPTOMS,
                            onNavigate = { route ->
                                navigateMainTab(route)
                            }
                        )
                    }
                ) { innerPadding ->
                    SymptomsScreen(
                        innerPadding = innerPadding,
                        patientId = currentPatientId,
                        messageHostState = messageHostState
                    )
                }
            }

            composable(MedTrackRoutes.MEDCOACH) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = MedTrackRoutes.MEDCOACH,
                            onNavigate = { route ->
                                navigateMainTab(route)
                            }
                        )
                    }
                ) { innerPadding ->
                    MedCoachScreen(
                        innerPadding = innerPadding,
                        patientId = currentPatientId
                    )
                }
            }

            composable(MedTrackRoutes.SETTINGS) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = MedTrackRoutes.SETTINGS,
                            onNavigate = { route ->
                                navigateMainTab(route)
                            }
                        )
                    }
                ) { innerPadding ->
                    SettingsScreen(
                        innerPadding = innerPadding,
                        patientId = currentPatientId,
                        onLogoutClick = {
                            currentPatientId = null
                            navController.navigate(MedTrackRoutes.WELCOME) {
                                popUpTo(MedTrackRoutes.HOME) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onClinicianLoginClick = {
                            navController.navigate(MedTrackRoutes.CLINICIAN_LOGIN) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.CLINICIAN_LOGIN) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ClinicianLoginScreen(
                        innerPadding = innerPadding,
                        onLoginSuccess = {
                            navController.navigate(MedTrackRoutes.CLINICIAN_DASHBOARD) {
                                popUpTo(MedTrackRoutes.CLINICIAN_LOGIN) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onBackClick = {
                            navController.navigate(MedTrackRoutes.SETTINGS) {
                                popUpTo(MedTrackRoutes.CLINICIAN_LOGIN) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(MedTrackRoutes.CLINICIAN_DASHBOARD) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ClinicianDashboardScreen(
                        innerPadding = innerPadding,
                        onBackToSettingsClick = {
                            navController.navigate(MedTrackRoutes.SETTINGS) {
                                popUpTo(MedTrackRoutes.CLINICIAN_DASHBOARD) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
