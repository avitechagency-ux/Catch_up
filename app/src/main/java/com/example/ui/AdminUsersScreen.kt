package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupabaseConfig
import com.example.data.SupabaseRepository
import com.example.data.SupabaseUserDto
import com.example.ui.theme.BlueTile
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CoralTile
import com.example.ui.theme.IconGrey
import com.example.ui.theme.IndigoPurple
import com.example.ui.theme.LightLavender
import com.example.ui.theme.NavyTile
import com.example.ui.theme.PeachOrange
import com.example.ui.theme.PurpleTile
import com.example.ui.theme.SoftLavenderGrey
import com.example.ui.theme.TealTile
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Filter tab categories for the Admin Users screen.
 */
enum class UserRoleFilter {
    STUDENTS,
    TEACHERS
}

/**
 * Data model for a user item on the Admin Users page.
 */
data class AdminUserItem(
    val id: String,
    val name: String,
    val role: UserRoleFilter,
    val detailTag: String, // e.g. "Senior 4" or "Physics & Math"
    val email: String,
    val status: String = "Active",
    val planOrDepartment: String = "Termly Plan",
    val joinedDate: String = "Sept 2026",
    val avatarColor: Color = BlueTile,
    val academicClass: String = "",
    val stream: String = "",
    val teacherIdCode: String = "",
    val subjectsTaught: String = "",
    val isPlanActive: Boolean = true,
    val planDuration: String = "1 Month",
    val rawDto: SupabaseUserDto? = null
)

/**
 * Production student list starts empty; populated directly from Supabase `public.users`.
 */
val DefaultStudentsList: List<AdminUserItem> = emptyList()

/**
 * Production teacher list starts empty; populated directly from Supabase `public.users`.
 */
val DefaultTeachersList: List<AdminUserItem> = emptyList()

/**
 * Converts a remote [SupabaseUserDto] from `public.users` into an [AdminUserItem]
 * for the Admin Panel Users screen.
 */
fun SupabaseUserDto.toAdminUserItem(): AdminUserItem? {
    val normalizedRole = role.trim().lowercase()
    val contactStr = phoneNumber.trim().ifBlank { email.trim() }
    return when (normalizedRole) {
        "teacher" -> {
            val code = teacherIdCode?.takeIf { it.isNotBlank() }
                ?: generateTeacherIdCode(fullName.ifBlank { contactStr })
            AdminUserItem(
                id = "usr_${contactStr.ifBlank { fullName.lowercase().replace(" ", "_") }}",
                name = fullName.trim().ifBlank { "Teacher" },
                role = UserRoleFilter.TEACHERS,
                detailTag = subjectsTaught.trim().ifBlank { "Secondary Curriculum" },
                email = contactStr,
                status = "Active",
                planOrDepartment = "ID: $code",
                joinedDate = createdAt?.take(10) ?: "Sept 2026",
                avatarColor = NavyTile,
                academicClass = academicClass,
                stream = stream,
                teacherIdCode = code,
                subjectsTaught = subjectsTaught,
                isPlanActive = isPlanActive,
                planDuration = planDuration,
                rawDto = this
            )
        }
        "student" -> {
            val combinedClassStream = listOf(academicClass.trim(), stream.trim())
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Student" }
            val resolvedPlan = planDuration.trim().ifBlank { "1 Month Plan" }
            AdminUserItem(
                id = "usr_${contactStr.ifBlank { fullName.lowercase().replace(" ", "_") }}",
                name = fullName.trim().ifBlank { "Student" },
                role = UserRoleFilter.STUDENTS,
                detailTag = combinedClassStream,
                email = contactStr,
                status = if (isPlanActive) "Active" else "Expired",
                planOrDepartment = resolvedPlan,
                joinedDate = createdAt?.take(10) ?: "Sept 2026",
                avatarColor = IndigoPurple,
                academicClass = academicClass,
                stream = stream,
                teacherIdCode = teacherIdCode.orEmpty(),
                subjectsTaught = subjectsTaught,
                isPlanActive = isPlanActive,
                planDuration = planDuration,
                rawDto = this
            )
        }
        else -> null
    }
}

/**
 * Merges newly signed-up users with the default users list for [targetRole], placing newly signed-up
 * users at the top and deduplicating by phone/email or normalized full name.
 */
