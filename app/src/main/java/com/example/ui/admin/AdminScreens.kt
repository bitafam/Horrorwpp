package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.HorrorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(viewModel: HorrorViewModel, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ورود امن ادمین (Supabase Auth)", fontWeight = FontWeight.Bold) },
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
                    modifier = Modifier.padding(28.dp),
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ایمیل ادمین") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = MutedAsh.copy(alpha = 0.4f),
                            focusedLabelColor = BloodGlow
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = MutedAsh.copy(alpha = 0.4f),
                            focusedLabelColor = BloodGlow
                        )
                    )

                    if (errorText != null) {
                        Text(text = errorText!!, color = BloodGlow, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
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

    val timeMirrors by viewModel.adminTimeMirrors.collectAsState()
    val realStories by viewModel.adminRealStories.collectAsState()
    val submissions by viewModel.adminSubmissions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پنل مدیریت ابدی (Admin Console)", fontWeight = FontWeight.Bold, color = BloodGlow) },
                actions = {
                    IconButton(onClick = onExitAdmin) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = BloodGlow)
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
                    label = { Text("داشبورد") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    icon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null) },
                    label = { Text("آینه زمان") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("داستان واقعی") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 },
                    icon = { Icon(Icons.Default.Inbox, contentDescription = null) },
                    label = { Text("دریافتی‌ها") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 4,
                    onClick = { adminTab = 4 },
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                    label = { Text("دیباگ") },
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
                0 -> AdminDashboardTab(timeMirrors.size, realStories.size, submissions.size)
                1 -> AdminTimeMirrorTab(viewModel, timeMirrors)
                2 -> AdminRealStoriesTab(viewModel, realStories)
                3 -> AdminModerationTab(viewModel, submissions)
                4 -> AdminDebugTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardTab(tmCount: Int, rsCount: Int, subCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "آمار و نظارت پایگاه داده Supabase",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = SpectralWhite)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(title = "محتوای آینه زمان", count = tmCount.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "داستان‌های واقعی", count = rsCount.toString(), modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(title = "داستان‌های در انتظار (PENDING)", count = subCount.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "وضعیت امنیت RLS", count = "فعال و ایمن", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MutedAsh)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = count, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = BloodGlow))
        }
    }
}

@Composable
fun AdminTimeMirrorTab(viewModel: HorrorViewModel, items: List<com.example.data.TimeMirrorContent>) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "مدیریت آینه زمان", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SpectralWhite))
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("افزودن روایت")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هیچ روایتی ثبت نشده است.", color = MutedAsh)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CryptCard),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = item.title, fontWeight = FontWeight.Bold, color = BloodGlow)
                                Badge(containerColor = if (item.status == "PUBLISHED") SuccessNeon else CryptCardElevated) {
                                    Text(item.status, color = SpectralWhite, modifier = Modifier.padding(6.dp))
                                }
                            }
                            Text(text = "تاریخ: ${item.date_key}", style = MaterialTheme.typography.bodySmall, color = MutedAsh)
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (item.status != "PUBLISHED") {
                                    Button(
                                        onClick = { viewModel.updateTimeMirrorStatus(item.id, "PUBLISHED") { } },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("انتشار") }
                                } else {
                                    Button(
                                        onClick = { viewModel.updateTimeMirrorStatus(item.id, "DRAFT") { } },
                                        colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("پیش‌نویس") }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.deleteTimeMirror(item.id) { } },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("حذف", color = BloodGlow) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var dateKey by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var narrative by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("افزودن روایت آینه زمان", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = dateKey, onValueChange = { dateKey = it }, label = { Text("تاریخ (مثال: 1405-06-08)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = narrative, onValueChange = { narrative = it }, label = { Text("روایت ترسناک") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(14.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dateKey.isNotBlank() && title.isNotBlank() && narrative.isNotBlank()) {
                            viewModel.createTimeMirror(dateKey, title, narrative, "PUBLISHED") {
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ذخیره و انتشار")
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("انصراف", color = MutedAsh) } }
        )
    }
}

@Composable
fun AdminRealStoriesTab(viewModel: HorrorViewModel, items: List<com.example.data.RealStory>) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "مدیریت داستان‌های واقعی", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SpectralWhite))
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("افزودن داستان")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هیچ داستان واقعی ثبت نشده است.", color = MutedAsh)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CryptCard),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = item.title, fontWeight = FontWeight.Bold, color = BloodGlow)
                                Badge(containerColor = if (item.status == "PUBLISHED") SuccessNeon else CryptCardElevated) {
                                    Text(item.status, color = SpectralWhite, modifier = Modifier.padding(6.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (item.status != "PUBLISHED") {
                                    Button(
                                        onClick = { viewModel.updateRealStoryStatus(item.id, "PUBLISHED") { } },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("انتشار") }
                                } else {
                                    Button(
                                        onClick = { viewModel.updateRealStoryStatus(item.id, "DRAFT") { } },
                                        colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("پیش‌نویس") }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.deleteRealStory(item.id) { } },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("حذف", color = BloodGlow) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("افزودن داستان واقعی جدید", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان داستان") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("متن داستان") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("نویسنده / منبع") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.createRealStory(title, content, author, "Supabase Admin", "", "horror", "PUBLISHED") {
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("انتشار در دیتابیس")
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("انصراف", color = MutedAsh) } }
        )
    }
}

