package com.example.ui.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RealStory
import com.example.data.TimeMirrorContent
import com.example.data.WrongChoiceScenario
import com.example.ui.theme.*
import com.example.viewmodel.HorrorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(viewModel: HorrorViewModel, onOpenAdminLogin: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var logoTapCount by remember { mutableIntStateOf(0) }

    val timeMirrors by viewModel.timeMirrorList.collectAsState()
    val realStories by viewModel.realStoriesList.collectAsState()
    val scenarios by viewModel.scenariosList.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                logoTapCount++
                                if (logoTapCount >= 7) {
                                    logoTapCount = 0
                                    onOpenAdminLogin()
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.radialGradient(listOf(BloodCrimson, DeepCrypt)),
                                    CircleShape
                                )
                                .border(1.dp, BloodGlow.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = SpectralWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "وحشت فارسی",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = SpectralWhite,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoidBlack)
            )
        },
        bottomBar = {
            Surface(
                color = DeepCrypt,
                tonalElevation = 12.dp,
                modifier = Modifier.border(1.dp, CryptBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CinematicNavTab(
                        title = "تالار اصلی",
                        icon = Icons.Default.Home,
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    CinematicNavTab(
                        title = "آینه زمان",
                        icon = Icons.Default.HourglassEmpty,
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    CinematicNavTab(
                        title = "انتخاب اشتباه",
                        icon = Icons.Default.Warning,
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    CinematicNavTab(
                        title = "کتابخانه ارواح",
                        icon = Icons.Default.MenuBook,
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VoidBlack, DeepCrypt, VoidBlack)
                    )
                )
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BloodGlow
                )
            } else {
                when (selectedTab) {
                    0 -> HomeDashboardSection(
                        onNavigateTimeMirror = { selectedTab = 1 },
                        onNavigateWrongChoice = { selectedTab = 2 },
                        onNavigateStories = { selectedTab = 3 },
                        timeMirrors = timeMirrors,
                        realStories = realStories
                    )
                    1 -> TimeMirrorSection(timeMirrors)
                    2 -> WrongChoiceSection(scenarios)
                    3 -> StoriesSection(realStories, viewModel)
                }
            }
        }
    }
}

@Composable
fun CinematicNavTab(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    val containerBg = if (selected) CryptCardElevated else Color.Transparent
    val contentColor = if (selected) BloodGlow else MutedAsh
    val borderColor = if (selected) CryptBorder else Color.Transparent

    Surface(
        onClick = onClick,
        color = containerBg,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun HomeDashboardSection(
    onNavigateTimeMirror: () -> Unit,
    onNavigateWrongChoice: () -> Unit,
    onNavigateStories: () -> Unit,
    timeMirrors: List<TimeMirrorContent>,
    realStories: List<RealStory>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Immersive Cinematic Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CryptCardElevated, DeepCrypt, VoidBlack),
                            radius = 800f
                        )
                    )
                    .border(1.dp, CryptBorder, RoundedCornerShape(32.dp))
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(VoidBlack, CircleShape)
                            .border(2.dp, BloodCrimson, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = BloodGlow,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "به تالار وحشت و ناشناخته‌ها خوش آمدید",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = SpectralWhite,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "مرز میان واقعیت و کابوس بسیار باریک است. از میان آینه‌ها عبور کنید و سرنوشت تاریک خود را رقم بزنید.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 26.sp),
                        color = MutedAsh,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Text(
                text = "دروازه‌های کابوس",
                style = MaterialTheme.typography.titleLarge.copy(color = SpectralWhite, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardFeatureCard(
                    title = "آینه زمان",
                    description = "کشف حوادث تاریک تاریخ",
                    icon = Icons.Default.HourglassEmpty,
                    accentColor = BloodGlow,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateTimeMirror
                )
                DashboardFeatureCard(
                    title = "انتخاب اشتباه",
                    description = "سناریوهای مرگبار تعاملی",
                    icon = Icons.Default.Warning,
                    accentColor = WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateWrongChoice
                )
            }
        }

        item {
            DashboardFeatureCard(
                title = "کتابخانه ارواح و اعترافات",
                description = "آرشیو داستان‌های واقعی و اعترافات مرموز کاربران",
                icon = Icons.Default.MenuBook,
                accentColor = GhostlyViolet,
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateStories
            )
        }

        item {
            if (timeMirrors.isNotEmpty()) {
                val featured = timeMirrors.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CryptBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = CryptCard),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "روایت ویژه امروز",
                                style = MaterialTheme.typography.labelLarge.copy(color = BloodGlow)
                            )
                            Text(
                                text = featured.date_key,
                                style = MaterialTheme.typography.labelLarge.copy(color = MutedAsh)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = featured.title,
                            style = MaterialTheme.typography.titleLarge.copy(color = SpectralWhite)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = featured.narrative,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                            color = MutedAsh,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, CryptBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CryptCard),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(VoidBlack, CircleShape)
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(color = SpectralWhite, fontSize = 16.sp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MutedAsh
            )
        }
    }
}

