package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Data model representing an Admin subscription plan.
 */
data class AdminPlanItem(
    val id: String,
    val duration: String,
    val price: String,
    val priceUgx: Int
)

val DefaultAdminPlans = listOf(
    AdminPlanItem(
        id = "plan_1_month",
        duration = "1 Month",
        price = "UGX 2,000",
        priceUgx = 2000
    )
)

/**
 * Helper to format raw integer amount into "UGX X,XXX" standard string.
 */
fun formatUgxPrice(amountUgx: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "UGX ${formatter.format(amountUgx)}"
}

/**
 * Helper to parse price input string to integer value.
 */
fun parseUgxInput(raw: String): Int {
    val digitsOnly = raw.replace(Regex("[^0-9]"), "")
    return digitsOnly.toIntOrNull() ?: 0
}

/**
 * Admin Plans Screen for the Catch Up app.
 *
 * Reached by tapping the "Plans" button on Admin Home.
 * Allows viewing, editing, deleting, and adding subscription plans.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlansScreen(
    modifier: Modifier = Modifier,
    initialPlans: List<AdminPlanItem> = DefaultAdminPlans,
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var plans by remember { mutableStateOf(initialPlans) }

    // State for Edit / Add Dialog or BottomSheet
    var isSheetOpen by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<AdminPlanItem?>(null) } // null means adding a new plan
    var inputDuration by remember { mutableStateOf("") }
    var inputPrice by remember { mutableStateOf("") }

    // State for Delete confirmation dialog
    var planToDelete by remember { mutableStateOf<AdminPlanItem?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_plans_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // TOP BAR
            // Solid purple header (#4B3F8F) with rounded bottom corners (28dp)
            // Back arrow on left navigating to Admin Home, centered title "Plans"
            // ==========================================
            Box(
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
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .testTag("admin_plans_header")
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("admin_plans_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Plans",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("admin_plans_title")
                )
            }

            // ==========================================
            // BODY (Scrollable)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Subscription Plans",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.testTag("admin_plans_section_label")
                    )

                    Text(
                        text = "${plans.size} total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION — Plan List (10-12dp spacing)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_plans_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    plans.forEach { plan ->
                        AdminPlanOptionCard(
                            plan = plan,
                            onEdit = {
                                editingPlan = plan
                                inputDuration = plan.duration
                                inputPrice = plan.priceUgx.toString()
                                isSheetOpen = true
                            },
                            onDelete = {
                                planToDelete = plan
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // ADD NEW PLAN BUTTON (Dashed border style)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBFAFD))
                        .drawBehind {
                            drawRoundRect(
                                color = Color(0xFFC9C4DE),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                )
                            )
                        }
                        .clickable {
                            editingPlan = null
                            inputDuration = ""
                            inputPrice = ""
                            isSheetOpen = true
                        }
                        .testTag("admin_add_plan_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Plan",
                            tint = IndigoPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Plan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // ==========================================
    // EDIT / ADD BOTTOM SHEET
    // ==========================================
    if (isSheetOpen) {
        val isEditing = editingPlan != null
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("admin_plan_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Plan" else "Add New Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.testTag("admin_plan_sheet_title")
                    )

                    TextButton(onClick = { isSheetOpen = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }

                // Duration Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Duration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = inputDuration,
                        onValueChange = { inputDuration = it },
                        placeholder = { Text("e.g. 1 Month, 1 Term, 6 Months") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_plan_duration_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )
                }

                // Price Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Price (UGX)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = inputPrice,
                        onValueChange = { inputPrice = it },
                        placeholder = { Text("e.g. 1000, 3000, 10000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_plan_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPurple,
                            focusedLabelColor = IndigoPurple
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Button (Save / Add)
                Button(
                    onClick = {
                        val duration = inputDuration.trim().ifEmpty { if (isEditing) editingPlan?.duration ?: "1 Month" else "New Plan" }
                        val priceNum = parseUgxInput(inputPrice).coerceAtLeast(0)
                        val formattedPrice = formatUgxPrice(priceNum)

                        if (isEditing && editingPlan != null) {
                            // Update existing plan
                            val currentId = editingPlan!!.id
                            plans = plans.map { p ->
                                if (p.id == currentId) {
                                    p.copy(duration = duration, price = formattedPrice, priceUgx = priceNum)
                                } else p
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Updated plan: $duration ($formattedPrice)")
                            }
                        } else {
                            // Add new plan
                            val newPlan = AdminPlanItem(
                                id = "plan_${System.currentTimeMillis()}",
                                duration = duration,
                                price = formattedPrice,
                                priceUgx = priceNum
                            )
                            plans = plans + newPlan
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added plan: $duration ($formattedPrice)")
                            }
                        }
                        isSheetOpen = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_plan_save_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                ) {
                    Text(
                        text = if (isEditing) "Save" else "Add",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // ==========================================
    // DELETE CONFIRMATION DIALOG
    // ==========================================
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = {
                Text(
                    text = "Delete Plan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Remove this plan (${plan.duration} — ${plan.price})? Students will no longer be able to select it.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        plans = plans.filterNot { it.id == plan.id }
                        planToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Removed plan: ${plan.duration}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_plan_delete_confirm_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { planToDelete = null },
                    modifier = Modifier.testTag("admin_plan_delete_cancel_button")
                ) {
                    Text("Cancel", color = TextMuted)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Single Plan Card for Admin Plans screen.
 *
 * Follows the style used on Student Account page's plan cards:
 * - Left accent bar (4dp width, IndigoPurple)
 * - Duration on left
 * - Price on right
 * - Far right: "⋮" more options button with "Edit" & "Delete" dropdown menu
 */
@Composable
private fun AdminPlanOptionCard(
    plan: AdminPlanItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.15f),
                spotColor = Color(0x15000000)
            )
            .border(1.5.dp, BorderGrey, RoundedCornerShape(16.dp))
            .testTag("admin_plan_card_${plan.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left vertical accent bar (4dp width, IndigoPurple)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IndigoPurple)
                    .testTag("admin_plan_accent_bar_${plan.id}")
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Duration on left
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = plan.duration,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.testTag("admin_plan_duration_${plan.id}")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Subscription Tier",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextMuted
                )
            }

            // Price on right
            Text(
                text = plan.price,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = IndigoPurple,
                textAlign = TextAlign.End,
                modifier = Modifier.testTag("admin_plan_price_${plan.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // More Options ("⋮") Icon Button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("admin_plan_more_button_${plan.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More Options",
                        tint = IconGrey,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    // Edit Option
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit",
                                    tint = TextDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Edit", fontSize = 14.sp, color = TextDark)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        modifier = Modifier.testTag("admin_plan_menu_edit_${plan.id}")
                    )

                    // Delete Option
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Delete", fontSize = 14.sp, color = Color(0xFFE53935))
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        modifier = Modifier.testTag("admin_plan_menu_delete_${plan.id}")
                    )
                }
            }
        }
    }
}