fun mergeSignedUpAndDefaultUsers(
    signedUpUsers: List<AdminUserItem>,
    defaultUsers: List<AdminUserItem>,
    targetRole: UserRoleFilter
): List<AdminUserItem> {
    val roleSignups = signedUpUsers.filter { it.role == targetRole }
    val combined = roleSignups + defaultUsers
    val seenKeys = mutableSetOf<String>()
    return combined.filter { item ->
        val contactKey = item.email.trim().lowercase()
        val nameKey = item.name.trim().lowercase()
        val dedupeKey = if (contactKey.isNotBlank()) "contact:$contactKey" else "name:$nameKey"
        val isNew = seenKeys.add(dedupeKey) && seenKeys.add("name:$nameKey")
        isNew
    }
}

/**
 * Admin Users Screen
 *
 * Requirements:
 * 1. Header: A clean header at the top with the title "Users".
 * 2. Search Bar: Directly below the header, search input field with "Search..." placeholder,
 *    magnifying glass icon on right, slight border and rounded corners.
 * 3. Filter Tabs: Two pill-shaped buttons: "Students" (active by default) and "Teachers".
 * 4. User List: Rectangular cards with subtle shadow/border featuring:
 *    - Ezekiel Joseph
 *    - Nifemi Moses
 *    - Sarah Seruwama
 *    - Apollo kenh
 *    (along with realistic avatars, tags, contact info, and status indicators)
 * 5. Bottom Navigation: Reused AdminBottomNavBar with "Users" (index 2) as active tab.
 */
@Composable
fun AdminUsersScreen(
    modifier: Modifier = Modifier,
    students: List<AdminUserItem> = DefaultStudentsList,
    teachers: List<AdminUserItem> = DefaultTeachersList,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var remoteSupabaseUsers by remember { mutableStateOf<List<AdminUserItem>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(false) }
    var usersFetchError by remember { mutableStateOf<String?>(null) }

    val loadUsersFromSupabase: () -> Unit = {
        coroutineScope.launch {
            isLoadingUsers = true
            usersFetchError = null
            try {
                val fetchedDtos = SupabaseRepository.fetchAllUsers()
                remoteSupabaseUsers = fetchedDtos.mapNotNull { it.toAdminUserItem() }
            } catch (_: Exception) {
                usersFetchError = "Unable to sync latest users from Supabase."
            } finally {
                isLoadingUsers = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUsersFromSupabase()
    }

    val allStudents = remember(remoteSupabaseUsers, students) {
        mergeSignedUpAndDefaultUsers(
            signedUpUsers = remoteSupabaseUsers,
            defaultUsers = students,
            targetRole = UserRoleFilter.STUDENTS
        )
    }

    val allTeachers = remember(remoteSupabaseUsers, teachers) {
        mergeSignedUpAndDefaultUsers(
            signedUpUsers = remoteSupabaseUsers,
            defaultUsers = teachers,
            targetRole = UserRoleFilter.TEACHERS
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(UserRoleFilter.STUDENTS) }
    var selectedUser by remember { mutableStateOf<AdminUserItem?>(null) }

    // Active dataset based on selected pill tab
    val currentRoleList = when (selectedTab) {
        UserRoleFilter.STUDENTS -> allStudents
        UserRoleFilter.TEACHERS -> allTeachers
    }

    // Filter by search query
    val filteredUsers = remember(searchQuery, currentRoleList) {
        if (searchQuery.isBlank()) {
            currentRoleList
        } else {
            val query = searchQuery.trim().lowercase()
            currentRoleList.filter {
                it.name.lowercase().contains(query) ||
                    it.detailTag.lowercase().contains(query) ||
                    it.email.lowercase().contains(query) ||
                    it.planOrDepartment.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_users_screen"),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = 2,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToAnalysis()
                        2 -> onNavigateToUsers()
                        3 -> onNavigateToMore()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ==========================================
            // 1. HEADER
            // Purple background card with back button, "Users" title,
            // and embedded search bar just like students subjects page
            // ==========================================
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
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 22.dp)
                    .testTag("admin_users_header")
            ) {
                // Top Row: Back button on left, "Users" title centered
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("admin_users_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Users",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("admin_users_title")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar embedded inside purple header card (just like students subjects page)
                AdminSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_users_search_bar")
                )
            }

            // ==========================================
            // 2. FILTER TABS
            // Two pill-shaped buttons: "Students" (active by default) and "Teachers"
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                    .testTag("admin_users_filter_tabs"),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Students pill
                FilterPillButton(
                    label = "Students",
                    count = allStudents.size,
                    isSelected = selectedTab == UserRoleFilter.STUDENTS,
                    testTag = "admin_filter_pill_students",
                    onClick = { selectedTab = UserRoleFilter.STUDENTS },
                    modifier = Modifier.weight(1f)
                )

                // Teachers pill
                FilterPillButton(
                    label = "Teachers",
                    count = allTeachers.size,
                    isSelected = selectedTab == UserRoleFilter.TEACHERS,
                    testTag = "admin_filter_pill_teachers",
                    onClick = { selectedTab = UserRoleFilter.TEACHERS },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isLoadingUsers) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    color = IndigoPurple,
                    trackColor = LightLavender
                )
            }

            usersFetchError?.let { errorMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMsg,
                            fontSize = 12.sp,
                            color = Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = loadUsersFromSupabase) {
                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoPurple)
                        }
                    }
                }
            }

            // ==========================================
            // 4. USER LIST
            // Rectangular user cards with slight border or subtle shadow
            // ==========================================
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("admin_users_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = SoftLavenderGrey,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonSearch,
                                    contentDescription = "No users found",
                                    tint = IconGrey,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No users found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No ${selectedTab.name.lowercase()} match '$searchQuery'",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_users_list"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredUsers,
                        key = { it.id }
                    ) { user ->
                        AdminUserCard(
                            user = user,
                            onClick = { selectedUser = user },
                            modifier = Modifier.testTag("admin_user_card_${user.id}")
                        )
                    }
                }
            }

            selectedUser?.let { user ->
                AdminUserDetailDialog(
                    user = user,
                    onDismiss = { selectedUser = null },
                    onUserDeleted = {
                        loadUsersFromSupabase()
                        selectedUser = null
                    },
                    onPasswordReset = {
                        loadUsersFromSupabase()
                    }
                )
            }
        }
    }
}

