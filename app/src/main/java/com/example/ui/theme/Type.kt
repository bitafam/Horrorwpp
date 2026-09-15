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
        name = "وزیر استاندارد (متعادل)",
        subtitle = "قلم متوازن، خوانا و کلاسیک برای مطالعه پیوسته",
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal,
        letterSpacing = 0.sp,
        lineHeightMultiplier = 1.75f
    ),
    HorrorFontOption(
        id = 1,
        name = "نسخ ظریف و ادبی (باریک)",
        subtitle = "خطوط سبک و با فاصله باز، مناسب رمان‌های کهن",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Light,
        fontStyle = FontStyle.Italic,
        letterSpacing = 0.8.sp,
        lineHeightMultiplier = 2.1f
    ),
    HorrorFontOption(
        id = 2,
        name = "کتیبه سنگین (بولد و کوبنده)",
        subtitle = "حروف ضخیم و سیاه برای روایات دلهره‌آور و شوکه‌کننده",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Normal,
        letterSpacing = (-0.4).sp,
        lineHeightMultiplier = 1.6f
    ),
    HorrorFontOption(
        id = 3,
        name = "دوات و طلسم (تحریری و جادویی)",
        subtitle = "حالت مایل و ادبی شبیه به دست‌نوشته‌های کهن طلسم‌شده",
        fontFamily = FontFamily.Cursive,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        letterSpacing = 1.4.sp,
        lineHeightMultiplier = 2.0f
    ),
    HorrorFontOption(
        id = 4,
        name = "مونوگرافیک با فاصله گوتیک (اسرارآمیز)",
        subtitle = "فاصله‌گذاری مهندسی‌شده و پهن با وزن نیمه‌ضخیم",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Normal,
        letterSpacing = 2.4.sp,
        lineHeightMultiplier = 1.9f
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
