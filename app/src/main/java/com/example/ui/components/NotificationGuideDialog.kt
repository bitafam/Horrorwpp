package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsPower
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun NotificationGuideDialog(
    onDismiss: () -> Unit,
    onRequestPostNotifications: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isBatteryIgnored by remember {
        mutableStateOf(isIgnoringBatteryOptimizations(context))
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130D1B))
                .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFDEC595),
                    modifier = Modifier.size(42.dp)
                )

                Text(
                    text = "🔔 راهنمای دریافت آنی و کامل اعلان‌ها",
                    color = Color(0xFFEDE8F5),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "برای اینکه داستان‌ها و اخبار جدید عمارت ارواح حتی در صورت بسته بودن برنامه یا خاموش بودن صفحه بدون تاخیر به شما برسد، مراحل زیر را فعال کنید:",
                    color = Color(0xFFB0A8C0),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Divider(color = Color(0xFFDEC595).copy(alpha = 0.2f))

                // Step 1: POST_NOTIFICATIONS Permission
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("۱. مجوز نمایش اعلان‌ها", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            "اجازه نمایش پیام‌های شناور و صدای اطلاع‌رسانی در گوشی شما.",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = onRequestPostNotifications,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF800E2E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("اعطای مجوز اعلان", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Step 2: Battery Optimization
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isBatteryIgnored) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = if (isBatteryIgnored) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(18.dp)
                            )
                            Text("۲. استثنا از بهینه‌سازی باتری", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            "اندروید به‌طور خودکار برنامه‌های بسته را متوقف می‌کند. با حذف برنامه از بهینه‌سازی باتری، دریافت اعلان‌ها تضمین می‌شود.",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp
                        )
                        OutlinedButton(
                            onClick = {
                                openBatteryOptimizationSettings(context)
                                isBatteryIgnored = isIgnoringBatteryOptimizations(context)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDEC595))),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isBatteryIgnored) "✓ غیرفعال شده است" else "تنظیم بهینه‌سازی باتری",
                                color = Color(0xFFDEC595),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Step 3: Autostart & OEM Settings (Xiaomi, Samsung, etc.)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SettingsPower, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(18.dp))
                            Text("۳. شروع خودکار (Autostart / پس‌زمینه)", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            "در گوشی‌های شیائومی، سامسونگ و هواوی، گزینه «شروع خودکار (Autostart)» را در تنظیمات برنامه فعال کنید.",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp
                        )
                        OutlinedButton(
                            onClick = { openAppSettings(context) },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDEC595))),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("ورود به تنظیمات برنامه", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEC595)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("متوجه شدم، متشکرم", color = Color(0xFF130D1B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    } else {
        true
    }
}

fun openBatteryOptimizationSettings(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
