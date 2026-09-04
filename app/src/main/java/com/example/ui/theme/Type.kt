package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class HorrorFontOption(
    val id: Int,
    val name: String,
    val subtitle: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontStyle: FontStyle = FontStyle.Normal,
    val letterSpacing: TextUnit = 0.sp,
    val lineHeightMultiplier: Float = 1.75f
)

val HorrorFontPresets = listOf(
    HorrorFontOption(
        id = 0,
        name = "وزیر کلاسیک",
        subtitle = "قلم استاندارد، متعادل و روان برای مطالعه طولانی",
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeightMultiplier = 1.75f
    ),
    HorrorFontOption(
        id = 1,
        name = "نسخ باستانی (سریف)",
        subtitle = "قلم کهن و ادبی با خطوط کلاسیک سنگی",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        letterSpacing = (-0.3).sp,
        lineHeightMultiplier = 1.9f
    ),
    HorrorFontOption(
        id = 2,
        name = "کتیبه سنگی (کوبنده)",
        subtitle = "حروف درشت و ضخیم برای داستان‌های پرتعلیق و هولناک",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp,
        lineHeightMultiplier = 1.65f
    ),
    HorrorFontOption(
        id = 3,
        name = "دوات و طلسم (تحریری)",
        subtitle = "سبک خوشنویسی دست‌نویس باستانی و رمزآلود",
        fontFamily = FontFamily.Cursive,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        letterSpacing = 0.2.sp,
        lineHeightMultiplier = 2.0f
    ),
    HorrorFontOption(
        id = 4,
        name = "مونوگرافیک تاریک (اسرارآمیز)",
        subtitle = "قلم با فاصله‌گذاری مهندسی‌شده و مدرن گوتیک",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        lineHeightMultiplier = 1.85f
    )
)

val HorrorTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
)
