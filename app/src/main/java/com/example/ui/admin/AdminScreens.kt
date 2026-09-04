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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
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
    var activeViewingStory by remember { mutableStateOf<RealStory?>(null) }

    if (activeViewingStory != null) {
        com.example.ui.user.StoryReaderScreen(
            story = activeViewingStory!!,
            viewModel = viewModel,
            onBack = { activeViewingStory = null }
        )
        return
    }

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
    val aiStories by viewModel.adminAiStories.collectAsState()
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
                    label = { Text("داشبورد", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                    label = { Text("داستان‌ها", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("طالع", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    label = { Text("داستان AI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 4,
                    onClick = { adminTab = 4 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    label = { Text("تنظیم AI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 5,
                    onClick = { adminTab = 5 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                    label = { Text("اتوماسیون", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BloodGlow, unselectedIconColor = MutedAsh)
                )
                NavigationBarItem(
                    selected = adminTab == 6,
                    onClick = { adminTab = 6 },
                    icon = { Icon(Icons.Default.Report, contentDescription = null) },
                    label = { Text("گزارش‌ها", fontSize = 10.sp) },
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
                0 -> AdminDashboardTab(grimFortunes.size, realStories.size, submissions.size, aiStories.size, onSwitchTab = { adminTab = it })
                1 -> AdminStoriesManagerTab(viewModel, realStories, submissions)
                2 -> AdminGrimFortuneTab(viewModel, grimFortunes)
                3 -> AdminAiStoriesTab(viewModel, aiStories)
                4 -> AdminAiSettingsTab(viewModel)
                5 -> AdminAutomationTab(viewModel)
                6 -> AdminReportsTab(viewModel) { story -> activeViewingStory = story }
            }
        }
    }
}

// ----------------------------------------------------
// EXACT TIME PICKER COMPONENT (Minute & Hour Precision)
// ----------------------------------------------------
@Composable
fun ExactTimePickerCard(
    title: String,
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = SpectralWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color(0xFF1E0E2B),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BloodGlow.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "${String.format("%02d", hour)}:${String.format("%02d", minute)}",
                        color = SpectralWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick Preset Buttons based on current Iran Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
                val curH = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val curM = cal.get(java.util.Calendar.MINUTE)

                listOf(
                    Pair("+2 دقیقه", 2),
                    Pair("+5 دقیقه", 5),
                    Pair("+15 دقیقه", 15),
                    Pair("+1 ساعت", 60)
                ).forEach { (label, addMins) ->
                    val totalM = curM + addMins
                    val newH = (curH + totalM / 60) % 24
                    val newM = totalM % 60
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2A1538))
                            .clickable {
                                onTimeChanged(newH, newM)
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = BloodGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Adjust Hours and Minutes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour controls
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ساعت (۰۰ - ۲۳)", color = MutedAsh, fontSize = 10.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onTimeChanged(if (hour <= 0) 23 else hour - 1, minute) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = SpectralWhite, modifier = Modifier.size(16.dp))
                        }
                        Text("${String.format("%02d", hour)}", color = SpectralWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { onTimeChanged((hour + 1) % 24, minute) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SpectralWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp),
                    color = MutedAsh.copy(alpha = 0.3f)
                )

                // Minute controls
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("دقیقه (۰۰ - ۵۹)", color = MutedAsh, fontSize = 10.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onTimeChanged(hour, if (minute <= 0) 59 else minute - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = SpectralWhite, modifier = Modifier.size(16.dp))
                        }
                        Text("${String.format("%02d", minute)}", color = SpectralWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { onTimeChanged(hour, (minute + 1) % 60) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SpectralWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 5: AUTOMATION & EDGE FUNCTIONS (Independent Tasks)
// ----------------------------------------------------
@Composable
fun AdminAutomationTab(viewModel: HorrorViewModel) {
    val automationConfigs by viewModel.automationConfigs.collectAsState()
    val automationLogs by viewModel.automationLogs.collectAsState()
    val currentApiKey by viewModel.geminiApiKey.collectAsState()
    val currentModel by viewModel.selectedGeminiModel.collectAsState()

    var inputKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var feedbackMsg by remember { mutableStateOf<String?>(null) }
    var runningTask by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAutomationData()
    }

    LaunchedEffect(currentApiKey) {
        inputKey = currentApiKey
    }

    val fortuneConfig = automationConfigs.find { it.id == "AUTO_GRIM_FORTUNES" } ?: AutomationConfig(id = "AUTO_GRIM_FORTUNES", is_active = false, frequency = "DAILY", schedule_hour_1 = 0)
    val scenarioConfig = automationConfigs.find { it.id == "AUTO_SCENARIOS" } ?: AutomationConfig(id = "AUTO_SCENARIOS", is_active = false, frequency = "TWICE_DAILY", schedule_hour_1 = 14, schedule_hour_2 = 22, batch_count = 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "مرکز اتوماسیون هوشمند و Edge Functions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BloodGlow)
        )
        Text(
            text = "هر یک از بخش‌های اتوماسیون زیر کاملاً مستقل و مجزا فعالیت می‌کنند و کلید Gemini در پایگاه‌داده دیتابیس Supabase به صورت امن نگهداری می‌شود.",
            color = MutedAsh,
            style = MaterialTheme.typography.bodySmall
        )

        // GEMINI API KEY & MODEL IN DB
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = BloodGlow)
                    Text("کلید هوش مصنوعی در پایگاه داده (Supabase Secrets)", fontWeight = FontWeight.Bold, color = SpectralWhite)
                }
                Text(
                    "کلید ثبت‌شده در این فیلد مستقیماً در جدول app_settings دیتابیس ذخیره شده و فانکشن‌های سروری مستقلاً از آن استفاده می‌کنند.",
                    color = MutedAsh,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Gemini API Key") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.setGeminiApiKey(inputKey)
                            feedbackMsg = "کلید Gemini با موفقیت در دیتابیس Supabase ذخیره گردید."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره در دیتابیس", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.setSelectedGeminiModel(
                                if (currentModel == "gemini-2.5-flash") "gemini-1.5-flash" else "gemini-2.5-flash"
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("مدل: $currentModel", fontSize = 11.sp, color = SpectralWhite)
                    }
                }
            }
        }

        // ==========================================
        // 1. AUTO GRIM FORTUNES AUTOMATION (24 Hours)
        // ==========================================
        var fortuneHour by remember { mutableIntStateOf(fortuneConfig.schedule_hour_1) }
        LaunchedEffect(fortuneConfig) {
            fortuneHour = fortuneConfig.schedule_hour_1
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (fortuneConfig.is_active) BloodGlow.copy(alpha = 0.5f) else MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if (fortuneConfig.is_active) BloodGlow else MutedAsh)
                        Column {
                            Text("۲. تولید خودکار طالع شوم ۱۲ ماه", fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 14.sp)
                            Text("هر ۲۴ ساعت در ساعت دلخواه", color = MutedAsh, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = fortuneConfig.is_active,
                        onCheckedChange = { active ->
                            viewModel.saveAutomationConfig(fortuneConfig.copy(is_active = active, schedule_hour_1 = fortuneHour)) {
                                feedbackMsg = "تنظیمات تولید طالع ذخیره شد."
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SpectralWhite, checkedTrackColor = BloodCrimson)
                    )
                }

                // Hour Picker for Fortune
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ساعت انتشار خودکار طالع (زمان ایران):", color = SpectralWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = { fortuneHour = if (fortuneHour <= 0) 23 else fortuneHour - 1 },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = SpectralWhite)
                            }
                            Text(
                                text = String.format("%02d:00", fortuneHour),
                                color = BloodGlow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(
                                onClick = { fortuneHour = (fortuneHour + 1) % 24 },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = SpectralWhite)
                            }
                        }
                    }
                }

                Text(
                    text = "طالع هر ۱۲ ماه سال، رأس ساعت ${String.format("%02d", fortuneHour)}:۰۰ به وقت تهران با پرامپت کلی بخش طالع‌ها تولید و جایگزین می‌شود.",
                    color = BloodGlow,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضعیت: " + (if (fortuneConfig.is_active) "فعال در ساعت ${String.format("%02d", fortuneHour)}:۰۰" else "غیرفعال"),
                        color = if (fortuneConfig.is_active) SuccessNeon else WarningAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.saveAutomationConfig(
                                    fortuneConfig.copy(
                                        schedule_hour_1 = fortuneHour
                                    )
                                ) {
                                    feedbackMsg = "تنظیمات طالع با موفقیت ذخیره شد."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ذخیره تنظیمات", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                runningTask = "auto-grim-fortunes"
                                viewModel.triggerEdgeFunction("auto-grim-fortunes") { success, msg ->
                                    runningTask = null
                                    feedbackMsg = msg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (runningTask == "auto-grim-fortunes") {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SpectralWhite)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تولید و تست فوری", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. AUTO AI STORIES AUTOMATION (1 or 2 per day, up to 20 stories)
        // ==========================================
        val storyConfig = automationConfigs.find { it.id == "AUTO_AI_STORIES" } ?: AutomationConfig(id = "AUTO_AI_STORIES", is_active = false, frequency = "TWICE_DAILY", schedule_hour_1 = 14, schedule_hour_2 = 22, batch_count = 3)
        var storyFreq by remember { mutableStateOf(storyConfig.frequency) }
        var storyHour1 by remember { mutableIntStateOf(storyConfig.schedule_hour_1) }
        var storyHour2 by remember { mutableIntStateOf(storyConfig.schedule_hour_2) }
        var storyBatchCount by remember { mutableIntStateOf(storyConfig.batch_count) }

        LaunchedEffect(storyConfig) {
            storyFreq = storyConfig.frequency
            storyHour1 = storyConfig.schedule_hour_1
            storyHour2 = storyConfig.schedule_hour_2
            storyBatchCount = storyConfig.batch_count
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (storyConfig.is_active) BloodGlow.copy(alpha = 0.5f) else MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = if (storyConfig.is_active) BloodGlow else MutedAsh)
                        Column {
                            Text("۲. تولید خودکار داستان‌های هوش مصنوعی", fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 14.sp)
                            Text("تولید تا ۲۰ داستان خودکار در دیتابیس بدون نیاز به باز بودن برنامه", color = MutedAsh, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = storyConfig.is_active,
                        onCheckedChange = { active ->
                            viewModel.saveAutomationConfig(
                                storyConfig.copy(
                                    is_active = active,
                                    frequency = storyFreq,
                                    schedule_hour_1 = storyHour1,
                                    schedule_hour_2 = storyHour2,
                                    batch_count = storyBatchCount
                                )
                            ) {
                                feedbackMsg = "تنظیمات داستان‌های هوش مصنوعی ذخیره شد."
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SpectralWhite, checkedTrackColor = BloodCrimson)
                    )
                }

                // Batch Count Selector (1 to 20)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تعداد داستان در هر نوبت اتوماسیون:", color = SpectralWhite, fontSize = 12.sp)
                        Text("$storyBatchCount داستان", color = BloodGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 3, 5, 10, 15, 20).forEach { c ->
                            val isSel = storyBatchCount == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BloodCrimson else CryptCardElevated)
                                    .clickable { storyBatchCount = c }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$c", color = if (isSel) SpectralWhite else MutedAsh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Frequency Selector: Once a day vs Twice a day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تناوب اجرای اتوماسیون در روز:", color = SpectralWhite, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isOnce = storyFreq == "DAILY"
                        val isTwice = storyFreq == "TWICE_DAILY"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isOnce) BloodCrimson else CryptCardElevated)
                                .clickable { storyFreq = "DAILY" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("۱ بار در روز", color = if (isOnce) SpectralWhite else MutedAsh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isTwice) BloodCrimson else CryptCardElevated)
                                .clickable { storyFreq = "TWICE_DAILY" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("۲ بار در روز", color = if (isTwice) SpectralWhite else MutedAsh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Hour Picker 1 for Stories
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ساعت نوبت اول (زمان ایران):", color = SpectralWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = { storyHour1 = if (storyHour1 <= 0) 23 else storyHour1 - 1 },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = SpectralWhite)
                            }
                            Text(
                                text = String.format("%02d:00", storyHour1),
                                color = BloodGlow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(
                                onClick = { storyHour1 = (storyHour1 + 1) % 24 },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = SpectralWhite)
                            }
                        }
                    }
                }

                if (storyFreq == "TWICE_DAILY") {
                    // Hour Picker 2 for Stories
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ساعت نوبت دوم (زمان ایران):", color = SpectralWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(
                                    onClick = { storyHour2 = if (storyHour2 <= 0) 23 else storyHour2 - 1 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = SpectralWhite)
                                }
                                Text(
                                    text = String.format("%02d:00", storyHour2),
                                    color = BloodGlow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                IconButton(
                                    onClick = { storyHour2 = (storyHour2 + 1) % 24 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SpectralWhite)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (storyFreq == "TWICE_DAILY") {
                        "تعداد $storyBatchCount داستان هوش مصنوعی، رأس ساعت‌های ${String.format("%02d", storyHour1)}:۰۰ و ${String.format("%02d", storyHour2)}:۰۰ به وقت تهران تولید و مستقیماً منتشر می‌شوند."
                    } else {
                        "تعداد $storyBatchCount داستان هوش مصنوعی، رأس ساعت ${String.format("%02d", storyHour1)}:۰۰ به وقت تهران تولید و مستقیماً منتشر می‌شوند."
                    },
                    color = BloodGlow,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val time1Str = "${String.format("%02d", storyHour1)}:۰۰"
                    val time2Str = "${String.format("%02d", storyHour2)}:۰۰"
                    Text(
                        text = "وضعیت: " + if (storyConfig.is_active) {
                            if (storyFreq == "TWICE_DAILY") "فعال در ساعت $time1Str و $time2Str" else "فعال در ساعت $time1Str"
                        } else "غیرفعال",
                        color = if (storyConfig.is_active) SuccessNeon else WarningAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.saveAutomationConfig(
                                    storyConfig.copy(
                                        frequency = storyFreq,
                                        schedule_hour_1 = storyHour1,
                                        schedule_hour_2 = storyHour2,
                                        batch_count = storyBatchCount
                                    )
                                ) {
                                    feedbackMsg = "تنظیمات داستان‌های خودکار ذخیره شد."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ذخیره تنظیمات", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                runningTask = "auto-ai-stories"
                                viewModel.triggerEdgeFunction("auto-ai-stories") { success, msg ->
                                    runningTask = null
                                    feedbackMsg = msg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (runningTask == "auto-ai-stories") {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SpectralWhite)
                            } else {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تست تولید فوری", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // QUEUED AUTOMATIONS STATUS (Cloud Scheduler Queue)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = BloodGlow)
                    Text("صف و وضعیت اتوماسیون‌های در انتظار اجرا (Cloud Cron Queue)", fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 13.sp)
                }
                Text(
                    text = "در این بخش می‌توانید لیست وظایف برنامه‌ریزی‌شده که در سرور منتظر فرارسیدن ساعت اجرای خود هستند را مشاهده کنید.",
                    color = MutedAsh,
                    fontSize = 11.sp
                )

                // Task 1: Grim Fortunes
                Surface(
                    color = Color(0xFF1E0E2B),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("طالع شوم ۱۲ ماه (AUTO_GRIM_FORTUNES)", color = SpectralWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                if (fortuneConfig.is_active) "در صف اجرای سرور • ساعت ${String.format("%02d", fortuneConfig.schedule_hour_1)}:۰۰ ایران" else "متوقف شده",
                                color = if (fortuneConfig.is_active) SuccessNeon else MutedAsh,
                                fontSize = 11.sp
                            )
                        }
                        Badge(containerColor = if (fortuneConfig.is_active) SuccessNeon.copy(alpha = 0.2f) else MutedAsh.copy(alpha = 0.2f)) {
                            Text(
                                if (fortuneConfig.is_active) "در صف فعال" else "غیرفعال",
                                color = if (fortuneConfig.is_active) SuccessNeon else MutedAsh,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                // Task 2: AI Stories
                Surface(
                    color = Color(0xFF1E0E2B),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("داستان‌های هوش مصنوعی (AUTO_AI_STORIES)", color = SpectralWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val schedText = if (storyConfig.frequency == "TWICE_DAILY") {
                                "ساعت‌های ${String.format("%02d", storyConfig.schedule_hour_1)}:۰۰ و ${String.format("%02d", storyConfig.schedule_hour_2)}:۰۰ ایران"
                            } else {
                                "ساعت ${String.format("%02d", storyConfig.schedule_hour_1)}:۰۰ ایران"
                            }
                            Text(
                                if (storyConfig.is_active) "در صف اجرای سرور • $schedText (${storyConfig.batch_count} داستان)" else "متوقف شده",
                                color = if (storyConfig.is_active) SuccessNeon else MutedAsh,
                                fontSize = 11.sp
                            )
                        }
                        Badge(containerColor = if (storyConfig.is_active) SuccessNeon.copy(alpha = 0.2f) else MutedAsh.copy(alpha = 0.2f)) {
                            Text(
                                if (storyConfig.is_active) "در صف فعال" else "غیرفعال",
                                color = if (storyConfig.is_active) SuccessNeon else MutedAsh,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                // Copy SQL script guidance
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                var sqlCopied by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = {
                        val sqlScript = """
-- فعال‌سازی اجرای سروری pg_cron در Supabase SQL Editor
CREATE EXTENSION IF NOT EXISTS pg_cron;
CREATE EXTENSION IF NOT EXISTS pg_net;

-- زمان‌بندی بررسی اتوماسیون‌ها در هر ساعت
SELECT cron.schedule(
    'run-automations-hourly',
    '0 * * * *',
    $$ SELECT cron_run_automations(); $$
);
                        """.trimIndent()
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(sqlScript))
                        sqlCopied = true
                        feedbackMsg = "کد فعال‌سازی سروری pg_cron کپی شد! در بخش SQL Editor کنسول Supabase اجرا نمایید."
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (sqlCopied) "کد کپی شد (آماده اجرا در SQL Editor)" else "کپی دستور راه‌اندازی pg_cron سرور", color = SpectralWhite, fontSize = 11.sp)
                }
            }
        }

        if (feedbackMsg != null) {
            Surface(
                color = CryptCardElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BloodGlow.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = feedbackMsg!!,
                    color = SpectralWhite,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // ==========================================
        // AUTOMATION LOGS VIEWER
        // ==========================================
        Text("تاریخچه آخرین اجراها و لاگ‌های خودکار (${automationLogs.size})", fontWeight = FontWeight.Bold, color = SpectralWhite)

        if (automationLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("هنوز لاگی ثبت نشده است. با اجرای تست، لاگ‌ها ظاهر خواهند شد.", color = MutedAsh, fontSize = 12.sp)
            }
        } else {
            automationLogs.take(15).forEach { log ->
                val isSuccess = log.status == "SUCCESS"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isSuccess) SuccessNeon.copy(alpha = 0.3f) else BloodCrimson.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CryptCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = when (log.task_type) {
                                    "SCHEDULED_NOTIFICATIONS" -> "اعلان‌های زمان‌بندی‌شده"
                                    "AUTO_GRIM_FORTUNES" -> "تولید خودکار طالع شوم"
                                    "AUTO_SCENARIOS" -> "تولید خودکار سناریوها"
                                    else -> log.task_type
                                },
                                fontWeight = FontWeight.Bold,
                                color = SpectralWhite,
                                fontSize = 13.sp
                            )
                            Badge(containerColor = if (isSuccess) SuccessNeon else BloodCrimson) {
                                Text(if (isSuccess) "موفق" else "ناموفق", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                            }
                        }
                        Text(log.message, color = MutedAsh, fontSize = 11.sp)
                        if (log.createdAt != null) {
                            Text(log.createdAt, color = BloodGlow.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
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
            StatCard(title = "داستان‌های هوش مصنوعی", count = scenCount.toString(), icon = Icons.Default.Psychology, modifier = Modifier.weight(1f).clickable { onSwitchTab(3) })
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
                    Text("تولید داستان‌های هوش مصنوعی با پرامپت اختصاصی (تا ۲۰ عدد)", color = SpectralWhite)
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
    var selectedSubmissionIds by remember { mutableStateOf(setOf<String>()) }

    // States for User Submissions search, sorting, sub-tab status
    var userStatusTab by remember { mutableIntStateOf(0) } // 0: منتشر شده, 1: منتشر نشده
    var userSearchQuery by remember { mutableStateOf("") }
    var userSortOption by remember { mutableIntStateOf(0) } // 0: Newest, 1: Rating, 2: Alphabetical

    LaunchedEffect(realStatusTab, realSearchQuery) {
        selectedRealStoryIds = emptySet()
    }
    LaunchedEffect(userStatusTab, userSearchQuery) {
        selectedSubmissionIds = emptySet()
    }

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
            val pendingCount = submissions.count { it.status != "PUBLISHED" }
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("داستان‌های کاربران ($pendingCount در انتظار)", fontWeight = FontWeight.Bold) },
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
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (realStatusTab == 1) {
                                Button(
                                    onClick = {
                                        viewModel.publishRealStoriesBulk(selectedRealStoryIds.toList()) {
                                            selectedRealStoryIds = emptySet()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("انتشار گروهی (${selectedRealStoryIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.draftRealStoriesBulk(selectedRealStoryIds.toList()) {
                                            selectedRealStoryIds = emptySet()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Drafts, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("پیش‌نویس گروهی (${selectedRealStoryIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteRealStoriesBulk(selectedRealStoryIds.toList()) {
                                        selectedRealStoryIds = emptySet()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف (${selectedRealStoryIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

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
                            showSelection = true,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptCardElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allIds = filteredSubmissions.map { it.id }.toSet()
                    val isAllSelected = selectedSubmissionIds.size == allIds.size && allIds.isNotEmpty()
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            selectedSubmissionIds = if (isAllSelected) emptySet() else allIds
                        }
                    ) {
                        Checkbox(
                            checked = isAllSelected,
                            onCheckedChange = { checked ->
                                selectedSubmissionIds = if (checked) allIds else emptySet()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BloodCrimson, uncheckedColor = MutedAsh)
                        )
                        Text(
                            text = if (isAllSelected) "لغو انتخاب همه" else "انتخاب همه ارسالی‌های این صفحه",
                            color = SpectralWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (selectedSubmissionIds.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (userStatusTab == 1) {
                                Button(
                                    onClick = {
                                        viewModel.publishSubmissionsBulk(selectedSubmissionIds.toList()) {
                                            selectedSubmissionIds = emptySet()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("انتشار گروهی (${selectedSubmissionIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.draftSubmissionsBulk(selectedSubmissionIds.toList()) {
                                            selectedSubmissionIds = emptySet()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Drafts, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("پیش‌نویس گروهی (${selectedSubmissionIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteSubmissionsBulk(selectedSubmissionIds.toList()) {
                                        selectedSubmissionIds = emptySet()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف (${selectedSubmissionIds.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredSubmissions) { sub ->
                        AdminSubmissionCard(
                            submission = sub,
                            onPublishWithPoster = { submissionToPublish = sub },
                            onToggleStatus = {
                                val next = if (sub.status == "PUBLISHED") "PENDING" else "PUBLISHED"
                                viewModel.updateSubmissionStatus(sub.id, next, null) {}
                            },
                            onDelete = { viewModel.deleteSubmission(sub.id) {} },
                            showSelection = true,
                            isSelected = selectedSubmissionIds.contains(sub.id),
                            onSelectionChange = { checked ->
                                selectedSubmissionIds = if (checked) selectedSubmissionIds + sub.id else selectedSubmissionIds - sub.id
                            }
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
                .padding(16.dp),
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
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = submission.title, fontWeight = FontWeight.Bold, color = BloodGlow)
                    Badge(
                        containerColor = when (submission.status) {
                            "PUBLISHED" -> SuccessNeon
                            "REJECTED" -> BloodCrimson
                            else -> WarningAmber
                        }
                    ) {
                        Text(if (submission.status == "PUBLISHED") "منتشر شده" else "پیش‌نویس", color = SpectralWhite, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                    }
                }
                Text(text = "ارسال‌کننده: ${submission.author_name}", style = MaterialTheme.typography.bodySmall, color = MutedAsh)
                Text(text = submission.content, style = MaterialTheme.typography.bodyMedium, color = SpectralWhite, maxLines = 3, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (submission.status != "PUBLISHED") {
                        Button(
                            onClick = onPublishWithPoster,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتشار با پوستر", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onToggleStatus,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (submission.status == "PUBLISHED") "پیش‌نویس" else "تأیید سریع", color = SpectralWhite, fontSize = 11.sp)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(18.dp))
                    }
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
// TAB 3: AI HORROR STORIES (Generation up to 20, Published & Draft tabs, Bulk & Single Management)
// ----------------------------------------------------
@Composable
fun AdminAiStoriesTab(
    viewModel: HorrorViewModel,
    stories: List<AiStory>
) {
    var savedPrompt by remember { mutableStateOf("") }
    val currentPrompt by viewModel.aiStoryPrompt.collectAsState()
    LaunchedEffect(currentPrompt) { savedPrompt = currentPrompt }

    var storyCountToGenerate by remember { mutableIntStateOf(3) }
    var selectedGenre by remember { mutableStateOf("همه") }
    val genres = listOf("همه", "ماورایی", "روانشناختی", "افسانه ایرانی", "گوتیک", "جنایی")

    var isGeneratingAI by remember { mutableStateOf(false) }
    var genResultFeedback by remember { mutableStateOf<String?>(null) }
    var promptExpanded by remember { mutableStateOf(false) }

    // Sub-tabs: 0 = Published, 1 = Drafts
    var currentSubTab by remember { mutableIntStateOf(0) }
    val publishedStories = remember(stories) { stories.filter { it.status == "PUBLISHED" } }
    val draftStories = remember(stories) { stories.filter { it.status != "PUBLISHED" } }
    val currentDisplayList = if (currentSubTab == 0) publishedStories else draftStories

    // Multi-selection state
    val selectedIds = remember { mutableStateListOf<String>() }
    // Clear selection when switching sub-tab
    LaunchedEffect(currentSubTab) { selectedIds.clear() }

    // Dialog states
    var storyToView by remember { mutableStateOf<AiStory?>(null) }
    var storyToEdit by remember { mutableStateOf<AiStory?>(null) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var storyToDeleteConfirm by remember { mutableStateOf<AiStory?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Prompt & Model Configuration
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
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = BloodGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تنظیم پرامپت داستان‌های هوش مصنوعی", fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 13.sp)
                    }
                    Icon(
                        if (promptExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MutedAsh
                    )
                }

                if (promptExpanded) {
                    Text(
                        text = "این پرامپت به عنوان دستورالعمل دائمی برای تولید داستان‌های ترسناک هوش مصنوعی به کار می‌رود.",
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
                            onClick = { viewModel.setAiStoryPrompt(savedPrompt) },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ذخیره پرامپت", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                savedPrompt = HorrorViewModel.DEFAULT_AI_STORY_PROMPT
                                viewModel.setAiStoryPrompt(savedPrompt)
                            }
                        ) {
                            Text("بازنشانی پیش‌فرض", color = MutedAsh, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // AI Generator Action Box (Up to 20 stories)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BloodCrimson.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CryptCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "تولید همزمان داستان با هوش مصنوعی (تا ۲۰ عدد)", fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 14.sp)
                    Badge(containerColor = BloodCrimson) {
                        Text("انتشار مستقیم در لیست", color = SpectralWhite, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                    }
                }

                // Count Selector (1 to 20)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تعداد داستان‌های درخواستی:", color = MutedAsh, fontSize = 12.sp)
                        Text("$storyCountToGenerate عدد", color = BloodGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 3, 5, 10, 15, 20).forEach { count ->
                            val isSel = storyCountToGenerate == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BloodCrimson else CryptCardElevated)
                                    .clickable { storyCountToGenerate = count }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$count", color = if (isSel) SpectralWhite else MutedAsh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Genre Filter Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ژانر داستان:", color = MutedAsh, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.forEach { g ->
                            val isSel = selectedGenre == g
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF3B184F) else CryptCardElevated)
                                    .border(1.dp, if (isSel) BloodGlow else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedGenre = g }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(g, color = if (isSel) SpectralWhite else MutedAsh, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        isGeneratingAI = true
                        genResultFeedback = null
                        viewModel.generateAiStoriesWithAI(
                            customPrompt = savedPrompt,
                            count = storyCountToGenerate,
                            genre = if (selectedGenre == "همه") null else selectedGenre
                        ) { success, msg, count ->
                            isGeneratingAI = false
                            genResultFeedback = msg
                            if (success) {
                                currentSubTab = 0 // Show Published tab where newly generated stories reside
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGeneratingAI
                ) {
                    if (isGeneratingAI) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SpectralWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال نگارش صف $storyCountToGenerate داستان هولناک...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تولید صف $storyCountToGenerate داستان با هوش مصنوعی")
                    }
                }

                val queueState by viewModel.aiGenQueueState.collectAsState()
                if (queueState.isGenerating || queueState.completedCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14081E)),
                        border = BorderStroke(1.dp, BloodGlow),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("وضعیت صف تولید داستان‌ها:", color = SpectralWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${queueState.completedCount} از ${queueState.totalRequested}", color = Color(0xFFDEC595), fontSize = 11.sp)
                            }
                            LinearProgressIndicator(
                                progress = { if (queueState.totalRequested > 0) queueState.completedCount.toFloat() / queueState.totalRequested else 0f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = BloodCrimson,
                                trackColor = Color(0xFF261238)
                            )
                            Text(
                                text = queueState.statusMessage,
                                color = MutedAsh,
                                fontSize = 10.sp
                            )
                        }
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

        // Sub-tabs: Published vs Drafts
        Surface(
            color = CryptCard,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Published Tab Button
                val isPub = currentSubTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPub) BloodCrimson else Color.Transparent)
                        .clickable { currentSubTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isPub) SpectralWhite else MutedAsh, modifier = Modifier.size(16.dp))
                        Text("منتشر شده (${publishedStories.size})", color = if (isPub) SpectralWhite else MutedAsh, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Draft Tab Button
                val isDraft = currentSubTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDraft) BloodCrimson else Color.Transparent)
                        .clickable { currentSubTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = if (isDraft) SpectralWhite else MutedAsh, modifier = Modifier.size(16.dp))
                        Text("پیش‌نویس‌ها (${draftStories.size})", color = if (isDraft) SpectralWhite else MutedAsh, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Action & Selection Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Select All / Deselect All
                OutlinedButton(
                    onClick = {
                        if (selectedIds.size == currentDisplayList.size && currentDisplayList.isNotEmpty()) {
                            selectedIds.clear()
                        } else {
                            selectedIds.clear()
                            selectedIds.addAll(currentDisplayList.map { it.id })
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (selectedIds.size == currentDisplayList.size && currentDisplayList.isNotEmpty()) "لغو انتخاب همه" else "انتخاب همه",
                        fontSize = 11.sp,
                        color = SpectralWhite
                    )
                }

                if (selectedIds.isNotEmpty()) {
                    Text("${selectedIds.size} مورد انتخاب شده", color = BloodGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Manual Add Button
            OutlinedButton(
                onClick = { showManualAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن دستی", fontSize = 11.sp)
            }
        }

        // Bulk Actions Bar (Shown when 1 or more items selected)
        if (selectedIds.isNotEmpty()) {
            Surface(
                color = Color(0xFF2A1015),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BloodGlow.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عملیات گروهی روی ${selectedIds.size} داستان:", color = SpectralWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentSubTab == 0) {
                            // In Published: Move to Draft (Bulk)
                            Button(
                                onClick = {
                                    viewModel.bulkUpdateAiStoriesStatus(selectedIds.toList(), "DRAFT") { success, msg ->
                                        selectedIds.clear()
                                        genResultFeedback = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CryptCardElevated),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("پیشنویس گروهی", fontSize = 11.sp)
                            }
                        } else {
                            // In Drafts: Publish (Bulk)
                            Button(
                                onClick = {
                                    viewModel.bulkUpdateAiStoriesStatus(selectedIds.toList(), "PUBLISHED") { success, msg ->
                                        selectedIds.clear()
                                        genResultFeedback = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp), tint = VoidBlack)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("انتشار گروهی", fontSize = 11.sp, color = VoidBlack, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Bulk Delete Button
                        Button(
                            onClick = { showBulkDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف گروهی", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Stories List
        if (currentDisplayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentSubTab == 0) "هیچ داستان منتشر شده‌ای وجود ندارد." else "هیچ داستان پیش‌نویسی وجود ندارد.",
                    color = MutedAsh,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentDisplayList.forEach { story ->
                    val isChecked = selectedIds.contains(story.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isChecked) BloodGlow else MutedAsh.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = CryptCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIds.add(story.id) else selectedIds.remove(story.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = BloodCrimson,
                                            uncheckedColor = MutedAsh
                                        )
                                    )
                                    Column {
                                        Text(text = story.title, fontWeight = FontWeight.Bold, color = SpectralWhite, fontSize = 13.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("ژانر: ${story.genre}", color = BloodGlow, fontSize = 10.sp)
                                            Text("•", color = MutedAsh, fontSize = 10.sp)
                                            Text("وحشت: ${story.doom_score}%", color = WarningAmber, fontSize = 10.sp)
                                            Text("•", color = MutedAsh, fontSize = 10.sp)
                                            Text("بازدید: ${story.view_count}", color = MutedAsh, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Badge(containerColor = if (story.status == "PUBLISHED") SuccessNeon.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f)) {
                                    Text(
                                        text = if (story.status == "PUBLISHED") "منتشر شده" else "پیش‌نویس",
                                        color = if (story.status == "PUBLISHED") SuccessNeon else WarningAmber,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }

                            Text(
                                text = story.synopsis?.ifBlank { story.content } ?: story.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedAsh,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )

                            // Action buttons (Single)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Full view button
                                TextButton(
                                    onClick = { storyToView = story },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = SpectralWhite, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مشاهده کامل", color = SpectralWhite, fontSize = 11.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (story.status == "PUBLISHED") {
                                        // Single Draft button
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateAiStoryStatus(story.id, "DRAFT") { success, msg ->
                                                    genResultFeedback = msg
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("پیش‌نویس", fontSize = 11.sp)
                                        }
                                    } else {
                                        // In Draft: Single Edit button
                                        OutlinedButton(
                                            onClick = { storyToEdit = story },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ویرایش", fontSize = 11.sp)
                                        }

                                        // In Draft: Single Publish button
                                        Button(
                                            onClick = {
                                                viewModel.updateAiStoryStatus(story.id, "PUBLISHED") { success, msg ->
                                                    genResultFeedback = msg
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessNeon),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp), tint = VoidBlack)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("انتشار", fontSize = 11.sp, color = VoidBlack, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Single Delete button
                                    IconButton(
                                        onClick = { storyToDeleteConfirm = story },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // DIALOG: FULL STORY READER
    // ----------------------------------------------------
    if (storyToView != null) {
        val s = storyToView!!
        AlertDialog(
            onDismissRequest = { storyToView = null },
            containerColor = CryptCardElevated,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(s.title, color = BloodGlow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(containerColor = Color(0xFF3B184F)) {
                            Text(s.genre ?: "روانشناختی", color = SpectralWhite, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                        }
                        Text("میزان وحشت: ${s.doom_score}%", color = WarningAmber, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!s.synopsis.isNullOrBlank()) {
                        Text(
                            text = "خلاصه: ${s.synopsis}",
                            color = MutedAsh,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Divider(color = MutedAsh.copy(alpha = 0.2f))
                    }
                    Text(
                        text = s.content,
                        color = SpectralWhite,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { storyToView = null }) {
                    Text("بستن", color = SpectralWhite)
                }
            }
        )
    }

    // ----------------------------------------------------
    // DIALOG: SINGLE STORY EDIT (For Drafts)
    // ----------------------------------------------------
    if (storyToEdit != null) {
        val target = storyToEdit!!
        var editTitle by remember { mutableStateOf(target.title) }
        var editGenre by remember { mutableStateOf(target.genre ?: "") }
        var editSynopsis by remember { mutableStateOf(target.synopsis ?: "") }
        var editContent by remember { mutableStateOf(target.content) }
        var editDoom by remember { mutableIntStateOf(target.doom_score) }

        AlertDialog(
            onDismissRequest = { storyToEdit = null },
            containerColor = CryptCardElevated,
            title = { Text("ویرایش داستان پیش‌نویس", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("عنوان داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editGenre,
                        onValueChange = { editGenre = it },
                        label = { Text("ژانر") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editSynopsis,
                        onValueChange = { editSynopsis = it },
                        label = { Text("خلاصه کوتاه") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("متن کامل داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 8,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = target.copy(
                            title = editTitle,
                            genre = editGenre,
                            synopsis = editSynopsis,
                            content = editContent
                        )
                        viewModel.updateAiStory(updated) { success, msg ->
                            storyToEdit = null
                            genResultFeedback = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson)
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { storyToEdit = null }) {
                    Text("انصراف", color = MutedAsh)
                }
            }
        )
    }

    // ----------------------------------------------------
    // DIALOG: MANUAL ADD STORY
    // ----------------------------------------------------
    if (showManualAddDialog) {
        var addTitle by remember { mutableStateOf("") }
        var addGenre by remember { mutableStateOf("ماورایی") }
        var addSynopsis by remember { mutableStateOf("") }
        var addContent by remember { mutableStateOf("") }
        var addStatus by remember { mutableStateOf("PUBLISHED") }

        AlertDialog(
            onDismissRequest = { showManualAddDialog = false },
            containerColor = CryptCardElevated,
            title = { Text("افزودن دستی داستان هوش مصنوعی", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = addTitle,
                        onValueChange = { addTitle = it },
                        label = { Text("عنوان داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = addGenre,
                        onValueChange = { addGenre = it },
                        label = { Text("ژانر") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = addSynopsis,
                        onValueChange = { addSynopsis = it },
                        label = { Text("خلاصه کوتاه") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = addContent,
                        onValueChange = { addContent = it },
                        label = { Text("متن کامل داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("وضعیت اولیه:", color = MutedAsh, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (addStatus == "PUBLISHED") BloodCrimson else CryptCard)
                                    .clickable { addStatus = "PUBLISHED" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("انتشار مستقیم", color = SpectralWhite, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (addStatus == "DRAFT") BloodCrimson else CryptCard)
                                    .clickable { addStatus = "DRAFT" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("پیش‌نویس", color = SpectralWhite, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addTitle.isNotBlank() && addContent.isNotBlank()) {
                            val newStory = AiStory(
                                id = java.util.UUID.randomUUID().toString(),
                                title = addTitle,
                                content = addContent,
                                synopsis = addSynopsis.ifBlank { addContent.take(120) },
                                genre = addGenre,
                                cover_image_url = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=600&auto=format&fit=crop&q=80",
                                tags = "ثبت دستی ادمین",
                                status = addStatus,
                                view_count = 0,
                                rating = 5.0f,
                                rating_count = 1,
                                createdAt = null,
                                updatedAt = null
                            )
                            viewModel.updateAiStory(newStory) { success, msg ->
                                showManualAddDialog = false
                                genResultFeedback = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    enabled = addTitle.isNotBlank() && addContent.isNotBlank()
                ) {
                    Text("ذخیره داستان")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddDialog = false }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }

    // ----------------------------------------------------
    // DIALOG: SINGLE DELETE CONFIRMATION
    // ----------------------------------------------------
    if (storyToDeleteConfirm != null) {
        val s = storyToDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { storyToDeleteConfirm = null },
            containerColor = CryptCardElevated,
            title = { Text("تأیید حذف داستان", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف داستان «${s.title}» اطمینان دارید؟ این عملیات غیرقابل بازگشت است.", color = SpectralWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAiStory(s.id) { success, msg ->
                            storyToDeleteConfirm = null
                            genResultFeedback = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson)
                ) {
                    Text("بله، حذف کن")
                }
            },
            dismissButton = {
                TextButton(onClick = { storyToDeleteConfirm = null }) { Text("انصراف", color = MutedAsh) }
            }
        )
    }

    // ----------------------------------------------------
    // DIALOG: BULK DELETE CONFIRMATION
    // ----------------------------------------------------
    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            containerColor = CryptCardElevated,
            title = { Text("تأیید حذف گروهی", color = BloodGlow, fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف همزمان ${selectedIds.size} داستان انتخاب شده اطمینان دارید؟", color = SpectralWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bulkDeleteAiStories(selectedIds.toList()) { success, msg ->
                            selectedIds.clear()
                            showBulkDeleteConfirm = false
                            genResultFeedback = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson)
                ) {
                    Text("بله، حذف همه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) { Text("انصراف", color = MutedAsh) }
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
                                        "gemini-3.7-flash" -> "مدل 3.7 Flash - پیشرفته‌ترین و قدرتمندترین مدل هوش مصنوعی"
                                        "gemini-3.6-flash" -> "مدل 3.6 Flash - بهینه‌شده برای سرعت و تحلیل پیشرفته"
                                        "gemini-3.5-flash" -> "مدل 3.5 Flash - پرسرعت و متعادل برای تولید روایات و سناریو"
                                        "gemini-3.5-flash-lite" -> "مدل 3.5 Flash Lite - بسیار سبک، سریع و کاملاً مناسب کلید رایگان"
                                        "gemini-3.1-flash-lite" -> "مدل 3.1 Flash Lite - فوق‌العاده سریع با حداقل مصرف توکن"
                                        else -> "مدل هوش مصنوعی گوگل"
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

@Composable
fun AdminReportsTab(viewModel: HorrorViewModel, onOpenStory: (RealStory) -> Unit) {
    val reports by viewModel.storyReports.collectAsState()
    val context = LocalContext.current
    var selectedReports by remember { mutableStateOf(setOf<String>()) }
    var isDeletingAll by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadStoryReports()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "گزارش‌های کاربران",
                    style = MaterialTheme.typography.titleLarge.copy(color = BloodGlow, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${reports.size} مورد گزارش نامناسب دریافت شده",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedAsh)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Refresh Button
                IconButton(
                    onClick = { viewModel.loadStoryReports() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(DeepCrypt, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی", tint = SpectralWhite)
                }

                // Bulk Delete button (if any selected)
                if (selectedReports.isNotEmpty()) {
                    Button(
                        onClick = {
                            isDeletingAll = true
                            viewModel.deleteStoryReportsBulk(selectedReports.toList()) { success ->
                                isDeletingAll = false
                                if (success) {
                                    android.widget.Toast.makeText(context, "گزارش‌های انتخاب شده حذف شدند.", android.widget.Toast.LENGTH_SHORT).show()
                                    selectedReports = emptySet()
                                } else {
                                    android.widget.Toast.makeText(context, "خطا در حذف برخی گزارش‌ها.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodGlow),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isDeletingAll,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (isDeletingAll) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("حذف گروهی (${selectedReports.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MutedAsh.copy(alpha = 0.2f))

        if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                    Text("هیچ گزارش جدیدی ثبت نشده است.", color = SpectralWhite, fontSize = 13.sp)
                    Text("تمامی لوح‌ها عاری از هرگونه آلودگی و محتوای نامناسب هستند.", color = MutedAsh, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    val isSelected = selectedReports.contains(report.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BloodGlow else DeepCrypt.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected) {
                                    selectedReports = selectedReports - report.id
                                } else {
                                    selectedReports = selectedReports + report.id
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = DeepCrypt.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked == true) {
                                                selectedReports = selectedReports + report.id
                                            } else {
                                                selectedReports = selectedReports - report.id
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = BloodGlow,
                                            checkmarkColor = Color.White,
                                            uncheckedColor = MutedAsh
                                        )
                                    )
                                    Text(
                                        text = report.story_title,
                                        color = SpectralWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = if (report.story_type == "USER") "روایت کاربر" else "داستان اصلی",
                                    color = if (report.story_type == "USER") Color(0xFFDEC595) else BloodGlow,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .background(
                                            color = (if (report.story_type == "USER") Color(0xFFDEC595) else BloodGlow).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "نویسنده/راوی: ${report.story_author}",
                                color = MutedAsh,
                                fontSize = 11.sp
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VoidBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, MutedAsh.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "علت گزارش: ${report.reason}",
                                    color = Color(0xFFEDE8F5),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Link to story button
                                Button(
                                    onClick = {
                                        val subStories = viewModel.adminSubmissions.value
                                        val realStories = viewModel.adminRealStories.value
                                        val foundStory = if (report.story_type == "USER") {
                                            subStories.find { it.id == report.story_id }?.toRealStory()
                                        } else {
                                            realStories.find { it.id == report.story_id }
                                        }
                                        if (foundStory != null) {
                                            onOpenStory(foundStory)
                                        } else {
                                            onOpenStory(
                                                RealStory(
                                                    id = report.story_id,
                                                    title = report.story_title,
                                                    content = "این داستان مستقیماً یافت نشد. ممکن است از پایگاه داده حذف شده باشد.",
                                                    author = report.story_author,
                                                    source = "گزارش خطا",
                                                    status = "PUBLISHED"
                                                )
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1135)),
                                    border = BorderStroke(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مشاهده داستان مرتبط", color = Color(0xFFDEC595), fontSize = 10.5.sp)
                                }

                                // Delete single report
                                TextButton(
                                    onClick = {
                                        viewModel.deleteStoryReport(report.id) { success ->
                                            if (success) {
                                                android.widget.Toast.makeText(context, "گزارش حذف شد.", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "خطا در حذف گزارش.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BloodGlow)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رد گزارش (حذف)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
