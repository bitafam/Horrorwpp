package com.example.ui.user

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AiStory
import com.example.data.StoryReport
import com.example.ui.theme.HorrorFontPresets
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

    val genres = listOf(
        "همه",
        "روانشناختی",
        "ماورایی",
        "افسانه و فولکلور",
        "جنایی و معمایی",
        "شهری و آپارتمان",
        "جاده و جنگل",
        "خانه‌های قدیمی",
        "علمی‌تخیلی"
    )
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
            subtitle = "روایت‌های سیاه و هولناک هوش مصنوعی",
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
// POSTER HELPER FOR AI STORIES
// ==========================================

fun getStoryPosterDrawableRes(posterUrl: String?, storyId: String): Int {
    val fallbackList = listOf(
        R.drawable.img_ai_story_poster_1_1788531305066,
        R.drawable.img_ai_story_poster_2_1788531326949,
        R.drawable.img_poster_1_1788266550537,
        R.drawable.img_poster_2_1788266563762,
        R.drawable.img_poster_3_1788266577786,
        R.drawable.img_dark_hafez_banner_1788111363222,
        R.drawable.img_dark_sorcerer_banner_1788114846553,
        R.drawable.img_sorcery_temple_1788114860980
    )

    if (!posterUrl.isNullOrBlank()) {
        when {
            posterUrl.contains("img_ai_story_poster_1") -> return R.drawable.img_ai_story_poster_1_1788531305066
            posterUrl.contains("img_ai_story_poster_2") -> return R.drawable.img_ai_story_poster_2_1788531326949
            posterUrl.contains("img_poster_1") -> return R.drawable.img_poster_1_1788266550537
            posterUrl.contains("img_poster_2") -> return R.drawable.img_poster_2_1788266563762
            posterUrl.contains("img_poster_3") -> return R.drawable.img_poster_3_1788266577786
            posterUrl.contains("dark_hafez") -> return R.drawable.img_dark_hafez_banner_1788111363222
            posterUrl.contains("dark_sorcerer") -> return R.drawable.img_dark_sorcerer_banner_1788114846553
            posterUrl.contains("sorcery_temple") -> return R.drawable.img_sorcery_temple_1788114860980
        }
    }

    val hash = kotlin.math.abs(storyId.hashCode())
    return fallbackList[hash % fallbackList.size]
}

