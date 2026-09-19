package com.example.ui.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.HorrorSoundManager
import kotlinx.coroutines.launch

/**
 * Slide model representing each screen in the gothic onboarding tour.
 */
data class HorrorOnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val drawableRes: Int,
    val accentColor: Color,
    val disclaimerText: String? = null
)

@Composable
fun HorrorOnboardingScreen(
    onFinish: () -> Unit
) {
    val slides = listOf(
        // Slide 1: Welcome & Real Haunted Stories
        HorrorOnboardingSlide(
            title = "عمارت وحشت",
            subtitle = "به قلمرو ترسناک‌ترین روایات خوش آمدید",
            description = "مجموعه‌ای عظیم و مستند از هولناک‌ترین تجربیات ماوراءالطبیعه، ارواح، طلسم‌ها، خانه‌های تسخیرشده و اعترافات هراس‌انگیز واقعی.",
            badge = "درگاه ورودی",
            icon = Icons.Default.MenuBook,
            drawableRes = R.drawable.img_poster_dark_demon_1789804434865,
            accentColor = Color(0xFFB8143F)
        ),

        // Slide 2: AI Horror Chronicles
        HorrorOnboardingSlide(
            title = "داستان‌های هوش تاریکی",
            subtitle = "روایت‌های جنون‌آمیز خلق شده با هوش مصنوعی",
            description = "کاتب ماورایی با هوش پیشرفته، بر اساس سبک‌های گوتیک، کیهانی و فولکلور ایرانی، داستان‌هایی بی‌انتها با فضاسازی سینمایی برای شما به نگارش درمی‌آورد.",
            badge = "هوش مصنوعی",
            icon = Icons.Default.Psychology,
            drawableRes = R.drawable.img_poster_ghost_corridor_1789804447925,
            accentColor = Color(0xFF9C27B0)
        ),

        // Slide 3: Grim Fortunes (Special feature spotlight + entertainment disclaimer)
        HorrorOnboardingSlide(
            title = "طالع شوم ۱۲ ماه سال",
            subtitle = "شوم‌ترین و سیاه‌ترین فال ماورایی برای ماه تولد شما",
            description = "پیش‌گویی‌های تاریک، هولناک و اشعار رمزآلود گوتیک حافظ در دنیای سایه‌ها برای متولدین هر ماه خورشیدی. سرنوشت شوم خود را در تالار ارواح کشف کنید!",
            badge = "ویژه و پرطرفدار",
            icon = Icons.Default.AutoAwesome,
            drawableRes = R.drawable.img_poster_blood_ritual_1789804466122,
            accentColor = Color(0xFFFF9800),
            disclaimerText = "⚠️ توجه: این بخش صرفاً جنبه فان، شوخی تاریک و سرگرمی دارد و نباید به عنوان واقعیت جدی گرفته شود."
        ),

        // Slide 4: User Story Submissions
        HorrorOnboardingSlide(
            title = "ارسال داستان و نگارش راز",
            subtitle = "روایت ترسناک و واقعی خود را ثبت کنید",
            description = "تجربه دلهره‌آور خود را برای هزاران کاربر عمارت وحشت بنویسید تا پس از بررسی در کتیبه‌های ماندگار عمارت ثبت و منتشر شود.",
            badge = "مشارکت کاربران",
            icon = Icons.Default.Create,
            drawableRes = R.drawable.img_poster_cemetery_curse_1789804492834,
            accentColor = Color(0xFFDEC595)
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07030A))
    ) {
        // Horizontal Pager for the slides
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val slide = slides[pageIndex]

            Box(modifier = Modifier.fillMaxSize()) {
                // Background Poster Image with terrifying dark vignette
                Image(
                    painter = painterResource(id = slide.drawableRes),
                    contentDescription = slide.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Multi-stop deep horror gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xCC050209),
                                    Color(0x88050209),
                                    Color(0xDD07030A),
                                    Color(0xFF07030A)
                                )
                            )
                        )
                )

                // Atmospheric border glow
                MirrorCracksCanvas(modifier = Modifier.fillMaxSize())

                // Slide Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Badge with Icon
                    Surface(
                        color = slide.accentColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, slide.accentColor.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = slide.badge,
                                color = slide.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Title
                    Text(
                        text = slide.title,
                        color = Color(0xFFDEC595),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle
                    Text(
                        text = slide.subtitle,
                        color = Color(0xFFFF4D4D),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detailed Description
                    Text(
                        text = slide.description,
                        color = Color(0xFFE4DFEC),
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )

                    // Special disclaimer banner (Required for Grim Fortunes)
                    if (!slide.disclaimerText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = Color(0xFF2B1408),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = slide.disclaimerText,
                                    color = Color(0xFFFFCC80),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Bar: Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF140A18).copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f))
            ) {
                Text(
                    text = "معرفی امکانات عمارت",
                    color = Color(0xFFDEC595),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (!isLastPage) {
                TextButton(
                    onClick = {
                        onFinish()
                    }
                ) {
                    Text(
                        text = "رد کردن ❯",
                        color = Color(0xFFDEC595),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Navigation Bar: Indicators and Next/Start Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dot Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                repeat(slides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFB8143F) else Color(0xFFDEC595).copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Action Button (Next or Enter Realm)
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastPage) Color(0xFFB8143F) else Color(0xFF280612)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isLastPage) Color(0xFFFF1E56) else Color(0xFFB8143F).copy(alpha = 0.8f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isLastPage) "ورود به عمارت وحشت" else "صفحه بعد",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLastPage) Icons.Default.DarkMode else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
