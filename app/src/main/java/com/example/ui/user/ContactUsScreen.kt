package com.example.ui.user

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.HorrorSoundManager

@Composable
fun ContactUsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var senderName by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf("") }
    var selectedSubjectIndex by remember { mutableIntStateOf(0) }
    var messageContent by remember { mutableStateOf("") }

    val subjectOptions = listOf(
        "پیشنهاد و انتقاد",
        "گزارش خطا / باگ",
        "همکاری تجاری",
        "سوال عمومی"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07040C))
    ) {
        // TOP BAR
        GamingTopBar(
            title = "تماس با ما",
            subtitle = "ارتباط مستقیم با توسعه‌دهنده",
            icon = Icons.Default.Email,
            badgeText = "پشتیبانی",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // DEVELOPER BIO CARD
            DeveloperProfileCard(context = context)

            // CONTACT FORM CARD
            DirectMessageFormCard(
                senderName = senderName,
                onSenderNameChange = { senderName = it },
                senderEmail = senderEmail,
                onSenderEmailChange = { senderEmail = it },
                selectedSubjectIndex = selectedSubjectIndex,
                onSelectSubject = { selectedSubjectIndex = it },
                subjectOptions = subjectOptions,
                messageContent = messageContent,
                onMessageChange = { messageContent = it },
                onSubmit = {
                    if (senderName.isBlank()) {
                        Toast.makeText(context, "لطفاً نام خود را وارد کنید", Toast.LENGTH_SHORT).show()
                        return@DirectMessageFormCard
                    }
                    if (messageContent.isBlank()) {
                        Toast.makeText(context, "لطفاً متن پیام را وارد کنید", Toast.LENGTH_SHORT).show()
                        return@DirectMessageFormCard
                    }

                    HorrorSoundManager.playScenarioChoiceSound()
                    sendEmailIntent(
                        context = context,
                        name = senderName.trim(),
                        email = senderEmail.trim(),
                        subjectCategory = subjectOptions.getOrElse(selectedSubjectIndex) { "پیام عمومی" },
                        message = messageContent.trim()
                    )
                }
            )

            // FOOTER COPYRIGHT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "توسعه دهنده: امیرحسین سالاری",
                        color = Color(0xFFDEC595),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "تمامی حقوق محفوظ است © 2026",
                    color = Color(0xFF8B8496),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DeveloperProfileCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
        border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CODE ICON AVATAR
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1F112E), Color(0xFF140B1E))
                        )
                    )
                    .border(1.5.dp, Color(0xFFDEC595).copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = Color(0xFFDEC595),
                    modifier = Modifier.size(28.dp)
                )
            }

            // BADGE: توسعه دهنده
            Surface(
                color = Color(0xFF1F112E),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.6f))
            ) {
                Text(
                    text = "توسعه دهنده",
                    color = Color(0xFFDEC595),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                )
            }

            // NAME
            Text(
                text = "امیرحسین سالاری",
                color = Color(0xFFDEC595),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // ROLE PILL
            Surface(
                color = Color(0xFF160E22),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.25f))
            ) {
                Text(
                    text = "برنامه‌نویس و طراح وب‌سایت",
                    color = Color(0xFFB8B2C4),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            // BIO PARAGRAPH
            Text(
                text = "سلام! من امیرحسین سالاری هستم. نزدیک به ۶ سال است که به عنوان برنامه‌نویس فعالیت دارم. شور و اشتیاق بی‌نهایتی به نوشتن کدهای تمیز، حل مسائل پیچیده و خلق محصولات کاربردی دارم. تجربه توسعه برنامه‌های اندرویدی، پیاده‌سازی وب‌سایت‌های حرفه‌ای و طراحی سیستم‌های تعاملی را در کارنامه خود دارم.",
                color = Color(0xFF9E98AA),
                fontSize = 12.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            // SKILL TAGS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkillBadge(text = "جاوا (Java)", hasGreenDot = true)
                Spacer(modifier = Modifier.width(8.dp))
                SkillBadge(text = "پایتون (Python)", hasGreenDot = true)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkillBadge(text = "طراحی وب‌سایت", icon = Icons.Default.Public)
                Spacer(modifier = Modifier.width(8.dp))
                SkillBadge(text = "بازی‌سازی علاقمند", icon = Icons.Default.SportsEsports)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // STAT CARDS (ROW 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "سابقه برنامه‌نویسی",
                    value = "نزدیک به ۶ سال"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "تخصص اصلی",
                    value = "Java & Python & Web"
                )
            }

            // STAT CARD (ROW 2 - FULL WIDTH)
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                label = "محصولات منتشر شده",
                value = "برنامه‌های کاربردی متعدد"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // MYKET PROMO BANNER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140A20)),
                border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "دیگر برنامه‌های امیرحسین سالاری را دیده‌اید؟",
                        color = Color(0xFFDEC595),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "می‌توانید تمام آثار، ابزارها و بازی‌های دیگر من را در مارکت مایکت بررسی کنید.",
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            openMyketDeveloperPage(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F112E)),
                        border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.7f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دیگر برنامه‌های ما (مایکت)",
                                color = Color(0xFFDEC595),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillBadge(
    text: String,
    icon: ImageVector? = null,
    hasGreenDot: Boolean = false
) {
    Surface(
        color = Color(0xFF140A20),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasGreenDot) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF2ECC71), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFDEC595),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = Color(0xFFDEC595),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF140A20),
        border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF8B8496),
                fontSize = 10.sp
            )
            Text(
                text = value,
                color = Color(0xFFDEC595),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DirectMessageFormCard(
    senderName: String,
    onSenderNameChange: (String) -> Unit,
    senderEmail: String,
    onSenderEmailChange: (String) -> Unit,
    selectedSubjectIndex: Int,
    onSelectSubject: (Int) -> Unit,
    subjectOptions: List<String>,
    messageContent: String,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
        border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // FORM HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فرم ارسال پیام مستقیم",
                        color = Color(0xFFDEC595),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "salarybusines@gmail.com",
                    color = Color(0xFF2ECC71),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                color = Color(0xFFDEC595).copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // NAME FIELD
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "نام و نام خانوادگی *",
                        color = Color(0xFFDEC595),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                OutlinedTextField(
                    value = senderName,
                    onValueChange = onSenderNameChange,
                    placeholder = {
                        Text(
                            text = "مثال: علی محمدی",
                            color = Color(0xFF6E687A),
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFDEC595),
                        unfocusedBorderColor = Color(0xFFDEC595).copy(alpha = 0.25f),
                        focusedContainerColor = Color(0xFF140A20),
                        unfocusedContainerColor = Color(0xFF140A20),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // EMAIL FIELD
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "آدرس ایمیل",
                        color = Color(0xFFDEC595),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                OutlinedTextField(
                    value = senderEmail,
                    onValueChange = onSenderEmailChange,
                    placeholder = {
                        Text(
                            text = "مثال: user@example.com",
                            color = Color(0xFF6E687A),
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFDEC595),
                        unfocusedBorderColor = Color(0xFFDEC595).copy(alpha = 0.25f),
                        focusedContainerColor = Color(0xFF140A20),
                        unfocusedContainerColor = Color(0xFF140A20),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SUBJECT SELECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "موضوع پیام",
                        color = Color(0xFFDEC595),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubjectChip(
                        modifier = Modifier.weight(1f),
                        text = subjectOptions[0],
                        isSelected = selectedSubjectIndex == 0,
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            onSelectSubject(0)
                        }
                    )
                    SubjectChip(
                        modifier = Modifier.weight(1f),
                        text = subjectOptions[1],
                        isSelected = selectedSubjectIndex == 1,
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            onSelectSubject(1)
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubjectChip(
                        modifier = Modifier.weight(1f),
                        text = subjectOptions[2],
                        isSelected = selectedSubjectIndex == 2,
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            onSelectSubject(2)
                        }
                    )
                    SubjectChip(
                        modifier = Modifier.weight(1f),
                        text = subjectOptions[3],
                        isSelected = selectedSubjectIndex == 3,
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            onSelectSubject(3)
                        }
                    )
                }
            }

            // MESSAGE FIELD
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "متن پیام شما *",
                            color = Color(0xFFDEC595),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${toPersianDigits(messageContent.length)} کاراکتر",
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp
                    )
                }
                OutlinedTextField(
                    value = messageContent,
                    onValueChange = onMessageChange,
                    placeholder = {
                        Text(
                            text = "متن پیام خود را بنویسید...",
                            color = Color(0xFF6E687A),
                            fontSize = 12.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFDEC595),
                        unfocusedBorderColor = Color(0xFFDEC595).copy(alpha = 0.25f),
                        focusedContainerColor = Color(0xFF140A20),
                        unfocusedContainerColor = Color(0xFF140A20),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // HINT NOTICE
            Text(
                text = "پیام شما مستقیم به ایمیل امیرحسین سالاری ارسال خواهد شد",
                color = Color(0xFF8B8496),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // SUBMIT BUTTON
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.8f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ثبت و آماده‌سازی پیام",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectChip(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF1F112E) else Color(0xFF140A20),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFFDEC595) else Color(0xFFDEC595).copy(alpha = 0.25f)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color(0xFFDEC595) else Color(0xFF8B8496),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun sendEmailIntent(
    context: Context,
    name: String,
    email: String,
    subjectCategory: String,
    message: String
) {
    val recipient = "salarybusines@gmail.com"
    val subject = "[عمارت وحشت - $subjectCategory] پیام از طرف $name"
    val emailBody = buildString {
        appendLine("فرستنده: $name")
        if (email.isNotBlank()) {
            appendLine("ایمیل فرستنده: $email")
        }
        appendLine("دسته‌بندی موضوع: $subjectCategory")
        appendLine("برنامه: عمارت وحشت (نسخه اندروید)")
        appendLine("----------------------------------------")
        appendLine("متن پیام:")
        appendLine(message)
    }

    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال پیام از طریق ایمیل"))
    } catch (e: Exception) {
        // Fallback generic send intent
        try {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }
            context.startActivity(Intent.createChooser(fallback, "ارسال پیام از طریق ایمیل"))
        } catch (_: Exception) {
            Toast.makeText(context, "برنامه ایمیل در دستگاه یافت نشد. لطفاً مستقیماً به $recipient پیام دهید.", Toast.LENGTH_LONG).show()
        }
    }
}

fun openMyketDeveloperPage(context: Context) {
    val developerPackage = "com.apps.wmqd"
    val myketPackage = "ir.mservices.market"
    try {
        // Official Myket developer intent: myket://developer/[PACKAGE_NAME]
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("myket://developer/$developerPackage")
            setPackage(myketPackage)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("myket://developer/$developerPackage"))
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            try {
                val webUri = Uri.parse("https://myket.ir/app/$developerPackage")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "خطا در باز کردن مارکت مایکت", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun toPersianDigits(number: Int): String {
    val persianChars = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val str = number.toString()
    val sb = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(persianChars[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}