/**
 * Search input field with placeholder "Search...", magnifying glass icon on the right side,
 * slight border and rounded corners.
 */
@Composable
fun AdminSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier
            .height(50.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x10000000),
                spotColor = Color(0x15000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFECEBF2),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search...",
                        fontSize = 14.sp,
                        color = IconGrey
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    ),
                    cursorBrush = SolidColor(IndigoPurple),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_text_input")
                )
            }

            // Magnifying glass search icon on the right side
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "Clear search",
                        tint = IconGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = IndigoPurple,
                modifier = Modifier
                    .size(22.dp)
                    .testTag("admin_search_icon")
            )
        }
    }
}

/**
 * Pill-shaped filter button fully filled with color and no awkward inner white box.
 */
@Composable
fun FilterPillButton(
    label: String,
    count: Int,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) IndigoPurple else Color(0xFFEDE9FE),
        label = "pillBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else IndigoPurple,
        label = "pillText"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) IndigoPurple else Color(0xFFD6D0F0),
        label = "pillBorder"
    )

    Surface(
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.25f),
                spotColor = Color(0x20000000)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label ($count)",
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Rectangular user card styled in solid purple gradient with white typography,
 * avatar badge with user initials, role/grade tags, and active status pill.
 */
@Composable
fun AdminUserCard(
    user: AdminUserItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Generate initials from name (e.g. "Ezekiel Joseph" -> "EJ")
    val initials = remember(user.name) {
        val parts = user.name.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "U"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = IndigoPurple.copy(alpha = 0.35f),
                spotColor = Color(0x25000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = IndigoPurple
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(IndigoPurple, Color(0xFF5A4EB0))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Avatar with crisp white container and bold Indigo initials
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Compact User Info Column: Name and Phone Number only
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user.email.contains("@")) Icons.Outlined.Mail else Icons.Outlined.PhoneAndroid,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.80f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.email,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserDetailDialog(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onUserDeleted: () -> Unit,
    onPasswordReset: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }
    var isResettingPassword by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var showPasswordInputBox by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var uploadedNotesCount by remember { mutableStateOf(0) }
    var connectedStudentsList by remember { mutableStateOf<List<ConnectedStudent>>(emptyList()) }
    var isLoadingTeacherStats by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) {
        if (user.role == UserRoleFilter.TEACHERS) {
            isLoadingTeacherStats = true
            try {
                val uploads = SupabaseRepository.fetchUploadsForTeacher(user.teacherIdCode, user.name)
                uploadedNotesCount = uploads.size
                val students = SupabaseRepository.fetchConnectedStudentsForTeacher(user.teacherIdCode)
                connectedStudentsList = students
            } catch (_: Exception) {}
            finally {
                isLoadingTeacherStats = false
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (user.role == UserRoleFilter.STUDENTS) "Student Account" else "Teacher Account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPurple
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = SoftLavenderGrey, shape = RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(label = "Contact / Phone", value = user.email)
                    DetailRow(label = "School Name", value = user.rawDto?.schoolName?.ifBlank { "Secondary School" } ?: "Secondary School")

                    if (user.role == UserRoleFilter.STUDENTS) {
                        DetailRow(label = "Class & Stream", value = user.detailTag.ifBlank { "Not Specified" })
                        DetailRow(label = "Plan Status", value = if (user.isPlanActive) "Active (${user.planDuration})" else "Expired")
                    } else {
                        DetailRow(label = "Teacher ID Code", value = user.teacherIdCode.ifBlank { "N/A" })
                        DetailRow(label = "Subjects Taught", value = user.subjectsTaught.ifBlank { "General Curriculum" })
                        DetailRow(label = "Uploaded Notes", value = if (isLoadingTeacherStats) "Loading..." else "$uploadedNotesCount notes")
                        DetailRow(label = "Connected Students", value = if (isLoadingTeacherStats) "Loading..." else "${connectedStudentsList.size} students connected")
                    }
                }

                statusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = msg, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = IndigoPurple)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (showPasswordInputBox) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = "Reset New Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        BasicTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderGrey, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showPasswordInputBox = false; newPasswordInput = "" }) {
                                Text("Cancel", fontSize = 12.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    if (newPasswordInput.isBlank()) {
                                        statusMessage = "Please enter a valid new password."
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        isResettingPassword = true
                                        statusMessage = "Updating password..."
                                        val targetPhoneOrEmail = user.rawDto?.phoneNumber?.ifBlank { user.email } ?: user.email
                                        val success = SupabaseRepository.resetUserPassword(targetPhoneOrEmail, newPasswordInput)
                                        isResettingPassword = false
                                        if (success) {
                                            statusMessage = "Password successfully reset!"
                                            showPasswordInputBox = false
                                            newPasswordInput = ""
                                            onPasswordReset()
                                        } else {
                                            statusMessage = "Failed to reset password. Please try again."
                                        }
                                    }
                                },
                                enabled = !isResettingPassword,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                            ) {
                                Text(if (isResettingPassword) "Saving..." else "Confirm Reset", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                } else if (showDeleteConfirm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = "Confirm Account Deletion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Are you sure you want to delete ${user.name}? This action cannot be undone.", fontSize = 12.sp, color = TextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("Cancel", fontSize = 12.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isDeleting = true
                                        statusMessage = "Deleting user account..."
                                        val targetPhoneOrEmail = user.rawDto?.phoneNumber?.ifBlank { user.email } ?: user.email
                                        val success = SupabaseRepository.deleteUserAccount(targetPhoneOrEmail)
                                        isDeleting = false
                                        if (success) {
                                            statusMessage = "Account deleted successfully."
                                            onUserDeleted()
                                        } else {
                                            statusMessage = "Failed to delete account. Please try again."
                                        }
                                    }
                                },
                                enabled = !isDeleting,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                            ) {
                                Text(if (isDeleting) "Deleting..." else "Yes, Delete", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showPasswordInputBox = true; statusMessage = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IndigoPurple)
                        }

                        androidx.compose.material3.Button(
                            onClick = { showDeleteConfirm = true; statusMessage = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                        ) {
                            Text("Delete Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
    }
}
