package com.example.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.UserRole
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.LoadingStateView
import com.example.ui.navigation.EduDestinations
import com.example.ui.navigation.Routes
import com.example.ui.theme.Blue600
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Teal500
import com.example.ui.theme.Violet400
import com.example.ui.theme.Violet600
import com.example.ui.viewmodel.AcademicUiState
import kotlinx.coroutines.launch

@Composable
fun EduMetricsShell(
    uiState: AcademicUiState,
    currentRoute: String,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onMarkAllRead: () -> Unit,
    onRetryLoad: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var sidebarCollapsed by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                DeepNavySidebar(
                    currentRoute = currentRoute,
                    collapsed = sidebarCollapsed,
                    demoStudentCount = uiState.students.count { it.isDemo },
                    onNavigate = onNavigate,
                    onToggleCollapse = { sidebarCollapsed = !sidebarCollapsed },
                    modifier = Modifier
                        .width(if (sidebarCollapsed) 78.dp else 248.dp)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                )

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        EduTopBar(
                            uiState = uiState,
                            currentRoute = currentRoute,
                            showMenuButton = false,
                            onMenuClick = {},
                            onSearchChange = onSearchChange,
                            onToggleTheme = onToggleTheme,
                            onSelectRole = onSelectRole,
                            onMarkAllRead = onMarkAllRead,
                            onNavigate = onNavigate
                        )
                    }
                ) { innerPadding ->
                    ShellBody(
                        uiState = uiState,
                        onRetryLoad = onRetryLoad,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        content = content
                    )
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Navy950,
                        modifier = Modifier.width(272.dp)
                    ) {
                        DeepNavySidebar(
                            currentRoute = currentRoute,
                            collapsed = false,
                            demoStudentCount = uiState.students.count { it.isDemo },
                            onNavigate = { route ->
                                scope.launch { drawerState.close() }
                                onNavigate(route)
                            },
                            onToggleCollapse = { scope.launch { drawerState.close() } },
                            isDrawerMode = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            ) {
                val quickDestinations = remember {
                    listOf(
                        EduDestinations[0], // Dashboard
                        EduDestinations[1], // Students
                        EduDestinations[3], // Marks
                        EduDestinations[5]  // Analytics
                    )
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        EduTopBar(
                            uiState = uiState,
                            currentRoute = currentRoute,
                            showMenuButton = true,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSearchChange = onSearchChange,
                            onToggleTheme = onToggleTheme,
                            onSelectRole = onSelectRole,
                            onMarkAllRead = onMarkAllRead,
                            onNavigate = onNavigate
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            quickDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onNavigate(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    ShellBody(
                        uiState = uiState,
                        onRetryLoad = onRetryLoad,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        content = content
                    )
                }
            }
        }
    }
}

@Composable
private fun ShellBody(
    uiState: AcademicUiState,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> LoadingStateView()
                uiState.error != null -> Box(modifier = Modifier.padding(16.dp)) {
                    ErrorStateBanner(message = uiState.error, onRetry = onRetryLoad)
                }
                else -> content()
            }
        }
    }
}

@Composable
private fun DeepNavySidebar(
    currentRoute: String,
    collapsed: Boolean,
    demoStudentCount: Int,
    onNavigate: (String) -> Unit,
    onToggleCollapse: () -> Unit,
    isDrawerMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Navy950)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        // Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Violet600, Blue600, Teal500))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "EduMetrics logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (!collapsed) {
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "EduMetrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Academic Intelligence",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Navy800)
        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EduDestinations.forEach { dest ->
                val selected = currentRoute == dest.route
                val itemModifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (selected) {
                            Modifier.background(Brush.horizontalGradient(listOf(Violet600, Blue600)))
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onNavigate(dest.route) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag("nav_${dest.route}")

                Row(
                    modifier = itemModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.Start
                ) {
                    Icon(
                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.label,
                        tint = if (selected) Color.White else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                    if (!collapsed) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else Color(0xFFCBD5E1),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (!collapsed && demoStudentCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Navy900,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Navy800, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Storage,
                            contentDescription = "Demo Dataset",
                            tint = Violet400,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Demo Dataset Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Violet400
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$demoStudentCount demo students loaded. Manage in Settings.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
            }
        }

        if (!isDrawerMode) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleCollapse)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (collapsed) {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    },
                    contentDescription = "Toggle Sidebar",
                    tint = Slate400
                )
                if (!collapsed) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Collapse",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400
                    )
                }
            }
        }
    }
}