@Composable
fun TimeMirrorSection(timeMirrors: List<TimeMirrorContent>) {
    var selectedDate by remember { mutableStateOf("1405-06-08") }
    var searchResult by remember {
        mutableStateOf<TimeMirrorContent?>(timeMirrors.firstOrNull())
    }

    LaunchedEffect(timeMirrors) {
        if (searchResult == null && timeMirrors.isNotEmpty()) {
            searchResult = timeMirrors.first()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CryptCard, DeepCrypt, CryptCardElevated)
                        )
                    )
                    .border(1.dp, CryptBorder, RoundedCornerShape(28.dp))
                    .padding(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(VoidBlack, CircleShape)
                            .border(1.5.dp, BloodGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "آینه زمان مجهول",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = SpectralWhite,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تاریخ تاریک مورد نظر خود را وارد کنید تا روایتی از اعماق تاریخ احضار شود.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedAsh,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("تاریخ (مثال: 1405-06-08)", color = MutedAsh) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = CryptBorder,
                            focusedLabelColor = BloodGlow,
                            cursorColor = BloodGlow,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val found = timeMirrors.find { it.date_key.contains(selectedDate) } ?: timeMirrors.firstOrNull()
                            searchResult = found
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("خیره شدن در آینه", style = MaterialTheme.typography.titleLarge.copy(color = SpectralWhite, fontSize = 16.sp))
                    }
                }
            }
        }

        item {
            if (searchResult != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CryptBorder, RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = CryptCard),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = searchResult!!.title,
                                style = MaterialTheme.typography.headlineMedium.copy(color = BloodGlow)
                            )
                            Surface(
                                color = BloodCrimson.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BloodCrimson)
                            ) {
                                Text(
                                    text = searchResult!!.date_key,
                                    color = SpectralWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = CryptBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = searchResult!!.narrative,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = SpectralWhite,
                                lineHeight = 32.sp
                            )
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ روایتی برای این تاریخ یافت نشد.", color = MutedAsh, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun WrongChoiceSection(scenarios: List<WrongChoiceScenario>) {
    var activeScenario by remember { mutableStateOf<WrongChoiceScenario?>(null) }

    if (activeScenario == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CryptCardElevated, CircleShape)
                        .border(1.dp, BloodCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "انتخاب اشتباه",
                        style = MaterialTheme.typography.headlineMedium.copy(color = SpectralWhite)
                    )
                    Text(
                        text = "تصمیمی بگیرید که عواقب آن تا ابد با شما خواهد ماند",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedAsh
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (scenarios.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ سناریویی یافت نشد.", color = MutedAsh)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(scenarios) { sc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CryptBorder, RoundedCornerShape(24.dp))
                                .clickable { activeScenario = sc },
                            colors = CardDefaults.cardColors(containerColor = CryptCard),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = sc.title,
                                    style = MaterialTheme.typography.titleLarge.copy(color = BloodGlow)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = sc.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedAsh,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("شروع کابوس", color = SpectralWhite, style = MaterialTheme.typography.labelLarge)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BloodGlow, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        InteractiveGamePlay(scenario = activeScenario!!) {
            activeScenario = null
        }
    }
}

@Composable
fun InteractiveGamePlay(scenario: WrongChoiceScenario, onBack: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    var endingState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = SpectralWhite)
                }
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleLarge.copy(color = BloodGlow)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (endingState) {
                            "DEAD" -> BloodCrimson
                            "SURVIVED" -> SuccessNeon
                            else -> CryptBorder
                        },
                        RoundedCornerShape(28.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CryptCardElevated),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    if (endingState != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    when (endingState) {
                                        "DEAD" -> BloodCrimson
                                        "SURVIVED" -> SuccessNeon
                                        else -> CryptCard
                                    },
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (endingState) {
                                    "SURVIVED" -> "پایان: زنده ماندید (نجات‌یافته)"
                                    "DEAD" -> "پایان: مرگ در سیاه‌چال تاریکی"
                                    else -> "پایان: اسرار ناشناخته"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(color = SpectralWhite)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = when (endingState) {
                                "SURVIVED" -> "با تصمیمی هوشمندانه از لبه‌ی پرتگاه مرگ بازگشتید، اما سایه‌ها همچنان ناظرتان هستند..."
                                "DEAD" -> "انتخاب شما مرگبار بود. جسم و روحتان در این تالار تاریک جاودانه شد!"
                                else -> "راز این تالار ابدی باقی خواهد ماند..."
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                            color = SpectralWhite,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "مرحله تصمیم‌گیری شماره $currentStep",
                            style = MaterialTheme.typography.labelLarge,
                            color = BloodGlow
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (currentStep == 1) scenario.description else "صدای قدم‌هایی سنگین از انتهای راهرو شنیده می‌شود. نور فانوس رو به خاموشی است. چه واکنشی نشان می‌دهید؟",
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 32.sp),
                            color = SpectralWhite
                        )
                    }
                }
            }
        }

        if (endingState == null) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Button(
                    onClick = { currentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CryptBorder)
                ) {
                    Text("مسیر سمت راست (پناه گرفتن در سکوت مطلق)", style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp))
                }
                Button(
                    onClick = { currentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CryptBorder)
                ) {
                    Text("فرار شتاب‌زده به سوی درگاه خروجی", style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp))
                }
                Button(
                    onClick = { endingState = listOf("SURVIVED", "DEAD", "MYSTERY").random() },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("روبه‌رو شدن بی‌محابا با منبع تاریکی", style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, color = SpectralWhite))
                }
            }
        } else {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("بازگشت به فهرست سناریوها", style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, color = SpectralWhite))
            }
        }
    }
}

