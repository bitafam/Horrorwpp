package com.example.ui.user

import android.app.Activity
import android.content.IntentSender
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.MyketBillingManager
import com.example.util.HorrorSoundManager
import com.example.viewmodel.HorrorViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SubscriptionScreen(
    viewModel: HorrorViewModel,
    billingManager: MyketBillingManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isPremium by viewModel.isPremiumUser.collectAsState()
    val priceToman by viewModel.subscriptionPriceToman.collectAsState()

    var isPurchasing by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val formattedPrice = remember(priceToman) {
        val nf = NumberFormat.getNumberInstance(Locale("fa", "IR"))
        "${nf.format(priceToman)} تومان"
    }

    val originalPrice = remember(priceToman) {
        val original = (priceToman * 2).coerceAtLeast(89000)
        val nf = NumberFormat.getNumberInstance(Locale("fa", "IR"))
        "${nf.format(original)} تومان"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07040C))
    ) {
        GamingTopBar(
            title = "اشتراک دائمی عمارت وحشت",
            subtitle = "عضویت ویژه VIP و حذف تبلیغات",
            icon = Icons.Default.WorkspacePremium,
            badgeText = if (isPremium) "فعال" else "پیشنهاد ویژه",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HERO VIP CROWN BADGE
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFDEC595).copy(alpha = 0.3f),
                                Color(0xFFB8143F).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFDEC595), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFDEC595),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "دسترسی نامحدود و همیشگی",
                color = Color(0xFFDEC595),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "با خرید اشتراک دائمی (یک‌بار برای همیشه)، تمامی امکانات ویژه را باز کنید و از شر تمام تبلیغات خلاص شوید.",
                color = Color(0xFF8B8496),
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PRICING CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E0A24),
                                Color(0xFF0F0615)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFFB8143F).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFB8143F))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "۵۰٪ تخفیف افتتاحیه",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "مالکیت مادام‌العمر ♾️",
                            color = Color(0xFFDEC595),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = originalPrice,
                            color = Color(0xFF6E687A),
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedPrice,
                            color = Color(0xFFDEC595),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "پرداخت فقط یک‌بار بدون نیاز به تمدید ماهانه",
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ADVANTAGES / FEATURES LIST
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "مزایای نسخه ویژه دائمی:",
                    color = Color(0xFFDEC595),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                SubscriptionFeatureItem(
                    icon = Icons.Default.Block,
                    title = "حذف دائمی تمامی تبلیغات",
                    description = "حذف کامل تبلیغات بنری و تمام‌صفحه بازگشت به برنامه در کل اپلیکیشن برای همیشه."
                )

                SubscriptionFeatureItem(
                    icon = Icons.Default.HistoryEdu,
                    title = "ارسال داستان و روایات شخصی",
                    description = "امکان نامحدود ثبت روایات و تجربیات وحشتناک برای انتشار با نام خود در کتابخانه عمومی."
                )

                SubscriptionFeatureItem(
                    icon = Icons.Default.LockOpen,
                    title = "مطالعه روان بدون وقفه",
                    description = "خواندن تمام داستان‌های کاربران، هوش مصنوعی و روایات بدون نیاز به مشاهده حتی ۱ ثانیه تبلیغ."
                )

                SubscriptionFeatureItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "عضویت دائمی در محفل تاریکی",
                    description = "دریافت نشان طلایی و دسترسی به تمام قابلیت‌ها و بروزرسانی‌های آینده عمارت وحشت."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // STATUS FEEDBACK
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1B0C1E))
                        .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = Color(0xFFDEC595),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACTION BUTTONS
            if (isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F2615))
                        .border(1.5.dp, Color(0xFF2ECC71), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "شما کاربر ویژه و دائمی عمارت هستید 👑",
                            color = Color(0xFF2ECC71),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (activity == null) {
                            Toast.makeText(context, "خطا در ارزیابی صفحه جاری", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        HorrorSoundManager.playClickSound()
                        isPurchasing = true
                        statusMessage = "در حال اتصال به درگاه پرداخت مایکت..."

                        billingManager.launchPurchaseFlow(
                            activity = activity,
                            onPendingIntentReady = { pendingIntent ->
                                isPurchasing = false
                                statusMessage = null
                                try {
                                    activity.startIntentSenderForResult(
                                        pendingIntent.intentSender,
                                        MyketBillingManager.RC_MYKET_PURCHASE,
                                        null,
                                        0,
                                        0,
                                        0
                                    )
                                } catch (e: IntentSender.SendIntentException) {
                                    statusMessage = "خطا در فراخوانی پرداخت مایکت: ${e.message}"
                                }
                            },
                            onError = { error ->
                                isPurchasing = false
                                statusMessage = error
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isPurchasing && !isRestoring,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB8143F),
                        contentColor = Color.White
                    )
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("در حال اتصال به مایکت...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "خرید اشتراک دائمی ($formattedPrice)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        HorrorSoundManager.playClickSound()
                        isRestoring = true
                        statusMessage = "در حال بررسی خریدهای پیشین شما در مایکت..."

                        billingManager.queryPurchasesAsync { isOwned ->
                            isRestoring = false
                            if (isOwned) {
                                viewModel.setPremiumUser(true)
                                statusMessage = "خرید شما با موفقیت بازیابی شد! شما اکنون کاربر ویژه هستید."
                                Toast.makeText(context, "اشتراک دائمی فعال گردید!", Toast.LENGTH_SHORT).show()
                            } else {
                                statusMessage = "هیچ خرید فعالی در حساب کاربری مایکت شما برای این محصول یافت نشد."
                            }
                        }
                    },
                    enabled = !isPurchasing && !isRestoring,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f))
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFFDEC595),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال بازیابی...", color = Color(0xFFDEC595), fontSize = 12.5.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بازیابی خرید قبلی",
                            color = Color(0xFFDEC595),
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECURITY BADGE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF6E687A),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "پرداخت امن مستقیم از درگاه درون‌برنامه‌ای مایکت",
                    color = Color(0xFF6E687A),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SubscriptionFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F0716))
            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF280B18))
                    .border(1.dp, Color(0xFFB8143F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFDEC595),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFFDEC595),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = Color(0xFF8B8496),
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