@Composable
private fun EduTopBar(
    uiState: AcademicUiState,
    currentRoute: String,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onMarkAllRead: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val currentTitle = EduDestinations.find { it.route == currentRoute }?.label ?: "EduMetrics"
    var searchOpen by remember { mutableStateOf(false) }
    var notifMenuOpen by remember { mutableStateOf(false) }
    var profileMenuOpen by remember { mutableStateOf(false) }
    val unreadCount = uiState.notifications.count { !it.isRead }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showMenuButton) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open navigation menu")
                        }
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${uiState.settings.institutionName} · AY ${uiState.settings.academicYear}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { searchOpen = !searchOpen },
                        modifier = Modifier.testTag("topbar_search_button")
                    ) {
                        Icon(
                            imageVector = if (searchOpen) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "Search students"
                        )
                    }

                    // Notifications Menu
                    Box {
                        IconButton(
                            onClick = { notifMenuOpen = true },
                            modifier = Modifier.testTag("topbar_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = Rose500) {
                                            Text(unreadCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Academic notifications")
                            }
                        }

                        DropdownMenu(
                            expanded = notifMenuOpen,
                            onDismissRequest = { notifMenuOpen = false },
                            modifier = Modifier.widthIn(min = 260.dp, max = 320.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Academic Alerts (${uiState.notifications.size})",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                if (unreadCount > 0) {
                                    TextButton(onClick = onMarkAllRead) {
                                        Text("Mark read", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            HorizontalDivider()
                            if (uiState.notifications.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("All metrics are within healthy thresholds.") },
                                    onClick = { notifMenuOpen = false }
                                )
                            } else {
                                uiState.notifications.forEach { notif ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = notif.title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = notif.message,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            notifMenuOpen = false
                                            onNavigate(notif.targetRoute)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("topbar_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.settings.darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle light or dark theme"
                        )
                    }

                    // Profile Menu
                    Box {
                        val initials = uiState.settings.activeUserName
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.first().uppercase() }
                            .ifBlank { "EM" }

                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp, end = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Violet600, Blue600)))
                                .clickable { profileMenuOpen = true }
                                .testTag("topbar_profile_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = profileMenuOpen,
                            onDismissRequest = { profileMenuOpen = false }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Text(
                                    text = uiState.settings.activeUserName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = uiState.settings.activeUserRole.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider()
                            UserRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    text = { Text("View as ${role.label}") },
                                    onClick = {
                                        onSelectRole(role)
                                        profileMenuOpen = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                                text = { Text("AI Insights") },
                                onClick = {
                                    profileMenuOpen = false
                                    onNavigate(Routes.AI_INSIGHTS)
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                text = { Text("Settings") },
                                onClick = {
                                    profileMenuOpen = false
                                    onNavigate(Routes.SETTINGS)
                                }
                            )
                        }
                    }
                }
            }

            // Expandable Global Search Bar
            AnimatedVisibility(visible = searchOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.globalSearch,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search students by name, roll no, or department...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.globalSearch.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.globalSearch.isNotBlank()) {
                        val quickMatches = uiState.students.filter {
                            it.fullName.contains(uiState.globalSearch, ignoreCase = true) ||
                                it.rollNumber.contains(uiState.globalSearch, ignoreCase = true) ||
                                it.department.displayName.contains(uiState.globalSearch, ignoreCase = true)
                        }.take(4)

                        if (quickMatches.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    quickMatches.forEach { stu ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    onSearchChange(stu.fullName)
                                                    searchOpen = false
                                                    onNavigate(Routes.STUDENTS)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = stu.fullName,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                                Text(
                                                    text = "${stu.rollNumber} · ${stu.department.displayName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "Sem ${stu.currentSemester}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
