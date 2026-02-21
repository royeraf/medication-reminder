package com.medicationreminder.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationreminder.R
import com.medicationreminder.data.LanguageSetting
import com.medicationreminder.data.ThemeSetting
import com.medicationreminder.presentation.theme.MedicationReminderTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeSetting by viewModel.themeSetting.collectAsState()
    val languageSetting by viewModel.languageSetting.collectAsState()
    
    SettingsScreenContent(
        themeSetting = themeSetting,
        languageSetting = languageSetting,
        onThemeChanged = viewModel::setThemeSetting,
        onLanguageChanged = viewModel::setLanguageSetting
    )
}

@Composable
internal fun SettingsScreenContent(
    themeSetting: ThemeSetting,
    languageSetting: LanguageSetting,
    onThemeChanged: (ThemeSetting) -> Unit,
    onLanguageChanged: (LanguageSetting) -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = languageSetting,
            onLanguageSelected = {
                onLanguageChanged(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingsSectionTitle(stringResource(R.string.settings_notifications_section))
        }

        item {
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.settings_push_notifications),
                    subtitle = stringResource(R.string.settings_push_notifications_desc),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                SettingsToggleItem(
                    icon = Icons.Rounded.VolumeUp,
                    title = stringResource(R.string.settings_sound),
                    subtitle = stringResource(R.string.settings_sound_desc),
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it },
                    enabled = notificationsEnabled
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                SettingsToggleItem(
                    icon = Icons.Rounded.Vibration,
                    title = stringResource(R.string.settings_vibration),
                    subtitle = stringResource(R.string.settings_vibration_desc),
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it },
                    enabled = notificationsEnabled
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsSectionTitle(stringResource(R.string.settings_appearance_section))
        }

        item {
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Rounded.DarkMode,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_dark_mode_desc),
                    checked = themeSetting == ThemeSetting.DARK,
                    onCheckedChange = { isDark ->
                        onThemeChanged(if (isDark) ThemeSetting.DARK else ThemeSetting.LIGHT)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                SettingsToggleItem(
                    icon = Icons.Rounded.SettingsBrightness,
                    title = stringResource(R.string.settings_follow_system),
                    subtitle = stringResource(R.string.settings_follow_system_desc),
                    checked = themeSetting == ThemeSetting.SYSTEM,
                    onCheckedChange = { useSystem ->
                        if (useSystem) onThemeChanged(ThemeSetting.SYSTEM)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                SettingsClickItem(
                    icon = Icons.Rounded.Translate,
                    title = stringResource(R.string.settings_language),
                    subtitle = when (languageSetting) {
                        LanguageSetting.SYSTEM -> stringResource(R.string.settings_follow_system)
                        LanguageSetting.ENGLISH -> stringResource(R.string.settings_language_en)
                        LanguageSetting.SPANISH -> stringResource(R.string.settings_language_es)
                    },
                    onClick = { showLanguageDialog = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsSectionTitle(stringResource(R.string.settings_about_section))
        }

        item {
            SettingsCard {
                SettingsClickItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = "1.0.0",
                    onClick = {}
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                SettingsClickItem(
                    icon = Icons.Rounded.PrivacyTip,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_privacy_policy_desc),
                    onClick = {}
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                SettingsClickItem(
                    icon = Icons.Rounded.LocalHospital,
                    title = stringResource(R.string.settings_data_source),
                    subtitle = stringResource(R.string.settings_data_source_desc),
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: LanguageSetting,
    onLanguageSelected: (LanguageSetting) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LanguageOption(
                    title = stringResource(R.string.settings_follow_system),
                    selected = currentLanguage == LanguageSetting.SYSTEM,
                    onClick = { onLanguageSelected(LanguageSetting.SYSTEM) }
                )
                LanguageOption(
                    title = stringResource(R.string.settings_language_en),
                    selected = currentLanguage == LanguageSetting.ENGLISH,
                    onClick = { onLanguageSelected(LanguageSetting.ENGLISH) }
                )
                LanguageOption(
                    title = stringResource(R.string.settings_language_es),
                    selected = currentLanguage == LanguageSetting.SPANISH,
                    onClick = { onLanguageSelected(LanguageSetting.SPANISH) }
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = { Column(content = content) }
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (enabled) 1f else 0.4f
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MedicationReminderTheme {
        SettingsScreenContent(
            themeSetting = ThemeSetting.SYSTEM,
            languageSetting = LanguageSetting.ENGLISH,
            onThemeChanged = {},
            onLanguageChanged = {}
        )
    }
}
