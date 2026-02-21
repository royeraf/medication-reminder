package com.medicationreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.res.Resources
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.medicationreminder.data.LanguageSetting
import com.medicationreminder.data.ThemeSetting
import com.medicationreminder.presentation.navigation.AppNavigation
import com.medicationreminder.presentation.screens.settings.SettingsViewModel
import com.medicationreminder.presentation.theme.MedicationReminderTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeSetting by settingsViewModel.themeSetting.collectAsState()
            val languageSetting by settingsViewModel.languageSetting.collectAsState()

            // Handle language change
            val context = LocalContext.current
            LaunchedEffect(languageSetting) {
                val locale = when (languageSetting) {
                    LanguageSetting.SYSTEM -> {
                        // Read the true device locale from system resources, NOT Locale.getDefault()
                        // which reflects whatever setDefault() last set (the in-app override).
                        val systemConfig = Resources.getSystem().configuration
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            systemConfig.locales[0]
                        } else {
                            @Suppress("DEPRECATION")
                            systemConfig.locale
                        }
                    }
                    else -> Locale(languageSetting.code)
                }

                val config = context.resources.configuration
                if (config.locales[0].language != locale.language) {
                    Locale.setDefault(locale)
                    config.setLocale(locale)
                    context.resources.updateConfiguration(config, context.resources.displayMetrics)

                    // Re-trigger recomposition by finding activity and recreating
                    (context as? MainActivity)?.recreate()
                }
            }

            val darkTheme = when (themeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            MedicationReminderTheme(darkTheme = darkTheme) {
                var showPermissionDialog by remember { mutableStateOf(false) }
                var showExactAlarmDialog by remember { mutableStateOf(false) }

                // Check notification permission on startup (Android 13+)
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            showPermissionDialog = true
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(AlarmManager::class.java)
                        if (!alarmManager.canScheduleExactAlarms()) {
                            showExactAlarmDialog = true
                        }
                    }
                }

                if (showPermissionDialog) {
                    NotificationPermissionDialog(
                        onDismiss = { showPermissionDialog = false },
                        onGoToSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
                            showPermissionDialog = false
                        }
                    )
                }

                if (showExactAlarmDialog) {
                    ExactAlarmPermissionDialog(
                        onDismiss = { showExactAlarmDialog = false },
                        onGoToSettings = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                            }
                            showExactAlarmDialog = false
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}

@Composable
fun ExactAlarmPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Allow Exact Alarms",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To deliver reminders at exactly the right time, please grant the \"Alarms & reminders\" permission in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onGoToSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Settings")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Not now")
                }
            }
        }
    }
}

@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onDismiss()
            }
        }
    )

    Dialog(
        onDismissRequest = { }, // Force user to interact
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "To ensure you never miss a dose, please enable notifications. This is crucial for your medication schedule.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Allow Notifications")
                }
                
                TextButton(
                    onClick = onGoToSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open App Settings")
                }
            }
        }
    }
}
