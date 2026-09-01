package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.AppMode
import com.example.viewmodel.HorrorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(viewModel: HorrorViewModel, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ورود امن ادمین عمارت وحشت", fontWeight = FontWeight.Bold, color = SpectralWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = SpectralWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepCrypt)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VoidBlack),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .border(1.dp, BloodCrimson.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(CryptCard, RoundedCornerShape(20.dp))
                            .border(1.dp, BloodGlow, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(38.dp))
                    }
                    Text(
                        text = "کنترل‌کده تاریکی",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = SpectralWhite)
                    )
                    Text(
                        text = "برای دسترسی به پنل مدیریت، ایمیل و رمز عبور ادمین Supabase را وارد کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedAsh,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorText = null },
                        label = { Text("ایمیل ادمین") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = MutedAsh.copy(alpha = 0.4f),
                            focusedLabelColor = BloodGlow,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorText = null },
                        label = { Text("رمز عبور") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MutedAsh
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = MutedAsh.copy(alpha = 0.4f),
                            focusedLabelColor = BloodGlow,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )

                    if (errorText != null) {
                        Surface(
                            color = BloodCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BloodGlow.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorText!!,
                                color = BloodGlow,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorText = "لطفاً ایمیل و رمز عبور را وارد نمایید."
                                return@Button
                            }
                            viewModel.loginAdmin(email, password) { success, msg ->
                                if (!success) {
                                    errorText = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SpectralWhite)
                        } else {
                            Text("ورود به پنل مدیریت", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: HorrorViewModel, onExitAdmin: () -> Unit) {
    var adminTab by remember { mutableIntStateOf(0) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val errorMessage by viewModel.errorMessage.collectAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    val grimFortunes by viewModel.adminGrimFortunes.collectAsState()
    val realStories by viewModel.adminRealStories.collectAsState()
    val submissions by viewModel.adminSubmissions.collectAsState()
    val scenarios by viewModel.adminScenarios.collectAsState()
    val currentModel by viewModel.selectedGeminiModel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("پنل مدیریت ابدی عمارت وحشت", fontWeight = FontWeight.Bold, color = BloodGlow, fontSize = 16.sp)
                        Text("مدل هوش مصنوعی: $currentModel", style = MaterialTheme.typography.bodySmall, color = MutedAsh, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAdminData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "بازنشانی اطلاعات", tint = SpectralWhite)
                    }
                    IconButton(onClick = onExitAdmin) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "خروج از ادمین", tint = BloodGlow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepCrypt)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = DeepCrypt, tonalElevation = 12.dp) {
                NavigationBarItem(
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("داشبورد", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                    label = { Text("داستان‌ها", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("طالع شوم", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 },
                    icon = { Icon(Icons.Default.AltRoute, contentDescription = null) },
                    label = { Text("سناریوها", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 4,
                    onClick = { adminTab = 4 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    label = { Text("تنظیمات AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 5,
                    onClick = { adminTab = 5 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("اعلان‌ها", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VoidBlack)
        ) {
            when (adminTab) {
                0 -> AdminDashboardTab(grimFortunes.size, realStories.size, submissions.size, scenarios.size, onSwitchTab = { adminTab = it })
                1 -> AdminStoriesManagerTab(viewModel, realStories, submissions)
                2 -> AdminGrimFortuneTab(viewModel, grimFortunes)
                3 -> AdminScenariosTab(viewModel, scenarios)
                4 -> AdminAiSettingsTab(viewModel)
                5 -> AdminNotificationsTab(viewModel)
            }
        }
    }
}

// ----------------------------------------------------
// TAB 5: NOTIFICATIONS MANAGER
// ----------------------------------------------------
@Composable
fun AdminNotificationsTab(viewModel: HorrorViewModel) {
    val notifications by viewModel.notificationsList.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("ارسال اعلان جدید", color = BloodGlow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("پیام") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("لینک تصویر (اختیاری)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (title.isBlank() || message.isBlank()) return@Button
                val notification = CachedAppNotification(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    message = message,
                    imageUrl = imageUrl.ifBlank { null },
                    timestamp = System.currentTimeMillis()
                )
                viewModel.upsertNotification(notification)
                title = ""
                message = ""
                imageUrl = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ارسال اعلان")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("اعلان‌های ارسالی", color = MutedAsh, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        notifications.forEach { notification ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CryptCardElevated)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notification.title, color = SpectralWhite, fontWeight = FontWeight.Bold)
                        Text(notification.message, color = MutedAsh, fontSize = 12.sp)
                    }
                    IconButton(onClick = { viewModel.deleteNotification(notification.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardTab(
    tmCount: Int,
    rsCount: Int,
    subCount: Int,
    scenCount: Int,
    onSwitchTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "مرکز پایش و کنترل عمارت",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = SpectralWhite)
        )
        Text(
            text = "مدیریت محتوای چندرسانه‌ای، داستان‌ها با پوستر، آینه زمان و سناریوهای هوش مصنوعی.",
            color = MutedAsh,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(title = "داستان‌های واقعی و پوسترها", count = rsCount.toString(), icon = Icons.Default.MenuBook, modifier = Modifier.weight(1f).clickable { onSwitchTab(1) })
            StatCard(title = "دریافتی‌های کاربران", count = subCount.toString(), icon = Icons.Default.Inbox, modifier = Modifier.weight(1f).clickable { onSwitchTab(1) })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(title = "طالع‌های ۱۲ ماه", count = tmCount.toString(), icon = Icons.Default.AutoAwesome, modifier = Modifier.weight(1f).clickable { onSwitchTab(2) })
            StatCard(title = "سناریوهای تعاملی", count = scenCount.toString(), icon = Icons.Default.AltRoute, modifier = Modifier.weight(1f).clickable { onSwitchTab(3) })
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "دسترسی‌های سریع ادمین", fontWeight = FontWeight.Bold, color = SpectralWhite)
                Divider(color = MutedAsh.copy(alpha = 0.2f))
                
                Button(
                    onClick = { onSwitchTab(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتشار داستان جدید با پوستر اختصاصی")
                }

                OutlinedButton(
                    onClick = { onSwitchTab(2) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BloodGlow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تولید یک‌جای طالع شوم ۱۲ ماه با هوش مصنوعی (کم‌مصرف)", color = BloodGlow)
                }

                OutlinedButton(
                    onClick = { onSwitchTab(3) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = SpectralWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تولید گروهی سناریوهای بازی با هوش مصنوعی", color = SpectralWhite)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = MutedAsh)
                Icon(icon, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = count, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = BloodGlow))
        }
    }
}

// ----------------------------------------------------
// TAB 1: STORIES & SUBMISSIONS MANAGER (With Posters)
// ----------------------------------------------------
@Composable
fun AdminStoriesManagerTab(
    viewModel: HorrorViewModel,
    realStories: List<RealStory>,
    submissions: List<UserStorySubmission>
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Real Stories, 1: User Submissions
    var showAddStoryDialog by remember { mutableStateOf(false) }
    var storyToEdit by remember { mutableStateOf<RealStory?>(null) }
    var submissionToPublish by remember { mutableStateOf<UserStorySubmission?>(null) }

    // States for Real Stories search, sorting, sub-tab status
    var realStatusTab by remember { mutableIntStateOf(0) } // 0: منتشر شده, 1: منتشر نشده
    var realSearchQuery by remember { mutableStateOf("") }
    var realSortOption by remember { mutableIntStateOf(0) } // 0: Newest, 1: Hottest, 2: Popular
    var showBulkAddDialog by remember { mutableStateOf(false) }
    var selectedRealStoryIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(realStatusTab, realSearchQuery) {
        selectedRealStoryIds = emptySet()
    }

    // States for User Submissions search, sorting, sub-tab status
    var userStatusTab by remember { mutableIntStateOf(0) } // 0: منتشر شده, 1: منتشر نشده
    var userSearchQuery by remember { mutableStateOf("") }
    var userSortOption by remember { mutableIntStateOf(0) } // 0: Newest, 1: Rating, 2: Alphabetical

    // Filter and Sort calculations for Real Stories
    val filteredRealStories = remember(realStories, realStatusTab, realSearchQuery, realSortOption) {
        val statusFiltered = if (realStatusTab == 0) {
            realStories.filter { it.status == "PUBLISHED" }
        } else {
            realStories.filter { it.status != "PUBLISHED" }
        }
        
        val searchFiltered = if (realSearchQuery.isBlank()) {
            statusFiltered
        } else {
            val q = realSearchQuery.trim().lowercase()
            statusFiltered.filter { story ->
                story.title.lowercase().contains(q) ||
                story.content.lowercase().contains(q) ||
                (story.author?.lowercase()?.contains(q) == true) ||
                (story.source?.lowercase()?.contains(q) == true) ||
                (story.createdAt?.lowercase()?.contains(q) == true)
            }
        }
        
        when (realSortOption) {
            1 -> searchFiltered.sortedByDescending { it.view_count }
            2 -> searchFiltered.sortedByDescending { it.rating }
            else -> searchFiltered.sortedByDescending { it.createdAt ?: it.id }
        }
    }

    // Filter and Sort calculations for User Submissions
    val filteredSubmissions = remember(submissions, userStatusTab, userSearchQuery, userSortOption) {
        val statusFiltered = if (userStatusTab == 0) {
            submissions.filter { it.status == "PUBLISHED" }
        } else {
            submissions.filter { it.status != "PUBLISHED" }
        }
        
        val searchFiltered = if (userSearchQuery.isBlank()) {
            statusFiltered
        } else {
            val q = userSearchQuery.trim().lowercase()
            statusFiltered.filter { sub ->
                sub.title.lowercase().contains(q) ||
                sub.content.lowercase().contains(q) ||
                sub.author_name.lowercase().contains(q) ||
                (sub.admin_notes?.lowercase()?.contains(q) == true) ||
                (sub.createdAt?.lowercase()?.contains(q) == true)
            }
        }
        
        when (userSortOption) {
            1 -> searchFiltered.sortedByDescending { it.view_count }
            2 -> searchFiltered.sortedByDescending { it.rating }
            else -> searchFiltered.sortedByDescending { it.createdAt ?: it.id }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sub-Tab Switcher
        TabRow(
            selectedTabIndex = subTab,
            containerColor = CryptCardElevated,
            contentColor = BloodGlow,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("داستان‌های واقعی (${realStories.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("داستان‌های کاربران (${submissions.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Inbox, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subTab == 0) {
            // REAL STORIES SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مدیریت داستان‌های واقعی و پوسترها",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpectralWhite),
                    modifier = Modifier.weight(1.5f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showBulkAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A154B)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن گروهی", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showAddStoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("داستان جدید", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-status tabs (منتشر شده / منتشر نشده)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CryptCardElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("منتشر شده (${realStories.count { it.status == "PUBLISHED" }})", "منتشر نشده (${realStories.count { it.status != "PUBLISHED" }})").forEachIndexed { index, title ->
                    val selected = realStatusTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) BloodCrimson.copy(alpha = 0.8f) else Color.Transparent)
                            .clickable { realStatusTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) Color.White else MutedAsh,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Filter controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = realSearchQuery,
                    onValueChange = { realSearchQuery = it },
                    placeholder = { Text("جستجو در عنوان، متن، کاتب...", color = MutedAsh.copy(alpha = 0.6f), fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodGlow,
                        unfocusedBorderColor = MutedAsh.copy(alpha = 0.3f),
                        focusedContainerColor = CryptCardElevated.copy(alpha = 0.6f),
                        unfocusedContainerColor = CryptCardElevated.copy(alpha = 0.3f),
                        focusedTextColor = SpectralWhite,
                        unfocusedTextColor = SpectralWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                var showSortMenu by remember { mutableStateOf(false) }
                val sortOptions = listOf("جدیدترین‌ها", "داغ‌ترین‌ها", "محبوب‌ترین‌ها")
                
                Box {
                    Button(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, MutedAsh.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortOptions[realSortOption], color = SpectralWhite, fontSize = 11.sp)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(CryptCardElevated)
                    ) {
                        sortOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option, color = SpectralWhite, fontSize = 12.sp) },
                                onClick = {
                                    realSortOption = index
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredRealStories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ داستانی با این مشخصات یافت نشد.", color = MutedAsh)
                }
            } else {
                if (realStatusTab == 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CryptCardElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allIds = filteredRealStories.map { it.id }.toSet()
                        val isAllSelected = selectedRealStoryIds.size == allIds.size && allIds.isNotEmpty()
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable {
                                selectedRealStoryIds = if (isAllSelected) emptySet() else allIds
                            }
                        ) {
                            Checkbox(
                                checked = isAllSelected,
                                onCheckedChange = { checked ->
                                    selectedRealStoryIds = if (checked) allIds else emptySet()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BloodCrimson, uncheckedColor = MutedAsh)
                            )
                            Text(
                                text = if (isAllSelected) "لغو انتخاب همه" else "انتخاب همه داستان‌های این صفحه",
                                color = SpectralWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (selectedRealStoryIds.isNotEmpty()) {
                            Button(
                                onClick = {
                                    viewModel.publishRealStoriesBulk(selectedRealStoryIds.toList()) {
                                        selectedRealStoryIds = emptySet()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("انتشار گروهی (${selectedRealStoryIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredRealStories) { story ->
                        AdminRealStoryHorizontalCard(
                            story = story,
                            onEdit = { storyToEdit = story },
                            onToggleStatus = {
                                val next = if (story.status == "PUBLISHED") "DRAFT" else "PUBLISHED"
                                viewModel.updateRealStoryStatus(story.id, next) {}
                            },
                            onDelete = { viewModel.deleteRealStory(story.id) {} },
                            showSelection = (realStatusTab == 1),
                            isSelected = selectedRealStoryIds.contains(story.id),
                            onSelectionChange = { checked ->
                                selectedRealStoryIds = if (checked) selectedRealStoryIds + story.id else selectedRealStoryIds - story.id
                            }
                        )
                    }
                }
            }
        } else {
            // USER SUBMISSIONS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رازها و داستان‌های ارسالی کاربران",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpectralWhite)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-status tabs (منتشر شده / منتشر نشده)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CryptCardElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("منتشر شده (${submissions.count { it.status == "PUBLISHED" }})", "منتشر نشده (${submissions.count { it.status != "PUBLISHED" }})").forEachIndexed { index, title ->
                    val selected = userStatusTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) BloodCrimson.copy(alpha = 0.8f) else Color.Transparent)
                            .clickable { userStatusTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) Color.White else MutedAsh,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Filter controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    placeholder = { Text("جستجو در عنوان، متن، نام کاتب...", color = MutedAsh.copy(alpha = 0.6f), fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodGlow,
                        unfocusedBorderColor = MutedAsh.copy(alpha = 0.3f),
                        focusedContainerColor = CryptCardElevated.copy(alpha = 0.6f),
                        unfocusedContainerColor = CryptCardElevated.copy(alpha = 0.3f),
                        focusedTextColor = SpectralWhite,
                        unfocusedTextColor = SpectralWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                var showSortMenu by remember { mutableStateOf(false) }
                val sortOptions = listOf("جدیدترین‌ها", "داغ‌ترین‌ها", "محبوب‌ترین‌ها")
                
                Box {
                    Button(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, MutedAsh.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortOptions[userSortOption], color = SpectralWhite, fontSize = 11.sp)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(CryptCardElevated)
                    ) {
                        sortOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option, color = SpectralWhite, fontSize = 12.sp) },
                                onClick = {
                                    userSortOption = index
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredSubmissions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ داستان ارسالی با این مشخصات یافت نشد.", color = MutedAsh)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredSubmissions) { sub ->
                        AdminSubmissionCard(
                            submission = sub,
                            onPublishWithPoster = { submissionToPublish = sub },
                            onReject = { viewModel.updateSubmissionStatus(sub.id, "REJECTED", "رد شده توسط مدیر") {} },
                            onDelete = { viewModel.deleteSubmission(sub.id) {} }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Add New Real Story
    if (showAddStoryDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("") }
        var coverUrl by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("وحشت, واقعی") }
        var isPublished by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddStoryDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("افزودن داستان واقعی با پوستر", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = coverUrl,
                        onValueChange = { coverUrl = it },
                        label = { Text("لینک پوستر تصویر (اختیاری)") },
                        placeholder = { Text("https://example.com/poster.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (coverUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = "پیش‌نمایش پوستر",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("متن داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("نام نویسنده یا راوی") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("منبع") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("برچسب‌ها (با ویرگول جدا کنید)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                viewModel.createRealStory(
                                    title = title,
                                    content = content,
                                    author = author.ifBlank { "کاتب عمارت" },
                                    source = source.ifBlank { "روایات واقعی" },
                                    coverUrl = coverUrl,
                                    tags = tags,
                                    status = "DRAFT"
                                ) {
                                    showAddStoryDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1C38)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("افزودن به پیش‌نویس", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                viewModel.createRealStory(
                                    title = title,
                                    content = content,
                                    author = author.ifBlank { "کاتب عمارت" },
                                    source = source.ifBlank { "روایات واقعی" },
                                    coverUrl = coverUrl,
                                    tags = tags,
                                    status = "PUBLISHED"
                                ) {
                                    showAddStoryDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره و انتشار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStoryDialog = false }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }

    if (showBulkAddDialog) {
        var bulkText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBulkAddDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("افزودن گروهی داستان‌ها (به پیش‌نویس)", color = BloodGlow, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "داستان‌های خود را با جداکننده --- از هم جدا کنید. برای وارد کردن سریع فیلدها می‌توانید از قالب زیر استفاده کنید:",
                        color = SpectralWhite,
                        fontSize = 11.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "عنوان: اسم داستان\nمتن: متن کامل داستان\nپوستر: لینک تصویر یا -\nنویسنده: کاتب عمارت یا -\n---\nعنوان: داستان دوم...\nمتن: متن دوم...",
                            color = BloodGlow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Left
                        )
                    }
                    Text(
                        text = "💡 راهنما: اگر قالبی وارد نکنید، به طور خودکار خط اول به عنوان عنوان و خطوط بعدی به عنوان متن داستان در تب پیش‌نویس (منتشر نشده) ثبت خواهد شد.",
                        color = Color(0xFFDEC595),
                        fontSize = 10.sp
                    )
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        placeholder = { Text("داستان‌ها را اینجا پیست کنید...", color = MutedAsh.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 8,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = parseBulkStories(bulkText)
                        if (parsed.isNotEmpty()) {
                            viewModel.createRealStoriesBulk(parsed) {
                                showBulkAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ثبت گروهی پیش‌نویس‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkAddDialog = false }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }

    // Dialog: Edit Story
    if (storyToEdit != null) {
        val s = storyToEdit!!
        var title by remember { mutableStateOf(s.title) }
        var content by remember { mutableStateOf(s.content) }
        var author by remember { mutableStateOf(s.author ?: "") }
        var source by remember { mutableStateOf(s.source ?: "") }
        var coverUrl by remember { mutableStateOf(s.cover_image_url ?: "") }
        var tags by remember { mutableStateOf(s.tags ?: "") }

        AlertDialog(
            onDismissRequest = { storyToEdit = null },
            containerColor = CryptCardElevated,
            title = { Text("ویرایش داستان و پوستر", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان داستان") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = coverUrl, onValueChange = { coverUrl = it }, label = { Text("آدرس پوستر (Cover URL)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    if (coverUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = "پیش‌نمایش پوستر",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("متن داستان") }, modifier = Modifier.fillMaxWidth(), minLines = 5, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("نویسنده") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("منبع") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("برچسب‌ها") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateRealStory(
                            id = s.id,
                            title = title,
                            content = content,
                            author = author,
                            source = source,
                            coverUrl = coverUrl,
                            tags = tags,
                            status = s.status
                        ) {
                            storyToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { storyToEdit = null }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }

    // Dialog: Publish User Submission with Poster
    if (submissionToPublish != null) {
        val sub = submissionToPublish!!
        var title by remember { mutableStateOf(sub.title) }
        var content by remember { mutableStateOf(sub.content) }
        var coverUrl by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { submissionToPublish = null },
            containerColor = CryptCardElevated,
            title = { Text("تأیید و انتشار داستان کاربر با پوستر", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("نویسنده: ${sub.author_name}", color = SpectralWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان نهایی") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(
                        value = coverUrl,
                        onValueChange = { coverUrl = it },
                        label = { Text("آدرس پوستر داستان (Image URL)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (coverUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = "پیش‌نمایش پوستر",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("متن ویرایش شده داستان") }, modifier = Modifier.fillMaxWidth(), minLines = 5, shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.publishSubmissionAsRealStory(
                            submission = sub,
                            coverUrl = coverUrl,
                            editedTitle = title,
                            editedContent = content
                        ) {
                            submissionToPublish = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("انتشار در برنامه", color = SpectralWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { submissionToPublish = null }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }
}

@Composable
fun AdminRealStoryHorizontalCard(
    story: RealStory,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    showSelection: Boolean = false,
    isSelected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSelection) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = BloodCrimson,
                        uncheckedColor = MutedAsh
                    ),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            // Poster thumbnail on the side
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF130B1C))
                    .border(1.dp, BloodGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!story.cover_image_url.isNullOrBlank()) {
                    AsyncImage(
                        model = story.cover_image_url,
                        contentDescription = "پوستر داستان",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MutedAsh, modifier = Modifier.size(28.dp))
                        Text("بدون پوستر", color = MutedAsh, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details and actions
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpectralWhite),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Badge(containerColor = if (story.status == "PUBLISHED") SuccessNeon else WarningAmber) {
                        Text(if (story.status == "PUBLISHED") "منتشر شده" else "پیش‌نویس", color = SpectralWhite, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                    }
                }

                Text(
                    text = "نویسنده: ${story.author ?: "نامشخص"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BloodGlow,
                    fontSize = 11.sp
                )

                Text(
                    text = story.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedAsh,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ویرایش", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onToggleStatus,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (story.status == "PUBLISHED") "پیش‌نویس" else "انتشار", fontSize = 11.sp)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSubmissionCard(
    submission: UserStorySubmission,
    onPublishWithPoster: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = submission.title, fontWeight = FontWeight.Bold, color = BloodGlow)
                Badge(
                    containerColor = when (submission.status) {
                        "PUBLISHED" -> SuccessNeon
                        "REJECTED" -> BloodCrimson
                        else -> WarningAmber
                    }
                ) {
                    Text(submission.status, color = SpectralWhite, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                }
            }
            Text(text = "ارسال‌کننده: ${submission.author_name}", style = MaterialTheme.typography.bodySmall, color = MutedAsh)
            Text(text = submission.content, style = MaterialTheme.typography.bodyMedium, color = SpectralWhite)

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPublishWithPoster,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأیید و انتشار با پوستر", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("رد کردن", color = BloodGlow, fontSize = 12.sp)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MutedAsh)
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: GRIM FORTUNES (طالع شوم ۱۲ ماه با تولید تک‌درخواستی کم‌مصرف هوش مصنوعی)
// ----------------------------------------------------
@Composable
fun AdminGrimFortuneTab(
    viewModel: HorrorViewModel,
    grimFortunes: List<GrimFortune>
) {
    var savedPrompt by remember { mutableStateOf("") }
    val currentPrompt by viewModel.grimFortunePrompt.collectAsState()
    LaunchedEffect(currentPrompt) { savedPrompt = currentPrompt }

    var selectedMonthIndex by remember { mutableIntStateOf(1) }
    val monthNames = HorrorViewModel.PERSIAN_MONTHS
    val selectedMonthName = monthNames.getOrElse(selectedMonthIndex - 1) { "فروردین" }

    val matchingFortune = grimFortunes.find { it.month_index == selectedMonthIndex }
    var isGeneratingAIBatch by remember { mutableStateOf(false) }
    var isGeneratingAISingle by remember { mutableStateOf(false) }
    var promptExpanded by remember { mutableStateOf(false) }
    var showManualEditDialog by remember { mutableStateOf(false) }
    var batchResultMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Prompt Config & Batch Generation Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { promptExpanded = !promptExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = BloodGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنظیم پرامپت و تولید گروهی طالع شوم ۱۲ ماه (AI)",
                            fontWeight = FontWeight.Bold,
                            color = SpectralWhite,
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        if (promptExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MutedAsh
                    )
                }

                if (promptExpanded) {
                    Text(
                        text = "این پرامپت در یک پیام به هوش مصنوعی فرستاده می‌شود و با فرمت عددگذاری ===1=== تا ===12=== هر ۱۲ ماه را در یک پاسخ دریافت و خودکار ذخیره می‌کند تا توکن بهینه‌سازی شود.",
                        color = MutedAsh,
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = savedPrompt,
                        onValueChange = { savedPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.setGrimFortunePrompt(savedPrompt)
                                batchResultMessage = "پرامپت با موفقیت ذخیره شد."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ذخیره پرامپت", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                savedPrompt = HorrorViewModel.DEFAULT_GRIM_FORTUNE_PROMPT
                                viewModel.setGrimFortunePrompt(savedPrompt)
                            }
                        ) {
                            Text("بازنشانی به پیش‌فرض", color = MutedAsh, fontSize = 12.sp)
                        }
                    }
                }

                Divider(color = MutedAsh.copy(alpha = 0.2f))

                // BATCH TRIGGER BUTTON (SINGLE API CALL FOR ALL 12 MONTHS)
                Button(
                    onClick = {
                        isGeneratingAIBatch = true
                        batchResultMessage = null
                        viewModel.generateGrimFortunesWithAI { success, msg, count ->
                            isGeneratingAIBatch = false
                            batchResultMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1D8C)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGeneratingAIBatch
                ) {
                    if (isGeneratingAIBatch) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SpectralWhite)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("در حال تولید و دسته‌بندی ۱۲ ماه با AI...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SpectralWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تولید همزمان ۱۲ ماه با هوش مصنوعی (یک پیام - صرفه‌جویی توکن)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (batchResultMessage != null) {
                    Surface(
                        color = Color(0xFF1E0E2B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BloodGlow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = batchResultMessage!!,
                            color = SpectralWhite,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 12 PERSIAN MONTHS SELECTOR GRID
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انتخاب ماه طالع (۱۲ ماه سال)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpectralWhite)
                    )
                    Badge(containerColor = BloodCrimson) {
                        Text("ماه $selectedMonthIndex: $selectedMonthName", color = SpectralWhite, modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(12) { index ->
                        val monthIdx = index + 1
                        val name = monthNames[index]
                        val hasContent = grimFortunes.any { it.month_index == monthIdx }
                        val isSelected = selectedMonthIndex == monthIdx

                        Box(
                            modifier = Modifier
                                .aspectRatio(1.2f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> BloodCrimson
                                        hasContent -> Color(0xFF261234)
                                        else -> Color(0xFF0F0816)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) BloodGlow else if (hasContent) BloodGlow.copy(alpha = 0.5f) else MutedAsh.copy(alpha = 0.15f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedMonthIndex = monthIdx },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = name,
                                    fontWeight = if (isSelected || hasContent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SpectralWhite else if (hasContent) BloodGlow else MutedAsh,
                                    fontSize = 12.sp
                                )
                                if (hasContent) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(if (isSelected) SpectralWhite else BloodGlow, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ACTIVE SELECTED MONTH DETAILS & CONTROLS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "طالع ماه $selectedMonthName (ماه $selectedMonthIndex)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BloodGlow)
                    )
                    if (matchingFortune != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge(containerColor = Color(0xFF4A154B)) {
                                Text(matchingFortune.doom_level ?: "شوم", color = SpectralWhite, modifier = Modifier.padding(4.dp))
                            }
                            Badge(containerColor = if (matchingFortune.status == "PUBLISHED") SuccessNeon else WarningAmber) {
                                Text(matchingFortune.status, color = SpectralWhite, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }

                if (matchingFortune != null) {
                    Text(
                        text = matchingFortune.title,
                        fontWeight = FontWeight.Bold,
                        color = SpectralWhite,
                        fontSize = 16.sp
                    )

                    if (!matchingFortune.omen_poem.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFDEC595).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "بیت شوم حافظ: « ${matchingFortune.omen_poem} »",
                                color = Color(0xFFDEC595),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = matchingFortune.fortune_text,
                        color = MutedAsh,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    Divider(color = MutedAsh.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isGeneratingAISingle = true
                                viewModel.generateGrimFortuneForSingleMonth(selectedMonthIndex) { success, title, poem, desc, doom ->
                                    isGeneratingAISingle = false
                                    if (success) {
                                        viewModel.saveGrimFortune(selectedMonthIndex, title, poem, desc, doom, "PUBLISHED") {}
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1D8C)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isGeneratingAISingle
                        ) {
                            if (isGeneratingAISingle) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SpectralWhite)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تولید تک‌ماه AI", fontSize = 11.sp)
                            }
                        }

                        FilledTonalButton(
                            onClick = { showManualEditDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val next = if (matchingFortune.status == "PUBLISHED") "DRAFT" else "PUBLISHED"
                                viewModel.saveGrimFortune(
                                    matchingFortune.month_index,
                                    matchingFortune.title,
                                    matchingFortune.omen_poem,
                                    matchingFortune.fortune_text,
                                    matchingFortune.doom_level,
                                    next
                                ) {}
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (matchingFortune.status == "PUBLISHED") "پیش‌نویس" else "انتشار", fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { viewModel.deleteGrimFortune(matchingFortune.id) {} }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = BloodGlow)
                        }
                    }
                } else {
                    Text(
                        text = "برای ماه $selectedMonthName هنوز طالعی ثبت نشده است. می‌توانید با زدن دکمه هوش مصنوعی یا نوشتن دستی طالع این ماه را ثبت کنید.",
                        color = MutedAsh,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isGeneratingAISingle = true
                                viewModel.generateGrimFortuneForSingleMonth(selectedMonthIndex) { success, title, poem, desc, doom ->
                                    isGeneratingAISingle = false
                                    if (success) {
                                        viewModel.saveGrimFortune(selectedMonthIndex, title, poem, desc, doom, "PUBLISHED") {}
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isGeneratingAISingle
                        ) {
                            if (isGeneratingAISingle) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SpectralWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("در حال خلق طالع...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("خلق طالع با هوش مصنوعی", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showManualEditDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("نوشتن دستی", color = SpectralWhite)
                        }
                    }
                }
            }
        }
    }

    if (showManualEditDialog) {
        var mTitle by remember { mutableStateOf(matchingFortune?.title ?: "طالع تاریک ماه $selectedMonthName") }
        var mPoem by remember { mutableStateOf(matchingFortune?.omen_poem ?: "") }
        var mDesc by remember { mutableStateOf(matchingFortune?.fortune_text ?: "") }
        var mDoom by remember { mutableStateOf(matchingFortune?.doom_level ?: "بسیار شوم") }

        AlertDialog(
            onDismissRequest = { showManualEditDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("ویرایش / ثبت طالع ماه $selectedMonthName", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = mTitle,
                        onValueChange = { mTitle = it },
                        label = { Text("عنوان طالع") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = mPoem,
                        onValueChange = { mPoem = it },
                        label = { Text("بیت شعر فال حافظ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = mDesc,
                        onValueChange = { mDesc = it },
                        label = { Text("تفسیر و هشدارهای طالع شوم") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = mDoom,
                        onValueChange = { mDoom = it },
                        label = { Text("درجه شومی (مثلاً: شوم، بسیار شوم، نفرین ابدی)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mTitle.isNotBlank() && mDesc.isNotBlank()) {
                            viewModel.saveGrimFortune(
                                monthIndex = selectedMonthIndex,
                                title = mTitle,
                                poem = mPoem,
                                fortuneText = mDesc,
                                doomLevel = mDoom,
                                status = matchingFortune?.status ?: "PUBLISHED"
                            ) {
                                showManualEditDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره و ثبت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualEditDialog = false }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }
}

// ----------------------------------------------------
// TAB 3: WRONG CHOICE SCENARIOS (AI Batch Generation)
// ----------------------------------------------------
@Composable
fun AdminScenariosTab(
    viewModel: HorrorViewModel,
    scenarios: List<WrongChoiceScenario>
) {
    var savedPrompt by remember { mutableStateOf("") }
    val currentPrompt by viewModel.scenarioPrompt.collectAsState()
    LaunchedEffect(currentPrompt) { savedPrompt = currentPrompt }

    var scenarioCountToGenerate by remember { mutableIntStateOf(3) }
    var isGeneratingAI by remember { mutableStateOf(false) }
    var genResultFeedback by remember { mutableStateOf<String?>(null) }
    var promptExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI Prompt Config Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodGlow.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { promptExpanded = !promptExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = BloodGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تنظیم پرامپت اصلی سناریوهای وحشت", fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 13.sp)
                    }
                    Icon(
                        if (promptExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MutedAsh
                    )
                }

                if (promptExpanded) {
                    Text(
                        text = "این پرامپت به عنوان الگوی اصلی برای خلق سناریوهای چندگزینه‌ای ذخیره می‌شود.",
                        color = MutedAsh,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = savedPrompt,
                        onValueChange = { savedPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.setScenarioPrompt(savedPrompt) },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ذخیره پرامپت سناریو", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                savedPrompt = HorrorViewModel.DEFAULT_SCENARIO_PROMPT
                                viewModel.setScenarioPrompt(savedPrompt)
                            }
                        ) {
                            Text("بازنشانی پیش‌فرض", color = MutedAsh, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Batch Generator Action Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "تولید گروهی سناریو با هوش مصنوعی", fontWeight = FontWeight.Bold, color = SpectralWhite)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تعداد سناریوها:", color = MutedAsh, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 5).forEach { count ->
                            val isSel = scenarioCountToGenerate == count
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BloodCrimson else CryptCardElevated)
                                    .clickable { scenarioCountToGenerate = count }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("$count", color = if (isSel) SpectralWhite else MutedAsh, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        isGeneratingAI = true
                        genResultFeedback = null
                        viewModel.generateScenariosWithAI(scenarioCountToGenerate) { success, msg ->
                            isGeneratingAI = false
                            genResultFeedback = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingAI) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SpectralWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال تولید $scenarioCountToGenerate سناریوی ماورایی...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تولید و اضافه کردن به پایگاه داده")
                    }
                }

                if (genResultFeedback != null) {
                    Text(
                        text = genResultFeedback!!,
                        color = if (genResultFeedback!!.startsWith("خطا")) BloodGlow else SuccessNeon,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scenario List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("سناریوهای ثبت شده (${scenarios.size})", fontWeight = FontWeight.Bold, color = SpectralWhite)
            OutlinedButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن دستی")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (scenarios.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هیچ سناریویی یافت نشد.", color = MutedAsh)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(scenarios) { scen ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = CryptCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = scen.title, fontWeight = FontWeight.Bold, color = BloodGlow, modifier = Modifier.weight(1f))
                                Badge(containerColor = if (scen.status == "PUBLISHED") SuccessNeon else WarningAmber) {
                                    Text(scen.status, color = SpectralWhite, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                                }
                            }
                            Text(text = scen.description, style = MaterialTheme.typography.bodySmall, color = SpectralWhite, maxLines = 3, overflow = TextOverflow.Ellipsis)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val next = if (scen.status == "PUBLISHED") "DRAFT" else "PUBLISHED"
                                        viewModel.updateScenarioStatus(scen.id, next) {}
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (scen.status == "PUBLISHED") "تبدیل به پیش‌نویس" else "انتشار", fontSize = 11.sp)
                                }

                                IconButton(onClick = { viewModel.deleteScenario(scen.id) {} }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("افزودن سناریوی وحشت دستی", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان سناریو") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("توصیف موقعیت و گزینه‌ها") }, modifier = Modifier.fillMaxWidth(), minLines = 5, shape = RoundedCornerShape(10.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            viewModel.createScenario(title, description, "PUBLISHED") {
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره و انتشار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }
}

// ----------------------------------------------------
// TAB 4: GEMINI AI & API SETTINGS (Exclusive Models)
// ----------------------------------------------------
@Composable
fun AdminAiSettingsTab(viewModel: HorrorViewModel) {
    val currentApiKey by viewModel.geminiApiKey.collectAsState()
    val currentModel by viewModel.selectedGeminiModel.collectAsState()
    val currentSupUrl by viewModel.supabaseUrl.collectAsState()
    val currentSupKey by viewModel.supabaseAnonKey.collectAsState()

    var inputKey by remember { mutableStateOf("") }
    LaunchedEffect(currentApiKey) { inputKey = currentApiKey }

    var supUrl by remember { mutableStateOf("") }
    var supKey by remember { mutableStateOf("") }
    LaunchedEffect(currentSupUrl, currentSupKey) {
        supUrl = currentSupUrl
        supKey = currentSupKey
    }

    var showKey by remember { mutableStateOf(false) }
    var showSupKey by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestingModel by remember { mutableStateOf(false) }
    var testingSpecificModel by remember { mutableStateOf<String?>(null) }

    // EXACT 4 MODELS REQUESTED BY USER
    val supportedModels = HorrorViewModel.SUPPORTED_GEMINI_MODELS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "تنظیمات اختصاصی کلید و مدل‌های هوش مصنوعی",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BloodGlow)
        )
        Text(
            text = "شما می‌توانید کلید اختصاصی Google AI Studio خود را وارد کرده، بین ۴ مدل مشخص شده سوئیچ کرده و هر کدام را به صورت مجزا تست نمایید.",
            color = MutedAsh,
            style = MaterialTheme.typography.bodySmall
        )

        // API Key Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "کلید اختصاصی Gemini API Key", fontWeight = FontWeight.Bold, color = SpectralWhite)

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("کلید API (Gemini)") },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MutedAsh
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodGlow,
                        focusedTextColor = SpectralWhite,
                        unfocusedTextColor = SpectralWhite
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.setGeminiApiKey(inputKey)
                            testResultText = "کلید با موفقیت ذخیره شد."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره کلید API")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.setGeminiApiKey("")
                            inputKey = ""
                            testResultText = "کلید بازنشانی شد."
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حذف کلید", color = MutedAsh)
                    }
                }
            }
        }

        // Model Selection Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "انتخاب مدل فعال هوش مصنوعی (سهمیه اختصاصی)", fontWeight = FontWeight.Bold, color = SpectralWhite)

                supportedModels.forEach { model ->
                    val isSelected = currentModel == model
                    val isTestingThis = testingSpecificModel == model

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isSelected) BloodGlow else MutedAsh.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSelectedGeminiModel(model) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF280F1E) else CryptCardElevated
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setSelectedGeminiModel(model) },
                                        colors = RadioButtonDefaults.colors(selectedColor = BloodGlow)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = model,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SpectralWhite else MutedAsh,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = when (model) {
                                        "gemini-3.5-flash" -> "مدل 3.5flash - پرسرعت و متعادل برای روایات و سناریو"
                                        "gemini-3.6-flash" -> "مدل 3.6flash - ارتقای سرعت و کیفیت ادبیات گوتیک"
                                        "gemini-3.7-flash" -> "مدل 3.7flash - پیشرفته‌ترین مدل تحلیلی و خلاقانه"
                                        "gemini-3.5-flash-lite" -> "مدل 3.5flashlite - سبک، فوق‌العاده سریع با کمترین تاخیر"
                                        else -> ""
                                    },
                                    color = MutedAsh,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 36.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    testingSpecificModel = model
                                    testResultText = null
                                    viewModel.testGeminiModel(inputKey, model) { success, msg ->
                                        testingSpecificModel = null
                                        testResultText = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) BloodCrimson else CryptCardElevated),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                if (isTestingThis) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SpectralWhite)
                                } else {
                                    Text("تست مدل", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Test Response Box
        if (testResultText != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BloodGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "نتیجه آزمایش ارتباط با مدل:", fontWeight = FontWeight.Bold, color = SpectralWhite)
                    Surface(
                        color = VoidBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResultText!!,
                            color = if (testResultText!!.contains("خطا")) BloodGlow else SuccessNeon,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Supabase Settings & Diagnostics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "تنظیمات اتصال پایگاه داده Supabase", fontWeight = FontWeight.Bold, color = SpectralWhite)
                Text(
                    text = "شما می‌توانید تنظیمات دیتابیس Supabase خود را در این بخش تغییر داده و به صورت محلی در دستگاه ذخیره کنید.",
                    color = MutedAsh,
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = supUrl,
                    onValueChange = { supUrl = it },
                    label = { Text("آدرس پروژه Supabase (URL)") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodGlow,
                        focusedTextColor = SpectralWhite,
                        unfocusedTextColor = SpectralWhite
                    )
                )

                OutlinedTextField(
                    value = supKey,
                    onValueChange = { supKey = it },
                    label = { Text("کلید عمومی (Anon Key)") },
                    placeholder = { Text("your-anon-key-here...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showSupKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showSupKey = !showSupKey }) {
                            Icon(
                                imageVector = if (showSupKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MutedAsh
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodGlow,
                        focusedTextColor = SpectralWhite,
                        unfocusedTextColor = SpectralWhite
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            viewModel.saveSupabaseConfig(supUrl, supKey) { success, msg ->
                                testResultText = if (success) "تنظیمات ذخیره شد و متصل شد: $msg" else "خطا در تنظیمات: $msg"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ذخیره محلی", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.testSupabaseConnection { success, msg ->
                                testResultText = if (success) "تست اتصال Supabase: $msg" else "خطا در اتصال Supabase: $msg"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تست اتصال", fontSize = 11.sp, color = SpectralWhite)
                    }
                }
            }
        }
    }
}

fun parseBulkStories(inputText: String): List<RealStory> {
    val rawBlocks = inputText.split("---")
    val list = mutableListOf<RealStory>()
    for (block in rawBlocks) {
        val trimmedBlock = block.trim()
        if (trimmedBlock.isBlank()) continue
        
        var title = ""
        var content = ""
        var poster: String? = null
        var author = "کاتب عمارت"
        var source = "روایات واقعی"
        
        val lines = trimmedBlock.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) continue
        
        // Check if block uses structured format (has 'عنوان:' or 'متن:')
        val hasKeys = lines.any { it.startsWith("عنوان:") || it.startsWith("متن:") }
        if (hasKeys) {
            for (line in lines) {
                when {
                    line.startsWith("عنوان:") -> {
                        title = line.substringAfter("عنوان:").trim()
                    }
                    line.startsWith("متن:") -> {
                        content = line.substringAfter("متن:").trim()
                    }
                    line.startsWith("پوستر:") -> {
                        val p = line.substringAfter("پوستر:").trim()
                        if (p != "-" && p != "—" && p.isNotBlank()) {
                            poster = p
                        }
                    }
                    line.startsWith("نویسنده:") -> {
                        val a = line.substringAfter("نویسنده:").trim()
                        if (a != "-" && a != "—" && a.isNotBlank()) {
                            author = a
                        }
                    }
                    line.startsWith("منبع:") -> {
                        val s = line.substringAfter("منبع:").trim()
                        if (s != "-" && s != "—" && s.isNotBlank()) {
                            source = s
                        }
                    }
                    else -> {
                        if (content.isNotEmpty()) {
                            content += "\n" + line
                        }
                    }
                }
            }
        } else {
            // Unstructured: first line is Title, the rest is Content!
            title = lines[0]
            content = lines.drop(1).joinToString("\n")
            if (content.isBlank()) {
                content = title
                title = if (title.length > 25) title.take(25) + "..." else title
            }
        }
        
        if (title.isNotBlank() && content.isNotBlank()) {
            list.add(
                RealStory(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    author = author,
                    source = source,
                    cover_image_url = poster,
                    tags = "وحشت, واقعی",
                    status = "DRAFT",
                    createdAt = null,
                    updatedAt = null
                )
            )
        }
    }
    return list
}
