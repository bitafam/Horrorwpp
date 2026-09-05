package com.example.ui.user

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.HorrorSoundManager

@Composable
fun AgeAndHealthGateScreen(
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    var isOver15Checked by remember { mutableStateOf(false) }
    var noHeartConditionChecked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Intercept back button - exit application if user did not consent
    BackHandler {
        (context as? Activity)?.finishAffinity()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07030A))
    ) {
        // Decorative background image with heavy atmospheric vignette
        Image(
            painter = painterResource(id = R.drawable.img_poster_1_1788266550537),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.18f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xEE07030A),
                            Color(0xCC07030A),
                            Color(0xFF07030A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Warning Seal Icon with glowing border
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF280612))
                    .border(2.dp, Color(0xFFFF1E56), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = "هشدار سن و سلامت",
                    tint = Color(0xFFFF1E56),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFFB8143F).copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFB8143F))
            ) {
                Text(
                    text = "هشدار الزامی ورود • رده سنی +۱۵",
                    color = Color(0xFFFF4D4D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "عمارت وحشت",
                color = Color(0xFFDEC595),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "تأیید شرایط ورود و سلامت جسمانی",
                color = Color(0xFFEDE8F5),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Warning explanation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13081A)),
                border = BorderStroke(1.dp, Color(0xFF381A3F))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "هشدار محتوای دلهره‌آور و ماورایی",
                            color = Color(0xFFDEC595),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "این برنامه حاوی داستان‌های دلهره‌آور، وحشت روان‌شناختی، روایت‌های ماوراءالطبیعه و فضاسازی‌های شوکه‌کننده است که ممکن است باعث افزایش شدید ضربان قلب و هیجان عصبی شود.",
                        color = Color(0xFFB8B0C4),
                        fontSize = 12.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Justify
                    )

                    Text(
                        text = "ورود به برنامه تنها برای افراد بالای ۱۵ سال تمام و فاقد هرگونه بیماری قلبی مجاز است.",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verification Checkboxes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160920)),
                border = BorderStroke(1.5.dp, if (isOver15Checked && noHeartConditionChecked) Color(0xFF00E676) else Color(0xFFB8143F).copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Checkbox 1: Age >= 15
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isOver15Checked = !isOver15Checked
                                errorMessage = null
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isOver15Checked,
                            onCheckedChange = {
                                isOver15Checked = it
                                errorMessage = null
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF1E56),
                                uncheckedColor = Color(0xFF8E849A),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سن من بالای ۱۵ سال تمام است.",
                            color = if (isOver15Checked) Color(0xFFDEC595) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().height(1.dp),
                        color = Color(0xFF2C1636)
                    ) {}

                    // Checkbox 2: No Heart Condition
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                noHeartConditionChecked = !noHeartConditionChecked
                                errorMessage = null
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = noHeartConditionChecked,
                            onCheckedChange = {
                                noHeartConditionChecked = it
                                errorMessage = null
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF1E56),
                                uncheckedColor = Color(0xFF8E849A),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF4D4D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هیچ‌گونه بیماری قلبی، عروقی یا سابقه تشنج ندارم.",
                                color = if (noHeartConditionChecked) Color(0xFFDEC595) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Error feedback if user tries to submit without checking both
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        color = Color(0xFF380714),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF1E56))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF4D4D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = msg, color = Color(0xFFFFD4D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Confirm & Enter Button
            Button(
                onClick = {
                    if (!isOver15Checked || !noHeartConditionChecked) {
                        errorMessage = "جهت ورود به برنامه، باید حتماً تأیید کنید که بالای ۱۵ سال هستید و بیماری قلبی ندارید."
                        HorrorSoundManager.playScreamShort()
                    } else {
                        HorrorSoundManager.playClickSound()
                        onConfirm()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOver15Checked && noHeartConditionChecked) Color(0xFFB8143F) else Color(0xFF4A1828)
                )
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تأیید شرایط و ورود به برنامه",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exit App Button
            OutlinedButton(
                onClick = {
                    (context as? Activity)?.finishAffinity()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF553A5E)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB8B0C4)
                )
            ) {
                Text(
                    text = "شرایط را ندارم / خروج از برنامه",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
