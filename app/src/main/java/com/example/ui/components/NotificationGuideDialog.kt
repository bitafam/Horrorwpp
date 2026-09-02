package com.example.ui.components

import android.content.ComponentName
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
import androidx.compose.material.icons.filled.PhoneAndroid
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

    var selectedBrandTab by remember { mutableIntStateOf(0) } // 0: Xiaomi, 1: Samsung, 2: Others

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130D1B))
                .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFDEC595),
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = "🔔 راهنمای دریافت ۱۰۰٪ نوتیفیکیشن‌ها",
                    color = Color(0xFFEDE8F5),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "برای اینکه اعلان‌ها حتی هنگام بسته بودن کامل برنامه یا خاموش بودن صفحه دریافت شوند، مراحل زیر را فعال کنید:",
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
                        Text("۱. مجوز نمایش اعلان‌ها (Android 13+)", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "مجوز رسمی سیستم‌عامل جهت نمایش پیام‌های بنری و پخش صدای اعلان.",
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
                            "جلوگیری از متوقف شدن سرویس بررسی اعلان‌ها در پس‌زمینه توسط سیستم‌عامل.",
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
                                text = if (isBatteryIgnored) "✓ غیرفعال شد (بهینه‌سازی برداشت شد)" else "تنظیم حذف محدودیت باتری",
                                color = Color(0xFFDEC595),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Step 3: OEM Brand Specific Autostart Guide
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.SettingsPower, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(18.dp))
                            Text("۳. تنظیمات شروع خودکار (Autostart)", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Text(
                            "برای فعال‌سازی گزینه Autostart و پس‌زمینه، مدل گوشی خود را انتخاب کنید:",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp
                        )

                        // Brand Selector Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = selectedBrandTab == 0,
                                onClick = { selectedBrandTab = 0 },
                                label = { Text("شیائومی", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF800E2E),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2038),
                                    labelColor = Color(0xFFB0A8C0)
                                )
                            )
                            FilterChip(
                                selected = selectedBrandTab == 1,
                                onClick = { selectedBrandTab = 1 },
                                label = { Text("سامسونگ", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF800E2E),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2038),
                                    labelColor = Color(0xFFB0A8C0)
                                )
                            )
                            FilterChip(
                                selected = selectedBrandTab == 2,
                                onClick = { selectedBrandTab = 2 },
                                label = { Text("سایر برندها", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF800E2E),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2038),
                                    labelColor = Color(0xFFB0A8C0)
                                )
                            )
                        }

                        // Tab Contents
                        when (selectedBrandTab) {
                            0 -> { // Xiaomi Guide
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF130D1B), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📱 راهنمای شیائومی (MIUI / HyperOS):", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("۱. به «تنظیمات گوشی -> برنامه‌ها -> مدیریت برنامه‌ها» بروید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)
                                    Text("۲. برنامه «عمارت ارواح» را پیدا کنید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)
                                    Text("۳. گزینه «شروع خودکار (Autostart)» را روشن (ON) کنید.", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("۴. وارد «صرفه‌جویی در باتری (Battery Saver)» شوید و آن را روی «بدون محدودیت (No restrictions)» قرار دهید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            if (!openXiaomiAutostart(context)) {
                                                openAppSettings(context)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEC595)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("⚡ ورود به مدیریت شروع خودکار شیائومی", color = Color(0xFF130D1B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            1 -> { // Samsung Guide
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF130D1B), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📱 راهنمای سامسونگ (One UI):", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("۱. به «تنظیمات گوشی -> برنامه‌ها» رفته و «عمارت ارواح» را انتخاب کنید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)
                                    Text("۲. وارد بخش «باتری (Battery)» شوید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)
                                    Text("۳. حالت باتری را روی «غیرمحدود (Unrestricted)» بگذارید.", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("۴. در «تنظیمات -> باتری -> محدودیت‌های پس‌زمینه»، مطمئن شوید برنامه در لیست «برنامه‌های در حال خواب (Sleeping apps)» نباشد.", color = Color(0xFFEDE8F5), fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = { openAppSettings(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEC595)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("⚡ ورود به تنظیمات باتری و برنامه سامسونگ", color = Color(0xFF130D1B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            2 -> { // Huawei / Others Guide
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF130D1B), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📱 راهنمای هواوی، اوپو، نوکیا و سایر برندها:", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("۱. به «تنظیمات -> برنامه‌ها -> راه‌اندازی برنامه (App Launch)» بروید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)
                                    Text("۲. مدیریت خودکار برنامه «عمارت ارواح» را خاموش کنید.", color = Color(0xFFEDE8F5), fontSize = 11.sp)
                                    Text("۳. هر سه گزینه «راه‌اندازی خودکار»، «راه‌اندازی ثانویه» و «اجرا در پس‌زمینه» را روشن کنید.", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            if (!openHuaweiAutostart(context)) {
                                                openAppSettings(context)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEC595)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("⚡ ورود به تنظیمات برنامه", color = Color(0xFF130D1B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEC595)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("متوجه شدم، تنظیمات را انجام دادم", color = Color(0xFF130D1B), fontWeight = FontWeight.Bold)
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

fun openXiaomiAutostart(context: Context): Boolean {
    val intents = listOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.autostart.AutoStartManagementActivity"))
    )
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (_: Exception) {}
    }
    return false
}

fun openHuaweiAutostart(context: Context): Boolean {
    val intents = listOf(
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity"))
    )
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (_: Exception) {}
    }
    return false
}
