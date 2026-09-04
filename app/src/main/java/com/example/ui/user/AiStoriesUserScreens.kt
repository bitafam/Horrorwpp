package com.example.ui.user

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiStory
import com.example.util.HorrorSoundManager
import com.example.viewmodel.HorrorViewModel

// ==========================================
// USER SECTION: AI STORIES (داستان‌های هوش مصنوعی)
// ==========================================

@Composable
fun AiStoriesUserSection(
    aiStories: List<AiStory>,
    viewModel: HorrorViewModel,
    onStoryRead: (AiStory) -> Unit,
    onBack: () -> Unit
) {
    val publishedStories = remember(aiStories) {
        aiStories.filter { it.status == "PUBLISHED" }
    }

    var selectedGenre by remember { mutableStateOf("همه") }
    var selectedSort by remember { mutableStateOf("جدیدترین") }
    var searchQuery by remember { mutableStateOf("") }

    val genres = listOf("همه", "ماورایی", "روانشناختی", "افسانه ایرانی", "گوتیک", "جنایی", "کیهانی", "اساطیری")
    val sortOptions = listOf("جدیدترین", "بیشترین وحشت", "محبوب‌ترین")

    val filteredStories = remember(publishedStories, selectedGenre, selectedSort, searchQuery) {
        var list = publishedStories

        if (selectedGenre != "همه") {
            list = list.filter { it.genre?.contains(selectedGenre, ignoreCase = true) == true }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                (it.synopsis?.lowercase()?.contains(q) == true) ||
                it.content.lowercase().contains(q)
            }
        }

        when (selectedSort) {
            "بیشترین وحشت" -> list.sortedByDescending { it.doomScore }
            "محبوب‌ترین" -> list.sortedWith(compareByDescending<AiStory> { it.ratingScore }.thenByDescending { it.ratingCount })
            else -> list.sortedByDescending { it.createdAt ?: "" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040208))
    ) {
        // TOP APP BAR
        GamingTopBar(
            title = "داستان‌های هوش مصنوعی",
            subtitle = "روایت‌های سیاه و هولناک Gemini AI",
            icon = Icons.Default.Psychology,
            badgeText = "${publishedStories.size} قصه",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SEARCH & CONTROLS HEADER
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search text field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("جستجو در قصه‌های هوش مصنوعی...", color = Color(0xFF7A6B88), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFDEC595)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "پاک کردن", tint = Color(0xFFDEC595))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF100A1A),
                            unfocusedContainerColor = Color(0xFF0C0714),
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2C1E3C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Genre Selector Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(genres) { g ->
                            val isSel = g == selectedGenre
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSel) Color(0xFFB8143F) else Color(0xFF140D20))
                                    .border(
                                        1.dp,
                                        if (isSel) Color(0xFFDEC595) else Color(0xFF2A1C3C),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        HorrorSoundManager.playScenarioChoiceSound()
                                        selectedGenre = g
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = g,
                                    color = if (isSel) Color.White else Color(0xFFC7BCCF),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Sort Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مرتب‌سازی براساس:",
                            color = Color(0xFF9E8DA8),
                            fontSize = 11.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            sortOptions.forEach { sort ->
                                val isSel = sort == selectedSort
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF4A0A17) else Color(0xFF120A1C))
                                        .border(
                                            1.dp,
                                            if (isSel) Color(0xFFFF1E56) else Color(0xFF251535),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedSort = sort
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = sort,
                                        color = if (isSel) Color(0xFFFFD700) else Color(0xFF8D7C98),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // EMPTY STATE
            if (filteredStories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF100818)),
                        border = BorderStroke(1.dp, Color(0xFF331B42)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFFB8143F),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "هیچ داستانی با این مشخصات یافت نشد",
                                color = Color(0xFFDEC595),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (publishedStories.isEmpty())
                                    "هنوز داستانی توسط هوش مصنوعی منتشر نشده است. ادمین می‌تواند از پنل مدیریت داستان‌های اختصاصی جدید جنریت کند."
                                else
                                    "با تغییر فیلترها یا جستجوی عبارت دیگر، داستان‌های دیگری را پیدا کنید.",
                                color = Color(0xFF8B8496),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                // STORIES LIST
                items(filteredStories, key = { it.id }) { story ->
                    UserAiStoryCard(
                        story = story,
                        onRead = {
                            HorrorSoundManager.playScenarioChoiceSound()
                            onStoryRead(story)
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// USER CARD: AI STORY
// ==========================================

@Composable
private fun UserAiStoryCard(
    story: AiStory,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFB8143F).copy(alpha = 0.7f), Color(0xFF1C0D2E))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onRead),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0719)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HEADER BADGES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Genre Badge
                Surface(
                    color = Color(0xFF280F38),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF6B2B85).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "🔮 ${story.genre}",
                        color = Color(0xFFD6A2E8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Doom Score Badge
                val doomColor = when {
                    story.doomScore >= 80 -> Color(0xFFFF1E56)
                    story.doomScore >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
                Surface(
                    color = doomColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, doomColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💀", fontSize = 10.sp)
                        Text(
                            text = "وحشت: ${story.doomScore}٪",
                            color = doomColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // TITLE
            Text(
                text = story.title,
                color = Color(0xFFDEC595),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // SYNOPSIS
            val syn = story.synopsis
            if (!syn.isNullOrBlank()) {
                Text(
                    text = syn,
                    color = Color(0xFFC7BED3),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = Color(0xFF201330), thickness = 1.dp)

            // FOOTER: Views + Rating + Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = String.format("%.1f", story.ratingScore),
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (story.ratingCount > 0) {
                            Text(
                                text = "(${story.ratingCount})",
                                color = Color(0xFF8B8496),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Views
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF8B8496),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${story.viewsCount}",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp
                        )
                    }
                }

                // Read Button
                Surface(
                    color = Color(0xFFB8143F),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "مطالعه قصه",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// READER SCREEN: AI STORY READER
// ==========================================

@Composable
fun AiStoryReaderScreen(
    story: AiStory,
    viewModel: HorrorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Record view increment automatically
    LaunchedEffect(story.id) {
        viewModel.incrementAiStoryViews(story.id)
    }

    var fontSize by remember { mutableFloatStateOf(16f) }
    var userRating by remember { mutableIntStateOf(0) }
    var ratingSubmitted by remember { mutableStateOf(false) }

    // Themes: 0 = Crypt Dark, 1 = Ancient Parchment, 2 = Pitch Black
    var themeIndex by remember { mutableIntStateOf(0) }

    val bgColor = when (themeIndex) {
        1 -> Color(0xFF16120C)
        2 -> Color(0xFF020104)
        else -> Color(0xFF09040F)
    }

    val textColor = when (themeIndex) {
        1 -> Color(0xFFE8DAC2)
        2 -> Color(0xFFE0DAE8)
        else -> Color(0xFFEDE4F5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // TOP APP BAR
        Surface(
            color = Color(0xFF06030B),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .border(1.dp, Color(0xFF2E1A3F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Color(0xFFDEC595)
                    )
                }

                Text(
                    text = story.title,
                    color = Color(0xFFDEC595),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "💀 داستان هوش مصنوعی: ${story.title}\n\n${story.content}\n\n— خانه وحشت (Gemini AI)"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری داستان هوش مصنوعی"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری", tint = Color(0xFFDEC595))
                }
            }
        }

        // READING CONTROLS STRIP
        Surface(
            color = Color(0xFF0C0714),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF261536))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Font Size Adjusters
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("اندازه قلم:", color = Color(0xFF8B8496), fontSize = 11.sp)
                    IconButton(
                        onClick = { if (fontSize > 13f) fontSize -= 1f },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Text("A-", color = Color(0xFFDEC595), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${fontSize.toInt()}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { if (fontSize < 24f) fontSize += 1f },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Text("A+", color = Color(0xFFDEC595), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Theme Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val themes = listOf("گوتیگ", "پوستینه", "ظلمت")
                    themes.forEachIndexed { idx, name ->
                        val isSel = idx == themeIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFFB8143F) else Color(0xFF160E22))
                                .border(1.dp, if (isSel) Color(0xFFDEC595) else Color(0xFF2E1C44), RoundedCornerShape(6.dp))
                            .clickable { themeIndex = idx }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(name, color = if (isSel) Color.White else Color(0xFF8B8496), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // READING CONTENT
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DOOM & GENRE HEADER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12081E)),
                    border = BorderStroke(1.dp, Color(0xFF381A54)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔮 سبک: ${story.genre}",
                                color = Color(0xFFDEC595),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                color = Color(0xFF260D18),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF1E56).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "💀 شاخص وحشت: ${story.doomScore}٪",
                                    color = Color(0xFFFF1E56),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Doom score horizontal progress bar
                        LinearProgressIndicator(
                            progress = { story.doomScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF1E56),
                            trackColor = Color(0xFF2D1426)
                        )

                        Text(
                            text = "🤖 پدید آمده از رازهای نهان هوش مصنوعی Gemini",
                            color = Color(0xFF7A6B88),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // SYNOPSIS (BLOCKQUOTE)
            val syn = story.synopsis
            if (!syn.isNullOrBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF160920).copy(alpha = 0.7f))
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFDEC595).copy(alpha = 0.8f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "« $syn »",
                            color = Color(0xFFDEC595),
                            fontSize = (fontSize - 1).sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = (fontSize * 1.5f).sp
                        )
                    }
                }
            }

            // FULL STORY CONTENT
            item {
                SelectionContainer {
                    Text(
                        text = story.content,
                        color = textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.8f).sp,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // RATING AND FEEDBACK BOX
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12081E)),
                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "به این داستان هوش مصنوعی چه امتیازی می‌دهید؟",
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = {
                                        userRating = star
                                        ratingSubmitted = true
                                        HorrorSoundManager.playScenarioChoiceSound()
                                        viewModel.rateAiStory(story.id, star.toFloat())
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        if (star <= userRating || (userRating == 0 && star <= story.ratingScore.toInt()))
                                            Icons.Default.Star
                                        else
                                            Icons.Default.StarBorder,
                                        contentDescription = "ستاره $star",
                                        tint = if (star <= userRating || (userRating == 0 && star <= story.ratingScore.toInt()))
                                            Color(0xFFFFD700)
                                        else
                                            Color(0xFF5A496B),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        if (ratingSubmitted) {
                            Text(
                                text = "✨ امتیاز شما ($userRating ستاره) با موفقیت ثبت شد!",
                                color = Color(0xFF4CAF50),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "میانگین فعلی: ${String.format("%.1f", story.ratingScore)} از ۵ (${story.ratingCount} رأی)",
                                color = Color(0xFF8B8496),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Return to list button
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A0A17)),
                            border = BorderStroke(1.dp, Color(0xFFFF1E56)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("بازگشت به فهرست داستان‌ها", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
