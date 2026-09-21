package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TrackPurple
import kotlinx.coroutines.launch

/**
 * Settings Screen for the Catch Up app.
 * Shared structure for both Student and Teacher panels (same screen, no role-specific differences).
 *
 * Requirements:
 * - HEADER CARD:
 *   - Styled like Subjects page header card in student panel (Solid deep indigo-purple #4B3F8F, rounded bottom corners 28dp)
 *   - Left: back arrow icon (24dp, White), navigates back to Profile
 *   - Bold "Settings" header text (24sp, Bold, Color.White)
 * - BODY (scrollable, 20dp left/right padding, 20dp top padding, 28dp spacing between section groups):
 *   - SECTION 1 — Notifications:
 *     - Label: "Notifications" (13sp, Medium, #8A8A8A)
 *     - Grouped card: #F7F6FB, corner radius 16dp, 1dp divider (#ECEAF5) between rows
 *     - Rows: "New note alerts", "Streak reminders", "General announcements"
 *   - SECTION 2 — Appearance:
 *     - Label: "Appearance" (13sp, Medium, #8A8A8A)
 *     - Grouped card: same style
 *     - Rows: "Language" (English)
 *   - SECTION 3 — Account Preferences:
 *     - Label: "Account Preferences" (13sp, Medium, #8A8A8A)
 *     - Grouped card: same style
 *     - Rows: "Download over Wi-Fi only" (toggle), "Clear cached notes" (destructive coral text button with confirm dialog)
 * - ROW STYLE (shared across all sections):
 *   - Height 56dp, 16dp horizontal padding
 *   - Label text: 14sp, Regular, #1A1A1A, left-aligned
 *   - Right-side control: right-aligned
 *   - Toggle switch: thumb white, track #4B3F8F when on, track #E5E2F2 when off
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onClearCachedNotes: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Notification Toggles
    var newNoteAlerts by remember { mutableStateOf(true) }
    var streakReminders by remember { mutableStateOf(true) }
    var generalAnnouncements by remember { mutableStateOf(false) }

    // Appearance
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageSelector by remember { mutableStateOf(false) }

    // Account Preferences
    var wifiOnlyDownload by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .navigationBarsPadding()
        ) {
            // ======================================
            // HEADER CARD
            // Styled exactly like Subjects page header card in student panel:
            // - Solid deep indigo-purple #4B3F8F
            // - Rounded bottom corners: bottomStart = 28dp, bottomEnd = 28dp
            // - statusBarsPadding() with generous internal padding
            // - Left back arrow to return to profile
            // - Bold "Settings" header text (24sp, Bold, Color.White)
            // ======================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
                    .testTag("settings_header_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("settings_title")
                    )
                }
            }

            // ======================================
            // BODY (scrollable, 20dp left/right padding, 20dp top padding, 28dp spacing between section groups)
            // ======================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // ======================================
                // SECTION 1 — Notifications
                // ======================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_section_notifications")
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_notifications")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row 1: New note alerts
                            SettingsSwitchRow(
                                label = "New note alerts",
                                checked = newNoteAlerts,
                                onCheckedChange = { newNoteAlerts = it },
                                testTag = "setting_new_note_alerts"
                            )

                            SettingsDivider()

                            // Row 2: Streak reminders
                            SettingsSwitchRow(
                                label = "Streak reminders",
                                checked = streakReminders,
                                onCheckedChange = { streakReminders = it },
                                testTag = "setting_streak_reminders"
                            )

                            SettingsDivider()

                            // Row 3: General announcements
                            SettingsSwitchRow(
                                label = "General announcements",
                                checked = generalAnnouncements,
                                onCheckedChange = { generalAnnouncements = it },
                                testTag = "setting_general_announcements"
                            )
                        }
                    }
                }

                // ======================================
                // SECTION 2 — Appearance
                // ======================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_section_appearance")
                ) {
                    Text(
                        text = "Appearance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_appearance")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Language
                            SettingsActionRow(
                                label = "Language",
                                value = selectedLanguage,
                                onClick = { showLanguageSelector = true },
                                testTag = "setting_language_row"
                            )
                        }
                    }
                }

                // ======================================
                // SECTION 3 — Account Preferences
                // ======================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_section_account_preferences")
                ) {
                    Text(
                        text = "Account Preferences",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.testTag("section_label_account_preferences")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftLavenderGrey
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row 1: Download over Wi-Fi only
                            SettingsSwitchRow(
                                label = "Download over Wi-Fi only",
                                checked = wifiOnlyDownload,
                                onCheckedChange = { wifiOnlyDownload = it },
                                testTag = "setting_wifi_only"
                            )

                            SettingsDivider()

                            // Row 2: Clear cached notes (coral destructive action)
                            SettingsDestructiveRow(
                                label = "Clear cached notes",
                                buttonText = "Clear",
                                onClick = { showClearCacheDialog = true },
                                testTag = "setting_clear_cached_notes"
                            )
                        }
                    }
                }
            }
        }
    }

    // ======================================
    // LANGUAGE SELECTOR BOTTOM SHEET
    // ======================================
    if (showLanguageSelector) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSelector = false },
            sheetState = languageSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("language_selector_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Select Language",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf("English", "Luganda (Coming Soon)", "Swahili (Coming Soon)").forEach { langOption ->
                    val isEnglish = langOption == "English"
                    val isSelected = selectedLanguage == "English" && isEnglish
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = isEnglish) {
                                selectedLanguage = langOption
                                showLanguageSelector = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Language set to $langOption")
                                }
                            }
                            .padding(vertical = 14.dp, horizontal = 12.dp)
                            .testTag("language_option_${langOption.split(" ").first()}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = langOption,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                isSelected -> IndigoPurple
                                !isEnglish -> TextMuted
                                else -> TextDark
                            }
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = IndigoPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ======================================
    // CLEAR CACHE CONFIRMATION DIALOG
    // ======================================
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Clear Cached Notes?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "This will remove all offline cached notes from local storage. You can re-download them at any time.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        onClearCachedNotes()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Cached notes cleared successfully.")
                        }
                    },
                    modifier = Modifier.testTag("confirm_clear_cache_button")
                ) {
                    Text(
                        text = "Clear",
                        fontWeight = FontWeight.Bold,
                        color = CoralTile
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCacheDialog = false },
                    modifier = Modifier.testTag("cancel_clear_cache_button")
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )
                }
            },
            modifier = Modifier.testTag("clear_cache_confirmation_dialog")
        )
    }
}

/**
 * 1dp divider (#ECEAF5) between card rows.
 */
@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFECEAF5))
    )
}

/**
 * Shared Settings Switch Row:
 * - Height 56dp, 16dp horizontal padding
 * - Label text: 14sp, weight Regular, color #1A1A1A, left-aligned
 * - Toggle switch: thumb white, track #4B3F8F when on, track #E5E2F2 when off
 */
@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IndigoPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TrackPurple, // #E5E2F2
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            ),
            modifier = Modifier.testTag("${testTag}_switch")
        )
    }
}

/**
 * Shared Settings Action Row (Value + Chevron):
 * - Height 56dp, 16dp horizontal padding
 * - Label text: 14sp, Regular, #1A1A1A, left-aligned
 * - Value + Chevron-right icon: right-aligned
 */
@Composable
fun SettingsActionRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF8A8A8A)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF8A8A8A),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Shared Settings Destructive Row (Destructive text button):
 * - Height 56dp, 16dp horizontal padding
 * - Label text: 14sp, Regular, #1A1A1A, left-aligned
 * - Text button right: color #E76F51 (coral)
 */
@Composable
fun SettingsDestructiveRow(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = buttonText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = CoralTile, // #E76F51
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .testTag("${testTag}_button")
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