@Composable
fun AiStoryPosterGraphic(
    posterUrl: String?,
    storyId: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val defaultRes = remember(posterUrl, storyId) { getStoryPosterDrawableRes(posterUrl, storyId) }
    val isRemote = remember(posterUrl) {
        !posterUrl.isNullOrBlank() && (posterUrl.startsWith("http://") || posterUrl.startsWith("https://"))
    }

    if (isRemote) {
        AsyncImage(
            model = posterUrl,
            contentDescription = "پوستر داستان هوش مصنوعی",
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = defaultRes),
            placeholder = painterResource(id = defaultRes)
        )
    } else {
        Image(
            painter = painterResource(id = defaultRes),
            contentDescription = "پوستر داستان هوش مصنوعی",
            modifier = modifier,
            contentScale = contentScale
        )
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
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFB8143F).copy(alpha = 0.8f), Color(0xFF1C0D2E))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onRead),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0719)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // HERO POSTER WITH GRADIENT OVERLAY & FLOATING BADGES
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AiStoryPosterGraphic(
                    posterUrl = story.cover_image_url,
                    storyId = story.id,
                    modifier = Modifier.fillMaxSize()
                )

                // Atmospheric Dark Gradient Vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color(0xFF0F0719)
                                )
                            )
                        )
                )

                // FLOATING HEADER BADGES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Genre Badge
                    Surface(
                        color = Color(0xDD280F38),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF9C41C0))
                    ) {
                        Text(
                            text = "🔮 ${story.genre}",
                            color = Color(0xFFF1D8FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Doom Score Badge
                    val doomColor = when {
                        story.doomScore >= 80 -> Color(0xFFFF1E56)
                        story.doomScore >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                    Surface(
                        color = Color(0xDD12040A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, doomColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
            }

            // CARD BODY: Title, Synopsis, Stats & CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        maxLines = 2,
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
}

// ==========================================
// STORY REPORT DIALOG (گزارش محتوای نامناسب)
// ==========================================

@Composable
fun StoryReportDialog(
    storyId: String,
    storyTitle: String,
    storyAuthor: String,
    storyType: String,
    viewModel: HorrorViewModel,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf("محتوای نامناسب یا توهین‌آمیز") }
    var additionalDetails by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val reasons = listOf(
        "محتوای نامناسب یا توهین‌آمیز",
        "خشونت شدید یا آزاردهنده",
        "نقض قوانین و کپی‌رایت",
        "کیفیت بسیار پایین یا متن نامفهوم",
        "سایر موارد"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF140A1E),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFFF1E56))
                Text(
                    "گزارش محتوای نامناسب",
                    color = Color(0xFFDEC595),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitted) {
                    Text(
                        "گزارش شما با موفقیت ثبت شد و در پنل مدیریت عمارت وحشت بررسی خواهد شد. با تشکر از همکاری شما.",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.5.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        "روایت: «$storyTitle»",
                        color = Color(0xFFDEC595),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "علت گزارش را مشخص نمایید:",
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp
                    )

                    reasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF2E1225) else Color(0xFF1E1428))
                                .clickable { selectedReason = reason }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                reason,
                                color = if (isSelected) Color(0xFFFF1E56) else Color.White,
                                fontSize = 11.sp
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF1E56))
                            )
                        }
                    }

                    OutlinedTextField(
                        value = additionalDetails,
                        onValueChange = { additionalDetails = it },
                        label = { Text("توضیحات تکمیلی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF1E56),
                            unfocusedBorderColor = Color(0xFF381A54),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color(0xFFE63956), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (isSubmitted) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A0A17)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("بستن", color = Color(0xFFDEC595), fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = {
                        val finalReason = if (additionalDetails.isNotBlank()) {
                            "$selectedReason - $additionalDetails"
                        } else {
                            selectedReason
                        }
                        isSubmitting = true
                        viewModel.submitStoryReport(
                            StoryReport(
                                id = java.util.UUID.randomUUID().toString(),
                                story_id = storyId,
                                story_title = storyTitle,
                                story_author = storyAuthor,
                                story_type = storyType,
                                reason = finalReason,
                                createdAt = null
                            )
                        ) { success ->
                            isSubmitting = false
                            if (success) {
                                isSubmitted = true
                            } else {
                                errorMessage = "خطا در ثبت گزارش. لطفاً اتصال اینترنت را بررسی کنید."
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "ارسال گزارش",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (!isSubmitted && !isSubmitting) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = Color(0xFF8B8496), fontSize = 12.sp)
                }
            }
        }
    )
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

    val fontSize by viewModel.fontSize.collectAsState()
    val selectedFontIndex by viewModel.selectedFontIndex.collectAsState()

    var userRating by remember { mutableIntStateOf(0) }
    var ratingSubmitted by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Themes: 0 = Crypt Dark, 1 = Ancient Parchment, 2 = Pitch Black
    var themeIndex by remember { mutableIntStateOf(0) }

    val activeFontPreset = HorrorFontPresets.getOrNull(selectedFontIndex) ?: HorrorFontPresets[0]

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

    if (showReportDialog) {
        StoryReportDialog(
            storyId = story.id,
            storyTitle = story.title,
            storyAuthor = "هوش مصنوعی عمارت وحشت",
            storyType = "AI",
            viewModel = viewModel,
            onDismiss = { showReportDialog = false }
        )
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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    fontFamily = activeFontPreset.fontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Report Button
                    IconButton(
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            showReportDialog = true
                        }
                    ) {
                        Icon(
                            Icons.Default.ReportProblem,
                            contentDescription = "گزارش تخلف یا محتوای نامناسب",
                            tint = Color(0xFFFF4D4D)
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "💀 داستان ترسناک: ${story.title}\n\n${story.content}\n\n— عمارت وحشت (هوش مصنوعی عمارت وحشت)"
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
        }

        // READING CONTROLS STRIP
        Surface(
            color = Color(0xFF0C0714),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF261536))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Size Adjusters
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("اندازه قلم:", color = Color(0xFF8B8496), fontSize = 11.sp)
                        IconButton(
                            onClick = { if (fontSize > 12f) viewModel.setFontSize(fontSize - 1f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("A-", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${fontSize.toInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { if (fontSize < 26f) viewModel.setFontSize(fontSize + 1f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("A+", color = Color(0xFFDEC595), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Theme Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(name, color = if (isSel) Color.White else Color(0xFF8B8496), fontSize = 9.sp)
                            }
                        }
                    }
                }

                // Font Family Selector Chips (5 distinct presets)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(HorrorFontPresets) { preset ->
                        val isSel = preset.id == selectedFontIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF3B184F) else Color(0xFF140B1E))
                                .border(1.dp, if (isSel) Color(0xFFFF1E56) else Color(0xFF2E1A3F), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setFontFamily(preset.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset.name,
                                color = if (isSel) Color(0xFFFF1E56) else Color.White,
                                fontSize = 10.sp,
                                fontFamily = preset.fontFamily,
                                fontWeight = if (isSel) FontWeight.Bold else preset.fontWeight,
                                fontStyle = preset.fontStyle
                            )
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
            // ATMOSPHERIC POSTER HERO BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.6f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AiStoryPosterGraphic(
                            posterUrl = story.cover_image_url,
                            storyId = story.id,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x99000000),
                                            Color(0xFF09040F)
                                        )
                                    )
                                )
                        )
                        // Story Title overlay at bottom of poster
                        Text(
                            text = story.title,
                            color = Color(0xFFDEC595),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = activeFontPreset.fontFamily,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        )
                    }
                }
            }

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🤖 نویسنده و منبع: هوش مصنوعی عمارت وحشت",
                                color = Color(0xFFDEC595).copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            TextButton(
                                onClick = { showReportDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.ReportProblem,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("گزارش محتوا", color = Color(0xFFFF6B6B), fontSize = 10.sp)
                            }
                        }
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
                            fontFamily = activeFontPreset.fontFamily,
                            fontWeight = activeFontPreset.fontWeight,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = activeFontPreset.letterSpacing,
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
                        lineHeight = (fontSize * activeFontPreset.lineHeightMultiplier).sp,
                        fontFamily = activeFontPreset.fontFamily,
                        fontWeight = activeFontPreset.fontWeight,
                        fontStyle = activeFontPreset.fontStyle,
                        letterSpacing = activeFontPreset.letterSpacing,
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