@Composable
fun AdminModerationTab(viewModel: HorrorViewModel, submissions: List<com.example.data.UserStorySubmission>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = "داستان‌های دریافتی کاربران (Moderation)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SpectralWhite))
        Spacer(modifier = Modifier.height(16.dp))

        if (submissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هیچ داستان ارسالی در انتظار بررسی وجود ندارد.", color = MutedAsh)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(submissions) { sub ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CryptCard),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = sub.title, fontWeight = FontWeight.Bold, color = BloodGlow)
                                Badge(containerColor = if (sub.status == "PUBLISHED") SuccessNeon else WarningAmber) {
                                    Text(sub.status, color = SpectralWhite, modifier = Modifier.padding(6.dp))
                                }
                            }
                            Text(text = "نویسنده: ${sub.author_name}", style = MaterialTheme.typography.bodySmall, color = MutedAsh)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = sub.content, style = MaterialTheme.typography.bodyMedium, color = SpectralWhite)
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.updateSubmissionStatus(sub.id, "PUBLISHED", null) { } },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تأیید و انتشار")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.updateSubmissionStatus(sub.id, "REJECTED", null) { } },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("رد کردن", color = BloodGlow)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDebugTab(viewModel: HorrorViewModel) {
    val email by viewModel.currentUserEmail.collectAsState()
    val userId by viewModel.currentUserId.collectAsState()
    val role by viewModel.currentUserRole.collectAsState()
    val token = com.example.data.SupabaseClientProvider.currentAuthToken
    var testResult by remember { mutableStateOf<String?>(null) }
    var testingConnection by remember { mutableStateOf(false) }

    var testSubTitle by remember { mutableStateOf("تست داستان دیباگ") }
    var testSubContent by remember { mutableStateOf("این یک تست خودکار از سمت پنل دیباگ است.") }
    var subResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "حالت دیباگ و عیب‌یابی (Debug & Diagnostics)",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = BloodGlow)
            )
            Text(
                text = "این بخش مخصوص مدیران سیستم جهت بررسی سلامت اتصال، احراز هویت و پایگاه داده است.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedAsh
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = CryptCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "وضعیت احراز هویت و نقش (Auth & Role)", fontWeight = FontWeight.Bold, color = SpectralWhite)
                    Divider(color = MutedAsh.copy(alpha = 0.2f))
                    Text(text = "ایمیل کاربر فعلی: ${email ?: "نامشخص"}", color = MutedAsh)
                    Text(text = "شناسه کاربر (User ID): ${userId ?: "نامشخص"}", color = MutedAsh)
                    Text(text = "نقش کاربر (Role): ${role ?: "ADMIN"}", color = SuccessNeon)
                    Text(text = "توکن احراز هویت: ${if (token != null) "موجود و فعال" else "موجود نیست"}", color = if (token != null) SuccessNeon else BloodGlow)
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = CryptCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "تست اتصال به Supabase (Connection Test)", fontWeight = FontWeight.Bold, color = SpectralWhite)
                    Divider(color = MutedAsh.copy(alpha = 0.2f))
                    Text(text = "آدرس URL: ${com.example.data.SupabaseClientProvider.supabaseUrl}", color = MutedAsh, fontSize = 12.sp)

                    Button(
                        onClick = {
                            testingConnection = true
                            testResult = null
                            viewModel.testConnection { success, msg ->
                                testingConnection = false
                                testResult = if (success) "موفق: $msg" else "خطا: $msg"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (testingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SpectralWhite)
                        } else {
                            Text("اجرای تست اتصال به پایگاه داده")
                        }
                    }

                    if (testResult != null) {
                        Text(
                            text = testResult!!,
                            color = if (testResult!!.startsWith("موفق")) SuccessNeon else BloodGlow,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = CryptCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "تست ارسال داستان (Story Submission Test)", fontWeight = FontWeight.Bold, color = SpectralWhite)
                    Divider(color = MutedAsh.copy(alpha = 0.2f))
                    OutlinedTextField(
                        value = testSubTitle,
                        onValueChange = { testSubTitle = it },
                        label = { Text("عنوان تست") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = testSubContent,
                        onValueChange = { testSubContent = it },
                        label = { Text("متن تست") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.submitUserStory(testSubTitle, testSubContent, "مدیر سیستم (دیباگ)") { success ->
                                subResult = if (success) "ارسال داستان با موفقیت در جدول user_story_submissions ثبت شد." else "خطا در ثبت داستان."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ارسال تست داستان به دیتابیس")
                    }
                    if (subResult != null) {
                        Text(text = subResult!!, color = SpectralWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
