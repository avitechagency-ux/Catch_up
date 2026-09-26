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
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val avatarColor: Color = BlueTile
)

/**
 * Default list of students requested by user:
 * - Ezekiel Joseph
 * - Nifemi Moses
 * - Sarah Seruwama
 * - Apollo kenh
 */
val DefaultStudentsList = listOf(
    AdminUserItem(
        id = "stu_1",
        name = "Ezekiel Joseph",
        role = UserRoleFilter.STUDENTS,
        detailTag = "Senior 4",
        email = "ezekiel.j@student.catchup.ug",
        status = "Active",
        planOrDepartment = "Termly Access",
        joinedDate = "Sept 2026",
        avatarColor = IndigoPurple
    ),
    AdminUserItem(
        id = "stu_2",
        name = "Nifemi Moses",
        role = UserRoleFilter.STUDENTS,
        detailTag = "Senior 3",
        email = "nifemi.m@student.catchup.ug",
        status = "Active",
        planOrDepartment = "Annual Pass",
        joinedDate = "Aug 2026",
        avatarColor = BlueTile
    ),
    AdminUserItem(
        id = "stu_3",
        name = "Sarah Seruwama",
        role = UserRoleFilter.STUDENTS,
        detailTag = "Senior 4",
        email = "sarah.s@student.catchup.ug",
        status = "Active",
        planOrDepartment = "Termly Access",
        joinedDate = "Sept 2026",
        avatarColor = CoralTile
    ),
    AdminUserItem(
        id = "stu_4",
        name = "Apollo kenh",
        role = UserRoleFilter.STUDENTS,
        detailTag = "Senior 2",
        email = "apollo.k@student.catchup.ug",
        status = "Active",
        planOrDepartment = "Monthly Pass",
        joinedDate = "Aug 2026",
        avatarColor = TealTile
    )
)

/**
 * Default list of teachers to populate when the "Teachers" filter tab is selected.
 */
val DefaultTeachersList = listOf(
    AdminUserItem(
        id = "tch_1",
        name = "Tr. Okello Peter",
        role = UserRoleFilter.TEACHERS,
        detailTag = "Mathematics & Physics",
        email = "okello.p@catchup.ug",
        status = "Active",
        planOrDepartment = "Senior 1–4 Dept",
        joinedDate = "Jan 2026",
        avatarColor = NavyTile
    ),
    AdminUserItem(
        id = "tch_2",
        name = "Tr. Nalubega Grace",
        role = UserRoleFilter.TEACHERS,
        detailTag = "Biology & Health Sciences",
        email = "nalubega.g@catchup.ug",
        status = "Active",
        planOrDepartment = "Sciences Dept",
        joinedDate = "Feb 2026",
        avatarColor = PurpleTile
    ),
    AdminUserItem(
        id = "tch_3",
        name = "Tr. Mukasa David",
        role = UserRoleFilter.TEACHERS,
        detailTag = "Chemistry",
        email = "mukasa.d@catchup.ug",
        status = "Active",
        planOrDepartment = "Sciences Dept",
        joinedDate = "Mar 2026",
        avatarColor = PeachOrange
    ),
    AdminUserItem(
        id = "tch_4",
        name = "Tr. Akello Beatrice",
        role = UserRoleFilter.TEACHERS,
        detailTag = "Geography & Social Studies",
        email = "akello.b@catchup.ug",
        status = "Active",
        planOrDepartment = "Humanities Dept",
        joinedDate = "May 2026",
        avatarColor = TealTile
    )
)

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
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(UserRoleFilter.STUDENTS) }

    // Active dataset based on selected pill tab
    val currentRoleList = when (selectedTab) {
        UserRoleFilter.STUDENTS -> students
        UserRoleFilter.TEACHERS -> teachers
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
                    it.email.lowercase().contains(query)
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
                    count = students.size,
                    isSelected = selectedTab == UserRoleFilter.STUDENTS,
                    testTag = "admin_filter_pill_students",
                    onClick = { selectedTab = UserRoleFilter.STUDENTS },
                    modifier = Modifier.weight(1f)
                )

                // Teachers pill
                FilterPillButton(
                    label = "Teachers",
                    count = teachers.size,
                    isSelected = selectedTab == UserRoleFilter.TEACHERS,
                    testTag = "admin_filter_pill_teachers",
                    onClick = { selectedTab = UserRoleFilter.TEACHERS },
                    modifier = Modifier.weight(1f)
                )
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
                            modifier = Modifier.testTag("admin_user_card_${user.id}")
                        )
                    }
                }
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
                // Dummy Avatar with crisp white container and bold Indigo initials
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // User Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Active status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.20f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Detail tag (e.g. Senior 4 or Mathematics & Physics)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user.role == UserRoleFilter.STUDENTS) Icons.Outlined.School else Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.detailTag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightLavender
                        )
                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                        Text(
                            text = user.planOrDepartment,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Contact email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.email,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
