package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.R

val VazirFontFamily = FontFamily(Font(R.font.vazirmatn, FontWeight.Normal))
val ShabnamFontFamily = FontFamily(Font(R.font.shabnam, FontWeight.Normal))
val SahelFontFamily = FontFamily(Font(R.font.sahel, FontWeight.Normal))
val GandomFontFamily = FontFamily(Font(R.font.gandom, FontWeight.Normal))

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
        name = "فونت وزیر (پیش‌فرض)",
        subtitle = "قلم استاندارد، بسیار خوانا و متوازن برای مطالعه پیوسته",
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal,
        letterSpacing = 0.sp,
        lineHeightMultiplier = 1.75f
    ),
    HorrorFontOption(
        id = 1,
        name = "فونت رسمی شبنم",
        subtitle = "قلم شکیل و هندسی، مناسب عناوین و متون روایی",
        fontFamily = ShabnamFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal,
        letterSpacing = 0.sp,
        lineHeightMultiplier = 1.75f
    ),
    HorrorFontOption(
        id = 2,
        name = "فونت زیبای ساحل",
        subtitle = "خطوط روان، صمیمی و دلنشین برای متون بلند",
        fontFamily = SahelFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal,
        letterSpacing = 0.sp,
        lineHeightMultiplier = 1.8f
    ),
    HorrorFontOption(
        id = 3,
        name = "فونت گندم",
        subtitle = "قلم گرم، هنری و چشم‌نواز با حس اصیل",
        fontFamily = GandomFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal,
        letterSpacing = 0.sp,
        lineHeightMultiplier = 1.8f
    )
)

val HorrorTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
)