@Composable
fun StoriesSection(realStories: List<RealStory>, viewModel: HorrorViewModel) {
    var storyTab by remember { mutableIntStateOf(0) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "کتابخانه ارواح",
                    style = MaterialTheme.typography.headlineMedium.copy(color = SpectralWhite)
                )
                Text(
                    text = "آرشیو داستان‌های واقعی و اعترافات تاریک",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedAsh
                )
            }
            Button(
                onClick = { showSubmitDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ارسال داستان", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = CryptCard,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CryptBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isTab0 = storyTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTab0) CryptCardElevated else Color.Transparent)
                        .clickable { storyTab = 0 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "داستان‌های واقعی",
                        color = if (isTab0) BloodGlow else MutedAsh,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp)
                    )
                }
                val isTab1 = storyTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTab1) CryptCardElevated else Color.Transparent)
                        .clickable { storyTab = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اعترافات شما",
                        color = if (isTab1) BloodGlow else MutedAsh,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (storyTab == 0) {
            if (realStories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ داستان واقعی منتشر نشده‌ای یافت نشد.", color = MutedAsh)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(realStories) { story ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CryptBorder, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = CryptCard),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = story.title,
                                    style = MaterialTheme.typography.titleLarge.copy(color = BloodGlow)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (!story.author.isNullOrBlank()) {
                                    Text(
                                        text = "نویسنده / منبع: ${story.author}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedAsh
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = story.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = SpectralWhite,
                                        lineHeight = 28.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اعترافات و داستان‌های ارسالی شما پس از بررسی و تأیید ادمین در پایگاه داده Supabase نمایش داده می‌شوند.",
                    textAlign = TextAlign.Center,
                    color = MutedAsh,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
                )
            }
        }
    }

    if (showSubmitDialog) {
        SubmitStoryDialog(viewModel) {
            showSubmitDialog = false
        }
    }
}

@Composable
fun SubmitStoryDialog(viewModel: HorrorViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CryptCardElevated,
        title = { Text("ارسال اعتراف یا داستان جدید", color = BloodGlow, style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (submitted) {
                    Text(
                        "داستان شما با موفقیت در پایگاه داده Supabase ثبت شد و در صف بررسی ادمین قرار گرفت.",
                        color = SpectralWhite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = CryptBorder,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("متن کامل داستان یا اعتراف") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = CryptBorder,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("نام مستعار شما") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodGlow,
                            unfocusedBorderColor = CryptBorder,
                            focusedTextColor = SpectralWhite,
                            unfocusedTextColor = SpectralWhite
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (!submitted) {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.submitUserStory(title, content, author.ifBlank { "ناشناس" }) { success ->
                                if (success) submitted = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ثبت در دیتابیس", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson), shape = RoundedCornerShape(12.dp)) {
                    Text("بستن")
                }
            }
        },
        dismissButton = {
            if (!submitted) {
                TextButton(onClick = onDismiss) { Text("انصراف", color = MutedAsh) }
            }
        }
    )
}
