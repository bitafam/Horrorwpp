package com.example.ui.user

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.HorrorSoundManager
import com.example.util.NetworkUtils
import com.example.viewmodel.HorrorViewModel
import com.example.viewmodel.AppMode

// ==========================================
// PREMIUM HORROR GRAPHICS (DYNAMIC CUSTOM CANVASES)
// ==========================================

@Composable
fun ModernSpookyBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    
    // Smooth pulsing mist opacity for a terrifying live atmosphere
    val mistAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mistAlpha"
    )

    // Pulsing blood-red moon aura
    val moonAuraScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonAura"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Premium deep pitch-black / midnight dark violet background
        val skyGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF030106), Color(0xFF0D061A), Color(0xFF020104))
        )
        drawRect(skyGrad)

        // Mystical glowing background mist
        drawCircle(
            color = Color(0xFFB8143F).copy(alpha = mistAlpha * 0.4f),
            radius = w * 0.5f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Pulsing Crimson blood-moon glowing aura
        drawCircle(
            color = Color(0xFFE63956).copy(alpha = 0.15f),
            radius = (w * 0.14f) * moonAuraScale,
            center = Offset(w * 0.2f, h * 0.35f)
        )

        // The actual blood-red Moon
        drawCircle(
            color = Color(0xFF9E1B32),
            radius = w * 0.08f,
            center = Offset(w * 0.2f, h * 0.35f)
        )
        
        // Moon craters detailing
        drawCircle(
            color = Color(0xFF6E0D1E),
            radius = w * 0.02f,
            center = Offset(w * 0.18f, h * 0.32f)
        )
        drawCircle(
            color = Color(0xFF6E0D1E),
            radius = w * 0.015f,
            center = Offset(w * 0.22f, h * 0.38f)
        )

        // Silhouette of a haunted majestic gothic fortress in the horizon
        val fortPath = Path().apply {
            moveTo(w * 0.65f, h)
            lineTo(w * 0.65f, h * 0.55f)
            lineTo(w * 0.7f, h * 0.55f)
            lineTo(w * 0.7f, h * 0.48f)
            lineTo(w * 0.68f, h * 0.48f)
            lineTo(w * 0.72f, h * 0.32f) // Pointy tower peak 1
            lineTo(w * 0.76f, h * 0.48f)
            lineTo(w * 0.74f, h * 0.48f)
            lineTo(w * 0.74f, h * 0.55f)
            lineTo(w * 0.85f, h * 0.55f)
            
            // Giant central gothic window structure
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.88f, h * 0.2f) // Central high Spire
            lineTo(w * 0.91f, h * 0.35f)
            lineTo(w * 0.91f, h * 0.58f)

            lineTo(w, h * 0.58f)
            lineTo(w, h)
            close()
        }
        drawPath(fortPath, Color(0xFF040208))

        // Tiny warm golden windows of the fortress (showing inside horror)
        drawRoundRect(
            color = Color(0xFFFFAA00),
            topLeft = Offset(w * 0.87f, h * 0.38f),
            size = Size(w * 0.02f, h * 0.05f),
            cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
        )

        // Bare skeleton trees silhouettes on the sides
        val treePath = Path().apply {
            // Left Tree
            moveTo(w * 0.1f, h)
            quadraticTo(w * 0.12f, h * 0.65f, w * 0.08f, h * 0.45f)
            moveTo(w * 0.11f, h * 0.75f)
            quadraticTo(w * 0.02f, h * 0.68f, 0f, h * 0.65f)
            moveTo(w * 0.1f, h * 0.6f)
            quadraticTo(w * 0.22f, h * 0.52f, w * 0.28f, h * 0.5f)

            // Right Tree
            moveTo(w * 0.55f, h)
            quadraticTo(w * 0.52f, h * 0.7f, w * 0.48f, h * 0.55f)
            moveTo(w * 0.54f, h * 0.8f)
            quadraticTo(w * 0.62f, h * 0.75f, w * 0.68f, h * 0.72f)
        }
        drawPath(treePath, Color(0xFF05030A), style = Stroke(width = 4f))

        // Red glowing mist at the base of the banner
        val groundMist = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xAA73091E), Color(0xFF030106))
        )
        drawRect(groundMist, topLeft = Offset(0f, h * 0.7f), size = Size(w, h * 0.3f))
    }
}

@Composable
fun SpookyBellTowerCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF03050C), Color(0xFF0C1424), Color(0xFF020408))
        )
        drawRect(bgGrad)

        // Moon glow
        drawCircle(
            color = Color(0xFFBACEDB).copy(alpha = 0.3f),
            radius = w * 0.22f,
            center = Offset(w * 0.75f, h * 0.32f)
        )
        drawCircle(
            color = Color(0xFFE3EDF5),
            radius = w * 0.15f,
            center = Offset(w * 0.75f, h * 0.32f)
        )

        // Heavy dark hill base
        val hill = Path().apply {
            moveTo(0f, h)
            quadraticTo(w * 0.5f, h * 0.78f, w, h * 0.88f)
            lineTo(w, h)
            close()
        }
        drawPath(hill, Color(0xFF020408))

        // Giant gothic Bell Tower outline
        val tower = Path().apply {
            moveTo(w * 0.35f, h * 0.9f)
            lineTo(w * 0.38f, h * 0.45f) // Left main pillar
            lineTo(w * 0.32f, h * 0.45f)
            lineTo(w * 0.32f, h * 0.4f)  // Deck
            lineTo(w * 0.62f, h * 0.4f)
            lineTo(w * 0.62f, h * 0.45f)
            lineTo(w * 0.56f, h * 0.45f)
            lineTo(w * 0.59f, h * 0.9f)  // Right main pillar
            close()

            // Triangular pointy spooky roof
            moveTo(w * 0.32f, h * 0.4f)
            lineTo(w * 0.47f, h * 0.12f)
            lineTo(w * 0.62f, h * 0.4f)
            close()
        }
        drawPath(tower, Color(0xFF04060C))

        // Small glowing bell inside the tower deck
        drawCircle(
            color = Color(0xFFDEC595),
            radius = w * 0.05f,
            center = Offset(w * 0.47f, h * 0.48f)
        )
    }
}

@Composable
fun SpookyWindowCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark teal/green foggy glow outside
        val fog = Brush.radialGradient(
            colors = listOf(Color(0xFF0A3333), Color(0xFF030F0F)),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.7f
        )
        drawRect(fog)

        // Spooky hand prints or face outline silhouette outside window
        drawCircle(Color(0xFF010505), w * 0.09f, Offset(w * 0.5f, h * 0.4f)) // Ghost head
        val torso = Path().apply {
            moveTo(w * 0.41f, h * 0.49f)
            lineTo(w * 0.59f, h * 0.49f)
            lineTo(w * 0.7f, h * 0.9f)
            lineTo(w * 0.3f, h * 0.9f)
            close()
        }
        drawPath(torso, Color(0xFF010505))

        // Bloody hand markings dripping on glass
        drawCircle(Color(0xFF8C0E26), w * 0.02f, Offset(w * 0.44f, h * 0.52f))
        drawLine(Color(0xFF8C0E26), Offset(w * 0.44f, h * 0.52f), Offset(w * 0.44f, h * 0.62f), 3f)
        
        drawCircle(Color(0xFF8C0E26), w * 0.015f, Offset(w * 0.55f, h * 0.47f))
        drawLine(Color(0xFF8C0E26), Offset(w * 0.55f, h * 0.47f), Offset(w * 0.55f, h * 0.55f), 2f)

        // Window iron grates
        val ironColor = Color(0xFF0D0D0D)
        val borderW = 12.dp.toPx()
        drawRect(ironColor, style = Stroke(width = borderW))
        drawLine(ironColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), 10f)
        drawLine(ironColor, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 10f)
    }
}

@Composable
fun SpookyCorridorCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark grey walls fading to scary pitch black center door
        val bgGrad = Brush.radialGradient(
            colors = listOf(Color(0xFF020104), Color(0xFF130E1A)),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.65f
        )
        drawRect(bgGrad)

        // Convergence corridor lines (giving 3D depth)
        val stroke = 3f
        val color = Color(0xFF332645)
        drawLine(color, Offset(0f, 0f), Offset(w * 0.4f, h * 0.4f), stroke)
        drawLine(color, Offset(w, 0f), Offset(w * 0.6f, h * 0.4f), stroke)
        drawLine(color, Offset(0f, h), Offset(w * 0.4f, h * 0.6f), stroke)
        drawLine(color, Offset(w, h), Offset(w * 0.6f, h * 0.6f), stroke)

        // Endless doorway frame at center
        drawRect(
            color = Color(0xFF010002),
            topLeft = Offset(w * 0.4f, h * 0.4f),
            size = Size(w * 0.2f, h * 0.2f)
        )

        // Red glowing demon eyes looking from inside the deep doorway
        drawCircle(Color(0xFFFF1A4D), w * 0.015f, Offset(w * 0.47f, h * 0.5f))
        drawCircle(Color(0xFFFF1A4D), w * 0.015f, Offset(w * 0.53f, h * 0.5f))
        
        // Radioactive toxic green light droplets on the stone ceiling
        drawCircle(Color(0xFF39FF14), w * 0.025f, Offset(w * 0.5f, h * 0.15f))
        drawCircle(Color(0xFF39FF14).copy(alpha = 0.4f), w * 0.05f, Offset(w * 0.5f, h * 0.15f))
    }
}

@Composable
fun SpookyTentaclesCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark marsh green mist background
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF010503), Color(0xFF091C13), Color(0xFF010503))
        )
        drawRect(bgGrad)

        // Glowing particles in radioactive green
        drawCircle(Color(0xBB39FF14), 4f, Offset(w * 0.2f, h * 0.3f))
        drawCircle(Color(0x9939FF14), 6f, Offset(w * 0.75f, h * 0.45f))
        drawCircle(Color(0xAA39FF14), 5f, Offset(w * 0.45f, h * 0.75f))

        // Creepy alien/occult tentacles rising
        val tentacleColor = Color(0xFF301934)
        val highlightColor = Color(0xFF7B1FA2)

        // Left Tentacle
        val leftPath = Path().apply {
            moveTo(w * 0.15f, h)
            cubicTo(w * 0.28f, h * 0.7f, w * 0.02f, h * 0.45f, w * 0.45f, h * 0.22f)
        }
        drawPath(leftPath, tentacleColor, style = Stroke(width = 18f, cap = StrokeCap.Round))
        drawPath(leftPath, highlightColor, style = Stroke(width = 6f, cap = StrokeCap.Round))

        // Right Tentacle
        val rightPath = Path().apply {
            moveTo(w * 0.85f, h)
            cubicTo(w * 0.72f, h * 0.65f, w * 0.95f, h * 0.4f, w * 0.55f, h * 0.18f)
        }
        drawPath(rightPath, tentacleColor, style = Stroke(width = 20f, cap = StrokeCap.Round))
        drawPath(rightPath, highlightColor, style = Stroke(width = 7f, cap = StrokeCap.Round))

        // Sucking cups circles along the tentacle
        drawCircle(Color(0xFFBA68C8), w * 0.02f, Offset(w * 0.35f, h * 0.41f))
        drawCircle(Color(0xFFBA68C8), w * 0.02f, Offset(w * 0.62f, h * 0.38f))
    }
}

@Composable
fun SpookySilhouettedPathCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deep spooky forest sky gradient
        val sky = Brush.verticalGradient(
            colors = listOf(Color(0xFF03010A), Color(0xFF140722), Color(0xFF040209))
        )
        drawRect(sky)

        // Golden glowing occult portal/light at the horizon
        drawCircle(
            color = Color(0xFFDEC595).copy(alpha = 0.2f),
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.45f)
        )
        drawCircle(
            color = Color(0xFFDEC595).copy(alpha = 0.5f),
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.45f)
        )
        drawCircle(
            color = Color(0xFFFFFFFF),
            radius = w * 0.03f,
            center = Offset(w * 0.5f, h * 0.45f)
        )

        // Curved scary path leading into the portal
        val path = Path().apply {
            moveTo(w * 0.48f, h * 0.45f)
            cubicTo(
                w * 0.42f, h * 0.6f,
                w * 0.75f, h * 0.75f,
                w * 0.2f, h
            )
            lineTo(w * 0.8f, h)
            cubicTo(
                w * 0.65f, h * 0.75f,
                w * 0.55f, h * 0.6f,
                w * 0.52f, h * 0.45f
            )
            close()
        }
        val pathGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFFDEC595).copy(alpha = 0.3f), Color(0xFF3B2A10).copy(alpha = 0.8f))
        )
        drawPath(path, pathGrad)

        // Spooky silhouettes of dense trees framing the pathway
        val leftForest = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.35f)
            lineTo(w * 0.15f, h * 0.45f)
            lineTo(w * 0.08f, h * 0.55f)
            lineTo(w * 0.25f, h * 0.65f)
            lineTo(w * 0.12f, h * 0.75f)
            lineTo(w * 0.35f, h * 0.88f)
            lineTo(w * 0.1f, h * 0.92f)
            lineTo(w * 0.4f, h)
            close()
        }
        drawPath(leftForest, Color(0xFF040209))

        val rightForest = Path().apply {
            moveTo(w, h)
            lineTo(w, h * 0.38f)
            lineTo(w * 0.82f, h * 0.48f)
            lineTo(w * 0.9f, h * 0.58f)
            lineTo(w * 0.72f, h * 0.68f)
            lineTo(w * 0.85f, h * 0.78f)
            lineTo(w * 0.62f, h * 0.9f)
            lineTo(w * 0.8f, h * 0.93f)
            lineTo(w * 0.55f, h)
            close()
        }
        drawPath(rightForest, Color(0xFF040209))
    }
}

// ==========================================
// CENTRALIZED COMPOSABLES & VIEW ENGINE
// ==========================================

// Premium Gothic Frame Border Modifier with Antique Gold Corner Accents
fun Modifier.gothicBorder(
    borderColor: Color = Color(0xFFDEC595), // Vintage Antique Gold
    alpha: Float = 0.5f,
    cornerRadiusDp: Float = 12f
) = this.drawBehind {
    val r = cornerRadiusDp.dp.toPx()
    val strokeWidth = 1.8.dp.toPx()
    val gold = borderColor.copy(alpha = alpha)
    
    // Draw thin elegant border
    drawRoundRect(
        color = gold,
        size = size,
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = strokeWidth)
    )

    // Corner L-accents for a beautiful medieval scroll look
    val length = 14.dp.toPx()
    val accentStroke = 3.dp.toPx()

    // Top-Left L
    drawLine(gold, Offset(0f, 0f), Offset(length, 0f), accentStroke)
    drawLine(gold, Offset(0f, 0f), Offset(0f, length), accentStroke)

    // Top-Right L
    drawLine(gold, Offset(size.width, 0f), Offset(size.width - length, 0f), accentStroke)
    drawLine(gold, Offset(size.width, 0f), Offset(size.width, length), accentStroke)

    // Bottom-Left L
    drawLine(gold, Offset(0f, size.height), Offset(length, size.height), accentStroke)
    drawLine(gold, Offset(0f, size.height), Offset(0f, size.height - length), accentStroke)

    // Bottom-Right L
    drawLine(gold, Offset(size.width, size.height), Offset(size.width - length, size.height), accentStroke)
    drawLine(gold, Offset(size.width, size.height), Offset(size.width, size.height - length), accentStroke)
}

@Composable
fun MirrorCracksCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val silver = Color(0x22BACEDB)
        val stroke = 2f

        // Jagged cracklines of a broken ghost mirror
        val cracks = listOf(
            listOf(w * 0.8f to h * 0.15f, w * 0.5f to h * 0.35f, w * 0.32f to h * 0.55f, w * 0.15f to h * 0.85f),
            listOf(w * 0.8f to h * 0.15f, w * 0.92f to h * 0.45f, w * 0.98f to h * 0.78f),
            listOf(w * 0.5f to h * 0.35f, w * 0.6f to h * 0.7f, w * 0.52f to h * 0.95f),
            listOf(w * 0.32f to h * 0.55f, w * 0.12f to h * 0.52f, w * 0.02f to h * 0.7f),
            listOf(w * 0.8f to h * 0.15f, w * 0.6f to h * 0.08f)
        )

        cracks.forEach { points ->
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                drawLine(
                    color = silver,
                    start = Offset(p1.first, p1.second),
                    end = Offset(p2.first, p2.second),
                    strokeWidth = stroke
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(viewModel: HorrorViewModel, onOpenAdminLogin: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showAiStoryGeneratorDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    val isOnline by viewModel.isNetworkOnline.collectAsState()
    val grimFortunes by viewModel.grimFortunesList.collectAsState()
    val realStories by viewModel.realStoriesList.collectAsState()
    val scenarios by viewModel.scenariosList.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var activeReadingStory by remember { mutableStateOf<RealStory?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                if (!isOnline) {
                    Surface(
                        color = Color(0xFF4A0A17),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "اینترنت متصل نیست (ثبت بازدید و امتیاز متوقف شد)",
                                    color = Color(0xFFEDE8F5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = { viewModel.loadUserData() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("اتصال مجدد 🔄", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                val infiniteTransition = rememberInfiniteTransition(label = "bottomBarPulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.04f,
                    targetValue = 0.18f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                // STUNNING FLAT GOTHIC BOTTOM NAVIGATION
                Surface(
                    color = Color(0xFF030106),
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFB8143F).copy(alpha = 0.5f),
                                    Color(0xFFDEC595),
                                    Color(0xFFB8143F).copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = RectangleShape
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .drawBehind {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFB8143F).copy(alpha = pulseAlpha),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 5 OPTION TAB BUTTONS EXACTLY PORTRAYING THE SCREENSHOT
                            ScreenshotNavTab(
                                title = "داستان‌ها",
                                icon = Icons.Default.Home,
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            ScreenshotNavTab(
                                title = "طالع شوم",
                                icon = Icons.Default.AutoAwesome,
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            ScreenshotNavTab(
                                title = "ارسال داستان",
                                icon = Icons.Default.AddBox,
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                            ScreenshotNavTab(
                                title = "سناریو",
                                icon = Icons.Default.Explore,
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 }
                            )
                            ScreenshotNavTab(
                                title = "تنظیمات",
                                icon = Icons.Default.Settings,
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 }
                            )
                        }
                    }
                }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF050308))
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFB8143F)
                    )
                } else {
                    if (activeReadingStory != null) {
                        StoryReaderScreen(
                            story = activeReadingStory!!,
                            viewModel = viewModel,
                            onBack = { activeReadingStory = null }
                        )
                    } else {
                        when (selectedTab) {
                            0 -> BeautifulStoriesDashboard(
                                realStories = realStories,
                                viewModel = viewModel,
                                onLogoClick = {
                                    logoTapCount++
                                    if (logoTapCount >= 7) {
                                        logoTapCount = 0
                                        onOpenAdminLogin()
                                    }
                                },
                                onStoryRead = { selected ->
                                    if (!NetworkUtils.isOnline(context)) {
                                        showNoInternetDialog = true
                                    } else {
                                        activeReadingStory = selected
                                    }
                                }
                            )
                            1 -> GrimFortuneScreen(grimFortunes, viewModel)
                            2 -> BeautifulSubmitStoryScreen(viewModel) {
                                selectedTab = 0 // Navigate back to stories tab
                            }
                            3 -> WrongChoiceSection(scenarios, viewModel)
                            4 -> GorgeousSettingsScreen(viewModel, onOpenAdminLogin)
                        }
                    }
                }
            }
        }
    }

    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            containerColor = Color(0xFF140C22),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFFB8143F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "عدم اتصال به اینترنت",
                        color = Color(0xFFDEC595),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "برای گشایش این لوح کهن، ثبت بازدید و امتیاز در کتیبه‌های عمارت، اتصال به شبکه اینترنت الزامی است. لطفاً ارتباط خود را متصل نمایید و مجدداً تلاش کنید.",
                    color = Color(0xFFEDE8F5),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoInternetDialog = false
                        viewModel.loadUserData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F))
                ) {
                    Text("تلاش مجدد", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoInternetDialog = false }) {
                    Text("بستن", color = Color(0xFF8B8496))
                }
            }
        )
    }

    if (showAiStoryGeneratorDialog) {
        AIGeneratorDialog(viewModel) {
            showAiStoryGeneratorDialog = false
        }
    }
}

@Composable
fun ScreenshotNavTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgScale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "navTabScale"
    )

    Column(
        modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High-contrast filled background square for selected tab mimicking the screenshot
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Color(0xFFB8143F) else Color.Transparent)
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else Color(0xFF8B8496),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (selected) Color(0xFFDEC595) else Color(0xFF8B8496),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CinematicNavTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.22f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tabScale"
    )

    val animTranslationY by animateFloatAsState(
        targetValue = if (selected) -5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tabTranslation"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) Color(0xFFFF1A4D) else Color(0xFF8B8496),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "tabColor"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.32f else 0.0f,
        animationSpec = tween(durationMillis = 400),
        label = "tabGlow"
    )

    // Breathing soul flame size fluctuation
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
    val flameSizeMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameSize"
    )

    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = animTranslationY
                }
        ) {
            // Under-icon glow / aura
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFF1A4D).copy(alpha = glowAlpha), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                
                // Spooky little floating flame/droplet indicator
                Canvas(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw a miniature burning flame path
                    val flamePath = Path().apply {
                        moveTo(w * 0.5f, 0f) // Tip
                        quadraticTo(w * 0.9f * flameSizeMultiplier, h * 0.5f, w * 0.5f, h * flameSizeMultiplier)
                        quadraticTo(w * 0.1f * flameSizeMultiplier, h * 0.5f, w * 0.5f, 0f)
                    }
                    drawPath(flamePath, Color(0xFFFF1A4D))
                }
            }

            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.graphicsLayer {
                scaleX = if (selected) 1.05f else 1.0f
                scaleY = if (selected) 1.05f else 1.0f
            }
        )
    }
}

// ==========================================
// TAB 1: STORIES DASHBOARD (REAL & USER STORIES)
// ==========================================

@Composable
fun BeautifulStoriesDashboard(
    realStories: List<RealStory>,
    viewModel: HorrorViewModel,
    onLogoClick: () -> Unit,
    onStoryRead: (RealStory) -> Unit
) {
    var storyTab by remember { mutableIntStateOf(0) } // 0 = داستان‌های واقعی, 1 = داستان‌های شما
    var showSubmitDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = جدیدترین‌ها, 1 = داغ‌ترین‌ها, 2 = محبوب‌ترین‌ها ♥️
    
    // User Stories tab state
    var userSearchQuery by remember { mutableStateOf("") }
    var userFilterIndex by remember { mutableIntStateOf(0) } // 0 = جدیدترین‌ها, 1 = داغ‌ترین‌ها, 2 = محبوب‌ترین‌ها ♥️
    
    var isAmbientPlaying by remember { mutableStateOf(false) }
    val notifications by viewModel.notificationsList.collectAsState(initial = emptyList())

    // Filter and sort real stories
    val filteredStories = remember(realStories, searchQuery, selectedFilterIndex) {
        var list = if (searchQuery.isBlank()) {
            realStories
        } else {
            val q = searchQuery.trim().lowercase()
            realStories.filter {
                it.title.lowercase().contains(q) ||
                it.content.lowercase().contains(q) ||
                (it.author?.lowercase()?.contains(q) == true)
            }
        }

        when (selectedFilterIndex) {
            0 -> list.sortedByDescending { it.createdAt ?: it.id } // جدیدترین‌ها ⏳
            1 -> list.sortedByDescending { it.view_count }          // داغ‌ترین‌ها 🔥
            2 -> list.sortedByDescending { it.rating }              // محبوب‌ترین‌ها ♥️
            else -> list
        }
    }

    // Filter and sort user stories
    val userSubmissions by viewModel.userSubmissionsList.collectAsState(initial = emptyList())
    val filteredUserSubmissions = remember(userSubmissions, userSearchQuery, userFilterIndex) {
        val published = userSubmissions.filter { it.status == "PUBLISHED" }
        var list = if (userSearchQuery.isBlank()) {
            published
        } else {
            val q = userSearchQuery.trim().lowercase()
            published.filter {
                it.title.lowercase().contains(q) ||
                it.content.lowercase().contains(q) ||
                it.author_name.lowercase().contains(q)
            }
        }

        when (userFilterIndex) {
            0 -> list.sortedByDescending { it.createdAt ?: it.id } // جدیدترین‌ها ⏳
            1 -> list.sortedByDescending { it.view_count }          // داغ‌ترین‌ها 🔥
            2 -> list.sortedByDescending { it.rating }              // محبوب‌ترین‌ها ♥️
            else -> list
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MODERN SPOOKY HERO BANNER HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 14f)
                    .clickable { onLogoClick() }
            ) {
                ModernSpookyBannerCanvas(modifier = Modifier.fillMaxSize())
                
                // Vignette gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x66000000), Color(0xDD05020A))
                            )
                        )
                )

                // Notification Bell with Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x880E0718))
                            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                viewModel.setAppMode(AppMode.NOTIFICATIONS)
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "اعلان‌ها",
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (notifications.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(16.dp)
                                .background(Color(0xFFB8143F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${notifications.size}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Dynamic Farsi Typography titles centered at bottom of banner
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "عـمــارت وحـشــت",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color(0xFFDEC595),
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "ـ مـحـفـل روایـات واقعی و اعـتـرافات غـیـرمنتظر صـاحـب‌خانـه ـ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFD4C8E0),
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB SWITCHER (REAL STORIES VS USER STORIES)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF100B1A), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val t0 = storyTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (t0) Color(0xFFB8143F) else Color.Transparent)
                        .clickable {
                            storyTab = 0
                            HorrorSoundManager.playClickSound()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (t0) Color.White else Color(0xFF8B8496),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "داستان‌های واقعی",
                            color = if (t0) Color.White else Color(0xFF8B8496),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }

                val t1 = storyTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (t1) Color(0xFFB8143F) else Color.Transparent)
                        .clickable {
                            storyTab = 1
                            HorrorSoundManager.playClickSound()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = if (t1) Color.White else Color(0xFF8B8496),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "داستان‌های شما",
                            color = if (t1) Color.White else Color(0xFF8B8496),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: REAL STORIES WITH FLOATING SEARCH & FILTER CHIPS
            if (storyTab == 0) {
                // FLOATING SEARCH BAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F0918))
                        .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "جستجو در کتیبه‌ها و روایات کهن...",
                                    color = Color(0xFF6E687A),
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "پاک کردن",
                                    tint = Color(0xFF8B8496),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FILTER CHIPS ROW
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterLabels = listOf("جدیدترین‌ها ⏳", "داغ‌ترین‌ها 🔥", "محبوب‌ترین‌ها ♥️")
                    items(filterLabels.size) { idx ->
                        val isSelected = selectedFilterIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFB8143F) else Color(0xFF140C20))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFDEC595) else Color(0xFFDEC595).copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    selectedFilterIndex = idx
                                    HorrorSoundManager.playClickSound()
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filterLabels[idx],
                                color = if (isSelected) Color.White else Color(0xFF8B8496),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredStories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "روایتی مطابق با عبارت مورد جستجو یافت نشد." else "در حال بارگذاری روایات عتیقه...",
                            color = Color(0xFF8B8496),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        filteredStories.forEachIndexed { index, story ->
                            StoryItemCard(
                                story = story,
                                index = index,
                                onRead = {
                                    HorrorSoundManager.playPageTurnSound()
                                    viewModel.incrementStoryViews(story.id)
                                    onStoryRead(story)
                                }
                            )
                        }
                    }
                }
            } else {
                // TAB 1: USER STORIES / CONFESSIONS (روایات و اعترافات شما)
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // INTRO BANNER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.3f), cornerRadiusDp = 12f)
                            .background(Color(0xFF0F0918))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF170E24), CircleShape)
                                    .border(1.dp, Color(0xFFB8143F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFFB8143F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "طومار روایات و اعترافات شما",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFDEC595),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "روایات وحشتناک ارسالی شما پس از تایید در این بخش منتشر می‌شوند.",
                                textAlign = TextAlign.Center,
                                color = Color(0xFF8B8496),
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // USER STORIES SEARCH BAR
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0918))
                            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "جستجو",
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                placeholder = {
                                    Text(
                                        "جستجو در اعترافات و روایات کاربران...",
                                        color = Color(0xFF6E687A),
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (userSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { userSearchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "پاک کردن",
                                        tint = Color(0xFF8B8496),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // USER STORIES FILTER CHIPS ROW
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val userFilterLabels = listOf("جدیدترین‌ها ⏳", "داغ‌ترین‌ها 🔥", "محبوب‌ترین‌ها ♥️")
                        items(userFilterLabels.size) { idx ->
                            val isSelected = userFilterIndex == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFFB8143F) else Color(0xFF140C20))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFDEC595) else Color(0xFFDEC595).copy(alpha = 0.2f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        userFilterIndex = idx
                                        HorrorSoundManager.playClickSound()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = userFilterLabels[idx],
                                    color = if (isSelected) Color.White else Color(0xFF8B8496),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (filteredUserSubmissions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userSearchQuery.isNotEmpty()) "روایتی مطابق با جستجوی شما یافت نشد." else "هنوز روایتی در این بخش منتشر نشده است. با دکمه پایین اولین راوی باشید!",
                                color = Color(0xFF8B8496),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            filteredUserSubmissions.forEachIndexed { idx, sub ->
                                UserStoryItemCard(
                                    submission = sub,
                                    index = idx,
                                    onRead = {
                                        HorrorSoundManager.playPageTurnSound()
                                        viewModel.incrementSubmissionViews(sub.id)
                                        onStoryRead(sub.toRealStory())
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(70.dp)) // Padding for FAB
                }
            }
        }

        // FLOATING ACTION BUTTON (ONLY VISIBLE ON USER STORIES TAB)
        if (storyTab == 1) {
            FloatingActionButton(
                onClick = {
                    HorrorSoundManager.playClickSound()
                    showSubmitDialog = true
                },
                containerColor = Color(0xFFB8143F),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .border(1.5.dp, Color(0xFFDEC595), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "افزودن روایت",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "افزودن روایت شما",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showSubmitDialog) {
        SubmitStoryDialog(viewModel) {
            showSubmitDialog = false
        }
    }
}

// ==========================================
// STORY ITEM CARD WITH FULL-BLEED BACKGROUND AND TRANSPARENT OVERLAY
// ==========================================

@Composable
fun StoryItemCard(
    story: RealStory,
    index: Int,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.4f), cornerRadiusDp = 14f)
            .clickable { onRead() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0714))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed background image or atmospheric procedural canvas
            if (!story.cover_image_url.isNullOrBlank()) {
                AsyncImage(
                    model = story.cover_image_url,
                    placeholder = painterResource(id = R.drawable.img_horror_fallback_1788266589613),
                    error = painterResource(id = R.drawable.img_horror_fallback_1788266589613),
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val defaultRes = when (index % 3) {
                    0 -> R.drawable.img_poster_1_1788266550537
                    1 -> R.drawable.img_poster_2_1788266563762
                    else -> R.drawable.img_poster_3_1788266577786
                }
                androidx.compose.foundation.Image(
                    painter = painterResource(id = defaultRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark transparent gradient overlay covering whole card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33000000),
                                Color(0x66000000),
                                Color(0xB3000000)
                            )
                        )
                    )
            )

            // Card content structure
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Tag badge + Rating + Views
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFB8143F).copy(alpha = 0.85f))
                            .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (index % 2 == 0) "روایت باستانی" else "واقعه واقعی",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Rating & Views Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Star Rating Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xAA160C24))
                                .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", story.rating),
                                color = Color(0xFFDEC595),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // View Count Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xAA160C24))
                                .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF8B8496),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val displayViews = if (story.view_count >= 1000) "${story.view_count / 1000}k" else "${story.view_count}"
                            Text(
                                text = displayViews,
                                color = Color(0xFFD4C8E0),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Middle: Story Title & Summary
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = story.content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFFC7BED4),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom: Author info & Read Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "کاتب: ${story.author ?: "نامعلوم"}",
                        color = Color(0xFF8B8496),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Prominent Read Action Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB8143F))
                            .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مشاهده و خواندن",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
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
// TAB 2: GRIM FORTUNE OF SORCERY (طالع شوم معبد جادوگری)
// ==========================================

@Composable
fun GrimFortuneScreen(grimFortunes: List<GrimFortune>, viewModel: HorrorViewModel) {
    var selectedMonthIndex by remember { mutableIntStateOf(1) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val activeFortune = grimFortunes.find { it.month_index == selectedMonthIndex }
    val monthNames = HorrorViewModel.PERSIAN_MONTHS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
    ) {
        // High-end Gothic ambient crack canvas background
        MirrorCracksCanvas(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FULL-WIDTH HERO BANNER: GOTHIC SORCERY TEMPLE & DARK SORCERER WITH TOP TRANSPARENT HOVER GRADIENT
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    // Full-width Sorcery Temple background image
                    Image(
                        painter = painterResource(id = R.drawable.img_sorcery_temple_1788114860980),
                        contentDescription = "معبد جادوگری در تم گوتیک",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top and bottom seamless transparent hover gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xD9030106), // Top soft transparent shade for status bar
                                        Color(0x33030106), // Mid transparent viewing window
                                        Color(0x66B8143F), // Subtle crimson mystic aura
                                        Color(0xEE030106), // Bottom deep shadow
                                        Color(0xFF030106)  // Canvas merge
                                    )
                                )
                            )
                    )

                    // Floating Sorcerer Badge / Avatar & Titles
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Header Row with mystical tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xCC11081F),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFDEC595),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "معبد جادوگری",
                                        color = Color(0xFFDEC595),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xCCB8143F),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = "ماه: ${monthNames.getOrElse(selectedMonthIndex - 1) { "" }}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Center: Sorcerer Mystic Medallion / Grimoire portrait
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFDEC595), CircleShape)
                                .background(Color(0xFF0F081C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_dark_sorcerer_banner_1788114846553),
                                contentDescription = "جادوگر تاریک عمارت وحشت",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Bottom Title & Subtitle inside banner
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "طالع شوم معبد جادوگری",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 25.sp,
                                    color = Color(0xFFDEC595),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "ـ نـدای طـلـسـم و پـیـشـگـویـی شـوم جـادوگـر عـمـارت وحـشـت ـ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE5D5BC),
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // PRIMARY CALL TO ACTION: "طالع منو بگیر" BUTTON
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = {
                            showMonthPicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("get_my_fortune_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB8143F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFDEC595))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "طالع منو بگیر",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // FORTUNE DISPLAY CARD
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (activeFortune != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 14f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F081C)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header Row: Month Name & Doom Level Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFF1E0E2B),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFDEC595))
                                    ) {
                                        Text(
                                            text = "طالع متولدین ${activeFortune.month_name}",
                                            color = Color(0xFFDEC595),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }

                                    Surface(
                                        color = when (activeFortune.doom_level) {
                                            "نفرین ابدی" -> Color(0xFF8B0000)
                                            "بسیار شوم" -> Color(0xFFB8143F)
                                            else -> Color(0xFF4A154B)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "فرجام: ${activeFortune.doom_level ?: "شوم"}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Fortune Title
                                Text(
                                    text = activeFortune.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontFamily = FontFamily.Serif,
                                        textAlign = TextAlign.Center
                                    )
                                )

                                // Sorcerer Omen Chant / Poem Box
                                if (!activeFortune.omen_poem.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color(0xFFDEC595),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF553F1B))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = Color(0xFF553F1B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "« ${activeFortune.omen_poem} »",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color(0xFF2C1E0A),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    lineHeight = 22.sp,
                                                    textAlign = TextAlign.Center,
                                                    fontFamily = FontFamily.Serif
                                                )
                                            )
                                        }
                                    }
                                }

                                // Sorcerer interpretation & ominous warning
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "افسون و پیشگویی جادوگر:",
                                    color = Color(0xFFDEC595),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeFortune.fortune_text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFE2DCED),
                                        fontSize = 13.sp,
                                        lineHeight = 22.sp,
                                        textAlign = TextAlign.Justify
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Single Clean Action: Choose/Change birth month
                                OutlinedButton(
                                    onClick = { showMonthPicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color(0xFFDEC595)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تغییر ماه تولد و دریافت طالع جدید", color = Color(0xFFDEC595), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // MONTH PICKER DIALOG (ALL 12 PERSIAN MONTHS)
    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            containerColor = Color(0xFF0D0616),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ماه تولد خود را برگزینید",
                        color = Color(0xFFDEC595),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "طالع و پیشگویی شوم معبد جادوگری بر اساس ماه تولد برای شما آشکار خواهد شد:",
                        color = Color(0xFFDEC595).copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(monthNames) { index, name ->
                            val monthIdx = index + 1
                            val isSelected = selectedMonthIndex == monthIdx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMonthIndex = monthIdx
                                        showMonthPicker = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFB8143F) else Color(0xFF190F28)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFDEC595) else Color(0xFF3B2555)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$monthIdx",
                                        color = if (isSelected) Color(0xFFDEC595) else Color(0xFF8B8496),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = name,
                                        color = if (isSelected) Color.White else Color(0xFFDEC595),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMonthPicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("بستن", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// TAB 3: SCENARIOS (انتخاب سناریو و بازی تعاملی)
// ==========================================

@Composable
fun WrongChoiceSection(scenarios: List<WrongChoiceScenario>, viewModel: HorrorViewModel? = null) {
    var activeScenario by remember { mutableStateOf<WrongChoiceScenario?>(null) }
    var selectedCategory by remember { mutableStateOf("داغ‌ترین") }

    val categories = listOf("داغ‌ترین", "علوم غریبه", "روانی", "هیولاها")

    if (activeScenario == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030106))
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SCENARIOS HEADER
            Text(
                text = "سناریوهای شوم عمارت",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Color(0xFFDEC595)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ـ سرنوشت روح خود را با گرفتن تصمیمات درست نجات دهید ـ",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFDEC595).copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // HORIZONTAL CATEGORY SELECTOR (PILLS)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { cat ->
                    val isSel = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isSel) Color(0xFFB8143F) else Color(0xFF100B1A))
                            .border(
                                1.dp,
                                if (isSel) Color(0xFFDEC595) else Color(0xFF2B1C3D),
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) Color.White else Color(0xFF8B8496),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val filteredScenarios = remember(scenarios, selectedCategory) {
                when (selectedCategory) {
                    "علوم غریبه" -> scenarios.filterIndexed { index, _ -> index % 3 == 1 || index == 0 }
                    "روانی" -> scenarios.filterIndexed { index, _ -> index % 3 == 2 }
                    "هیولاها" -> scenarios.filterIndexed { index, _ -> index % 3 == 0 && index != 0 }
                    else -> scenarios // "داغ‌ترین"
                }
            }

            // 2-COLUMN PREMIUM PORTRAIT CARD POSTERS
            if (filteredScenarios.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("هنوز سناریویی در این دسته‌بندی بارگذاری نشده است.", color = Color(0xFF8B8496), fontSize = 12.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(filteredScenarios) { index, sc ->
                        GothicScenarioCard(
                            sc = sc,
                            index = index + 1,
                            onClick = { activeScenario = sc }
                        )
                    }
                }
            }
        }
    } else {
        InteractiveGamePlay(scenario = activeScenario!!, viewModel = viewModel) {
            activeScenario = null
        }
    }
}

@Composable
fun GothicScenarioCard(sc: WrongChoiceScenario, index: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.66f) // Modern Spooky tall poster aspect ratio
            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.35f), cornerRadiusDp = 12f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0714)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cover Artwork based on specific index
            when (index) {
                1 -> SpookyCorridorCanvas(modifier = Modifier.fillMaxSize())
                2 -> SpookyWindowCanvas(modifier = Modifier.fillMaxSize())
                3 -> SpookyTentaclesCanvas(modifier = Modifier.fillMaxSize())
                else -> SpookySilhouettedPathCanvas(modifier = Modifier.fillMaxSize())
            }

            // Premium gradient vignette over card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xBB000000), Color(0xFF030106))
                        )
                    )
            )

            // Index tag badge
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp)
                    .background(Color(0xDD000000), CircleShape)
                    .border(1.dp, Color(0xFFDEC595), CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = Color(0xFFDEC595),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }

            // Titles overlay bottom of poster
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = sc.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sc.description,
                        color = Color(0xFFD4C8E0),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Crimson launch scenario trigger button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(Color(0xFFB8143F), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "احضار گذرگاه مرگ",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// STORY SUBMISSION PARCHMENT DIALOG
// ==========================================

@Composable
fun SubmitStoryDialog(viewModel: HorrorViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = Color(0xFF0F0918),
        title = {
            Text(
                text = "ثبت روایت یا راز تاریخی جدید",
                color = Color(0xFFDEC595),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (submitted) {
                    Text(
                        text = "اعتراف گرانبهای شما با موفقیت در کتیبه‌ها ذخیره شد و پس از بررسی و تطهیر به نمایش درخواهد آمد.",
                        color = Color.White,
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp)
                    )
                } else {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFE63956),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it 
                            errorMessage = null
                        },
                        label = { Text("عنوان راز / کتیبه") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { 
                            content = it 
                            errorMessage = null
                        },
                        label = { Text("روایت حادثه ماورایی یا اعتراف شما") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("نام مستعار یا کاتب") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (!submitted) {
                Button(
                    onClick = {
                        if (title.isBlank() || content.isBlank()) {
                            errorMessage = "لطفاً عنوان و متن داستان را وارد کنید."
                            return@Button
                        }
                        isSubmitting = true
                        viewModel.submitUserStory(title.trim(), content.trim(), author.ifBlank { "ناشناس" }) { success ->
                            isSubmitting = false
                            if (success) {
                                submitted = true
                            } else {
                                errorMessage = "خطا در ثبت داستان، لطفاً اتصال اینترنت را بررسی کنید."
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
                        Text("ثبت در طومار", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("بستن", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!submitted && !isSubmitting) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = Color(0xFF8B8496))
                }
            }
        }
    )
}

// ==========================================
// SYSTEM PLAY SCENARIO SCREEN (MULTI-STAGE INTERACTIVE GAMEPLAY)
// ==========================================

@Composable
fun InteractiveGamePlay(
    scenario: WrongChoiceScenario,
    viewModel: HorrorViewModel? = null,
    onBack: () -> Unit
) {
    // Parse scenario into stages with dedicated choices
    val parsedStages = remember(scenario.id, scenario.description) {
        ScenarioParser.parse(scenario.description, scenario.title)
    }

    var dynamicStages by remember(scenario.id) { mutableStateOf(parsedStages) }
    var currentStageIdx by remember(scenario.id) { mutableIntStateOf(0) }
    var endingState by remember(scenario.id) { mutableStateOf<String?>(null) } // "DEAD", "SURVIVED"
    var endingNarrative by remember(scenario.id) { mutableStateOf<String?>(null) }
    var previousChoiceMade by remember(scenario.id) { mutableStateOf<String?>(null) }
    var isGeneratingNextAI by remember { mutableStateOf(false) }

    val currentStage = dynamicStages.getOrElse(currentStageIdx) {
        dynamicStages.lastOrNull() ?: ScenarioParsedStage(
            stageNumber = 1,
            stageTitle = scenario.title,
            narrative = scenario.description,
            choices = emptyList()
        )
    }

    val totalStages = dynamicStages.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP APP BAR / HEADER
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (endingState == null) {
                        Text(
                            text = "مرحله ${currentStageIdx + 1} از $totalStages",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp
                        )
                    }
                }
                // Restart button
                IconButton(onClick = {
                    currentStageIdx = 0
                    endingState = null
                    endingNarrative = null
                    previousChoiceMade = null
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "شروع مجدد", tint = Color(0xFFDEC595))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // STAGE PROGRESS INDICATOR BAR
            if (endingState == null && totalStages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until totalStages) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        i < currentStageIdx -> Color(0xFFDEC595)
                                        i == currentStageIdx -> Color(0xFFB8143F)
                                        else -> Color(0xFF2B1C3D)
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // NARRATIVE PARCHMENT SCENE BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(
                        borderColor = when (endingState) {
                            "DEAD" -> Color(0xFFB8143F)
                            "SURVIVED" -> Color(0xFF2D936C)
                            else -> Color(0xFFDEC595)
                        },
                        cornerRadiusDp = 14f
                    )
                    .background(Color(0xFF0F0918))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    if (endingState != null) {
                        // ENDING STATE PRESENTATION
                        val isDead = endingState == "DEAD"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDead) Icons.Default.Warning else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isDead) Color(0xFFE63956) else Color(0xFF2D936C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDead) "فرجام شوم: روح شما اسیر شد (مرگ)" else "فرجام نیک: بقا و رهایی از طلسم عمارت!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isDead) Color(0xFFE63956) else Color(0xFF2D936C),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 17.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Detailed Ending Narrative
                        Text(
                            text = endingNarrative ?: if (isDead) {
                                "تصمیم شوم شما را در دام ارواح عمارت گوتیک گرفتار کرد. شما در این گذرگاه جان باختید!"
                            } else {
                                "شما با شجاعت و هوشیاری از گذرگاه‌های مرگبار عبور کردید و طلسم کهن را در هم شکستید!"
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                lineHeight = 26.sp,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Justify
                            )
                        )
                    } else {
                        // CURRENT STAGE NARRATIVE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentStage.stageTitle,
                                color = Color(0xFFDEC595),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                fontSize = 13.sp
                            )
                            Surface(
                                color = Color(0xFFB8143F).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFB8143F))
                            ) {
                                Text(
                                    text = "مرحله ${currentStage.stageNumber}",
                                    color = Color(0xFFDEC595),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (!previousChoiceMade.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFF1E122E),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "نتیجه انتخاب پیشین: $previousChoiceMade",
                                    color = Color(0xFFDEC595),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Text(
                            text = currentStage.narrative,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFFEDE8F5),
                                lineHeight = 26.sp,
                                fontSize = 13.5.sp,
                                textAlign = TextAlign.Justify
                            )
                        )
                    }
                }
            }
        }

        // BOTTOM CHOICES BUTTONS PANEL
        if (endingState == null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "ـ تصمیم شما برای بقا در این مرحله چیست؟ ـ",
                    color = Color(0xFFDEC595),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Render each choice inside a dedicated interactive Gothic Button
                currentStage.choices.forEachIndexed { idx, choice ->
                    GothicChoiceButton(
                        choiceNumber = idx + 1,
                        text = choice.text,
                        onClick = {
                            if (choice.isDeath) {
                                endingState = "DEAD"
                                endingNarrative = choice.outcomeText ?: "تصمیم شوم «${choice.text}» شما را به تله مرگبار ارواح کشاند و کشته شدید!"
                            } else if (choice.isVictory) {
                                endingState = "SURVIVED"
                                endingNarrative = choice.outcomeText ?: "شما با انتخاب «${choice.text}» با موفقیت نجات یافتید و طلسم را باطل کردید!"
                            } else {
                                // Progress to next stage
                                val nextIdx = currentStageIdx + 1
                                if (nextIdx < dynamicStages.size) {
                                    previousChoiceMade = choice.outcomeText ?: choice.text
                                    currentStageIdx = nextIdx
                                } else {
                                    // If no more stages in scenario, conclude with survival or allow AI extension
                                    endingState = "SURVIVED"
                                    endingNarrative = choice.outcomeText ?: "شما با گذر موفق از تمامی دالان‌ها و تله‌های عمارت وحشت جان سالم به در بردید!"
                                }
                            }
                        }
                    )
                }

                // AI NEXT STAGE GENERATION (Infinite survival gameplay)
                if (viewModel != null) {
                    OutlinedButton(
                        onClick = {
                            isGeneratingNextAI = true
                            val prompt = "تو بازی‌گردان سناریوی وحشت گوتیک هستی. نام سناریو: ${scenario.title}.\n" +
                                    "بازیکن تا مرحله ${currentStageIdx + 1} پیش رفته و تصمیم اخیرش این بود: ${currentStage.narrative}\n" +
                                    "یک مرحله خطرناک بعدی به زبان فارسی بنویس با فرمت زیر:\n" +
                                    "---مرحله ${currentStageIdx + 2}---\n" +
                                    "روایت: [توصیف موقعیت بعدی]\n" +
                                    "گزینه ۱: [متن دکمه اول] -> [نتیجه]\n" +
                                    "گزینه ۲: [متن دکمه دوم (تله مرگ)] -> [مرگ]\n" +
                                    "گزینه ۳: [متن دکمه سوم] -> [بقا یا ادامه]"

                            viewModel.generateAILore(prompt) { aiResponse ->
                                isGeneratingNextAI = false
                                if (!aiResponse.startsWith("خطا")) {
                                    val newStages = ScenarioParser.parse(aiResponse, scenario.title)
                                    if (newStages.isNotEmpty()) {
                                        val nextStage = newStages[0].copy(
                                            stageNumber = dynamicStages.size + 1,
                                            stageTitle = "گذرگاه عمیق‌تر عمارت (مرحله ${dynamicStages.size + 1})"
                                        )
                                        dynamicStages = dynamicStages + nextStage
                                        currentStageIdx = dynamicStages.size - 1
                                        previousChoiceMade = "ادامه مسیر در عمق تاریکی با هدایت هوش مصنوعی..."
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF553F1B)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isGeneratingNextAI
                    ) {
                        if (isGeneratingNextAI) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFFDEC595),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال احضار مرحله بعدی...", color = Color(0xFFDEC595), fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("احضار مرحلهٔ بعدی ناشناخته با AI", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // GAME OVER / VICTORY BUTTONS
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Button(
                    onClick = {
                        // Restart scenario from stage 1
                        currentStageIdx = 0
                        endingState = null
                        endingNarrative = null
                        previousChoiceMade = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (endingState == "DEAD") Color(0xFFB8143F) else Color(0xFF2D936C)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFDEC595))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شروع مجدد این سناریو از ابتدا", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بازگشت به معبد سناریوها", color = Color(0xFFDEC595), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GothicChoiceButton(choiceNumber: Int, text: String, onClick: () -> Unit) {
    val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val numStr = if (choiceNumber in 1..9) persianDigits[choiceNumber] else "$choiceNumber"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF140C22))
            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Choice Number Badge inside the button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFB8143F), CircleShape)
                    .border(1.dp, Color(0xFFDEC595), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numStr,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Choice Text clearly placed INSIDE the button
            Text(
                text = text,
                color = Color(0xFFEDE8F5),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFDEC595).copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==========================================
// SINGLE STORY READER VIEW WITH ADJ STYLING
// ==========================================

@Composable
fun StoryReaderScreen(
    story: RealStory,
    viewModel: HorrorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Five premium Persian Font options mapping to Android System FontFamilies
    val fontOptions = remember {
        listOf(
            "پیش‌فرض سیستم" to FontFamily.Default,
            "نسخ مذهبی عتیق" to FontFamily.Serif,
            "کتیبه سنتی دیواری" to FontFamily.Monospace,
            "ایران‌سنس مدرن" to FontFamily.SansSerif,
            "دوات تحریری شکسته" to FontFamily.Cursive
        )
    }
    var selectedFontIndex by remember { mutableIntStateOf(0) }
    val selectedFont = fontOptions[selectedFontIndex]

    // Font size options for elegant scrolls
    val sizeOptions = remember {
        listOf(
            "بسیار کوچک" to 13f,
            "کوچک" to 15f,
            "متوسط" to 18f,
            "بزرگ" to 22f,
            "بسیار بزرگ" to 26f,
            "عتیق و غول‌آسا" to 32f
        )
    }
    var fontSizeMultiplier by remember { mutableFloatStateOf(18f) }

    val realStoriesState by viewModel.realStoriesList.collectAsState()
    val userSubmissionsState by viewModel.userSubmissionsList.collectAsState()
    
    val isUserSub = remember(story.id, userSubmissionsState) {
        userSubmissionsState.any { it.id == story.id } || 
        story.source == "روایات و اعترافات شما" || 
        (story.tags?.contains("روایت کاربر") == true || story.tags?.contains("اعترافات") == true)
    }

    val liveStory = remember(story, realStoriesState, userSubmissionsState, isUserSub) {
        if (isUserSub) {
            userSubmissionsState.find { it.id == story.id }?.toRealStory() ?: story
        } else {
            realStoriesState.find { it.id == story.id } ?: story
        }
    }

    val sharedPrefs = remember(context) { context.getSharedPreferences("horror_house_user_ratings", android.content.Context.MODE_PRIVATE) }
    var userRatingGiven by remember(story.id) {
        mutableIntStateOf(sharedPrefs.getFloat("rating_${story.id}", 0f).toInt())
    }
    var ratingSubmitted by remember(story.id) {
        mutableStateOf(sharedPrefs.contains("rating_${story.id}"))
    }
    
    var showFontMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(story.id) {
        if (NetworkUtils.isOnline(context)) {
            if (isUserSub) {
                viewModel.incrementSubmissionViews(story.id)
            } else {
                viewModel.incrementStoryViews(story.id)
            }
        }
    }

    val shareUrl = "https://ai.studio/build/horrorhouse/story?id=${story.id}"
    val deepLinkSchema = "horrorhouse://story?id=${story.id}"
    val shareMessage = """
📜 بخشی از روایت ترسناک «${story.title}» در عمارت وحشت:

«${story.content.take(150)}...»

👁️ برای خواندن کامل این لوح گرانبها و تجربه سناریوهای ماوراء الطبیعه عمارت، وارد معبد شوید:
🔗 لینک برنامه: $shareUrl
🔑 آدرس مستقیم کتیبه: $deepLinkSchema
""".trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            
            // FADED / BLURRED HERO CARD HEADER CONTAINING OPTIONS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.5f), cornerRadiusDp = 14f)
            ) {
                // Background Cover Image or Canvas
                if (!story.cover_image_url.isNullOrBlank()) {
                    AsyncImage(
                        model = story.cover_image_url,
                        placeholder = painterResource(id = R.drawable.img_horror_fallback_1788266589613),
                        error = painterResource(id = R.drawable.img_horror_fallback_1788266589613),
                        contentDescription = story.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val defaultRes = when (story.id.hashCode() % 3) {
                        0 -> R.drawable.img_poster_1_1788266550537
                        1 -> R.drawable.img_poster_2_1788266563762
                        else -> R.drawable.img_poster_3_1788266577786
                    }
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = defaultRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Blurred/Faded Translucent Overlay (Lighter opacity)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x33000000),
                                    Color(0x66000000),
                                    Color(0xB3000000)
                                )
                            )
                        )
                )

                // Back Button (Top Right Corner)
                IconButton(
                    onClick = {
                        HorrorSoundManager.playClickSound()
                        onBack()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color(0x88000000), CircleShape)
                        .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "بازگشت",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Interactive Option Icons in Top-Left Corner (Dropdown Menus Inside)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Family Selector Menu Button
                    Box {
                        IconButton(
                            onClick = {
                                HorrorSoundManager.playClickSound()
                                showFontMenu = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x88000000), CircleShape)
                                .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.TextFields,
                                contentDescription = "قلم کتیبه",
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showFontMenu,
                            onDismissRequest = { showFontMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF140C22))
                                .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            fontOptions.forEachIndexed { idx, (name, fontFamily) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = name,
                                            color = if (selectedFontIndex == idx) Color(0xFFB8143F) else Color.White,
                                            fontFamily = fontFamily,
                                            fontWeight = if (selectedFontIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        selectedFontIndex = idx
                                        showFontMenu = false
                                        HorrorSoundManager.playClickSound()
                                    }
                                )
                            }
                        }
                    }

                    // Font Size Selector Menu Button
                    Box {
                        IconButton(
                            onClick = {
                                HorrorSoundManager.playClickSound()
                                showSizeMenu = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x88000000), CircleShape)
                                .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.FormatSize,
                                contentDescription = "اندازه قلم",
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showSizeMenu,
                            onDismissRequest = { showSizeMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF140C22))
                                .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            sizeOptions.forEach { (label, value) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (fontSizeMultiplier == value) Color(0xFFB8143F) else Color.White,
                                            fontWeight = if (fontSizeMultiplier == value) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        fontSizeMultiplier = value
                                        showSizeMenu = false
                                        HorrorSoundManager.playClickSound()
                                    }
                                )
                            }
                        }
                    }
                }

                // Centered Story Info and Title inside the card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontFamily = selectedFont.second,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ـ مکتوب گرانبها در معبد عتیق ـ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8B8496),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // STORY NARRATIVE CONTENT BODY CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.35f), cornerRadiusDp = 12f)
                    .background(Color(0xFF0F0918))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Story Narrative Content Body
                    Text(
                        text = story.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFFEDE8F5),
                            lineHeight = (fontSizeMultiplier * 1.8f).sp,
                            fontSize = fontSizeMultiplier.sp,
                            fontFamily = selectedFont.second
                        )
                    )

                    HorizontalDivider(color = Color(0xFFDEC595).copy(alpha = 0.2f))

                    // AUTHOR NAME & SOURCE DETAILS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "راوی باستانی: ${story.author ?: "کاتبان عمارت وحشت"}",
                                color = Color(0xFFDEC595),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = selectedFont.second
                            )
                            Text(
                                text = "منبع: ${story.source ?: "طومارهای عتیق معبد"}",
                                color = Color(0xFF8B8496),
                                fontSize = 10.5.sp,
                                fontFamily = selectedFont.second
                            )
                        }

                        // Star stat (displays live average rating)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(String.format(java.util.Locale.US, "%.1f (%d رأی)", liveStory.rating, liveStory.rating_count), color = Color(0xFFDEC595), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. AVERAGE RATING STATS (Filled Stars visually showing average)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F0B18))
                            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "میانگین امتیاز کل کتیبه",
                                color = Color(0xFF8B8496),
                                fontSize = 11.sp,
                                fontFamily = selectedFont.second
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { star ->
                                    // Calculate if filled
                                    val isFilled = star <= liveStory.rating
                                    Icon(
                                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (isFilled) Color(0xFFFFD700) else Color(0xFF4C4556),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f از ۵ (%d رأی)", liveStory.rating, liveStory.rating_count),
                                    color = Color(0xFFDEC595),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = selectedFont.second
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. INTERACTIVE USER RATING SYSTEM (Saves to Room DB & Supabase Remote DB)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF140C22))
                            .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (ratingSubmitted) "رأی شما با موفقیت ثبت شد!" else "میزان وحشت و گیرایی این داستان از نظر شما چطور بود؟",
                                color = if (ratingSubmitted) Color(0xFFDEC595) else Color(0xFFEDE8F5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = selectedFont.second
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { star ->
                                    val isFilled = star <= userRatingGiven
                                    IconButton(
                                        onClick = {
                                            if (!ratingSubmitted) {
                                                if (!NetworkUtils.isOnline(context)) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "❌ اتصال اینترنت برقرار نیست! ثبت رأی نیازمند اینترنت است.",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    sharedPrefs.edit().putFloat("rating_${story.id}", star.toFloat()).apply()
                                                    userRatingGiven = star
                                                    ratingSubmitted = true
                                                    HorrorSoundManager.playStarRatingSound(star)
                                                    
                                                    if (isUserSub) {
                                                        viewModel.rateUserSubmission(story.id, star.toFloat())
                                                    } else {
                                                        viewModel.rateStory(story.id, star.toFloat())
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "ستاره $star",
                                            tint = if (isFilled) Color(0xFFFFD700) else Color(0xFF8B8496),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (ratingSubmitted) {
                                Text(
                                    text = "امتیاز ثبت‌شده شما: $userRatingGiven ستاره از ۵ (حق رأی شما محفوظ است)",
                                    color = Color(0xFFDEC595),
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontFamily = selectedFont.second
                                )
                            } else {
                                Text(
                                    text = "💡 راهنما: امتیازدهی از راست به چپ افزایش می‌یابد؛ ستاره اول از راست = ۱ (کمترین) 🌟 چپ‌ترین ستاره = ۵ (بیشترین وحشت)",
                                    color = Color(0xFF8B8496),
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontFamily = selectedFont.second,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ADVANCED SOCIAL NETWORK SHARING SYSTEM (Dynamic Deep Link Schema)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "اشتراک‌گذاری طومار در شبکه‌های اجتماعی:",
                            color = Color(0xFF8B8496),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = selectedFont.second
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Telegram Share
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF229ED9).copy(alpha = 0.15f))
                                    .border(0.5.dp, Color(0xFF229ED9), RoundedCornerShape(8.dp))
                                    .clickable {
                                        HorrorSoundManager.playClickSound()
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = android.net.Uri.parse("https://t.me/share/url?url=${android.net.Uri.encode(shareUrl)}&text=${android.net.Uri.encode(shareMessage)}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback standard share
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری در تلگرام"))
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("تلگرام ✈️", color = Color(0xFF229ED9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // WhatsApp Share
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF25D366).copy(alpha = 0.15f))
                                    .border(0.5.dp, Color(0xFF25D366), RoundedCornerShape(8.dp))
                                    .clickable {
                                        HorrorSoundManager.playClickSound()
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = android.net.Uri.parse("https://api.whatsapp.com/send?text=${android.net.Uri.encode(shareMessage)}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری در واتساپ"))
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("واتساپ 💬", color = Color(0xFF25D366), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Copy Link Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDEC595).copy(alpha = 0.15f))
                                    .border(0.5.dp, Color(0xFFDEC595), RoundedCornerShape(8.dp))
                                    .clickable {
                                        HorrorSoundManager.playClickSound()
                                        try {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("طومار وحشت", deepLinkSchema)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "نشانی عتیقه کتیبه با موفقیت کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {}
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("کپی نشانی 🔗", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FOOTER MAIN CLOSE ACTION BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFB8143F))
                .clickable {
                    HorrorSoundManager.playClickSound()
                    onBack()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("بستن کتیبه راز", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun BeautifulSubmitStoryScreen(viewModel: HorrorViewModel, onSubmissionComplete: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "کتیبه ارسال رازها",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFFDEC595)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ـ رازها و اعترافات ماوراء الطبیعه خود را در دفترچه عمارت ثبت کنید ـ",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF8B8496),
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.35f), cornerRadiusDp = 12f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (submitted) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2D936C),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "راز شما با موفقیت حک شد!",
                                color = Color(0xFFDEC595),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "پس از تطهیر کاتبان، راز شما در بخش اعترافات عمارت به نمایش در خواهد آمد.",
                                color = Color(0xFF8B8496),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    title = ""
                                    content = ""
                                    author = ""
                                    submitted = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ثبت کتیبه جدید", color = Color.White)
                            }
                        }
                    }
                } else {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFE63956),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it 
                            errorMessage = null
                        },
                        label = { Text("عنوان راز / کتیبه") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { 
                            content = it 
                            errorMessage = null
                        },
                        label = { Text("روایت واقعه مرموز یا اعتراف شما") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("نام مستعار شما") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                errorMessage = "لطفاً عنوان و متن داستان را وارد کنید."
                                return@Button
                            }
                            isSubmitting = true
                            viewModel.submitUserStory(title.trim(), content.trim(), author.ifBlank { "ناشناس" }) { success ->
                                isSubmitting = false
                                if (success) {
                                    showSuccessDialog = true
                                    submitted = true
                                } else {
                                    errorMessage = "خطا در ثبت داستان، لطفاً اتصال اینترنت را بررسی کنید."
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "حکاکی روی کتیبه گوتیک",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSubmissionComplete()
            },
            containerColor = Color(0xFF0F0918),
            title = {
                Text(
                    text = "ارسال موفقیت‌آمیز داستان",
                    color = Color(0xFFDEC595),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Text(
                    text = "داستان شما با موفقیت به عمارت ارسال شد. ادمین پس از بررسی و تایید، داستان شما را منتشر خواهد کرد. زمان انتشار معمولاً بین ۱۵ دقیقه تا ۱۲ ساعت متغیر است.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSubmissionComplete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("فهمیدم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: HorrorViewModel, onBack: () -> Unit) {
    val notifications by viewModel.notificationsList.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اعلان‌های عمارت", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0918))
            )
        },
        containerColor = Color(0xFF050209)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1224))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = notification.title,
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = notification.message,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        if (!notification.imageUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            coil.compose.AsyncImage(
                                model = notification.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(notification.timestamp)),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GorgeousSettingsScreen(
    viewModel: HorrorViewModel,
    onOpenAdminLogin: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var spookyModeEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "تنظیمات عمارت",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFFDEC595)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ـ پیکربندی طنین‌ها و اتمسفر قلعه باستانی گوتیک ـ",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF8B8496),
                fontSize = 11.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ambient Sound Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.25f), cornerRadiusDp = 12f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "طنین‌های وحشت و ماوراء",
                    color = Color(0xFFDEC595),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("صداهای شوم پس‌زمینه", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("پخش افکت باد و شیون ارواح در تمام فضا", color = Color(0xFF8B8496), fontSize = 10.sp)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFB8143F),
                            checkedTrackColor = Color(0xFF1E1428)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("حالت تنفس قلعه گوتیک", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("نبض خون‌رنگ و لرزش‌های ملایم رابط کاربری", color = Color(0xFF8B8496), fontSize = 10.sp)
                    }
                    Switch(
                        checked = spookyModeEnabled,
                        onCheckedChange = { spookyModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFB8143F),
                            checkedTrackColor = Color(0xFF1E1428)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AIGeneratorDialog(viewModel: HorrorViewModel, onDismiss: () -> Unit) {
    var userPromptTheme by remember { mutableStateOf("") }
    var generatedStory by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0918),
        title = {
            Text(
                text = "احضار کاتب ارواح (هوش مصنوعی)",
                color = Color(0xFFDEC595),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFB8143F))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "در حال دمیدن در بوق بادگیرهای تاریخ...",
                                color = Color(0xFFDEC595),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (generatedStory.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "سرگذشت احضار شده:",
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.25f), cornerRadiusDp = 8f)
                                .background(Color(0xFF050308))
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Text(
                                text = generatedStory,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "یک ایده یا موضوع شوم وارد کنید (مثال: 'پنجره خیس خون'، 'راهبه بی‌سر'، 'کلبه جنگل سیاه') تا کاتب ارواح عمارت روایتی هولناک برایتان خلق کند:",
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = userPromptTheme,
                        onValueChange = { userPromptTheme = it },
                        label = { Text("موضوع روایت وحشت") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2B1C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (generatedStory.isNotEmpty() && !isGenerating) {
                Button(
                    onClick = {
                        userPromptTheme = ""
                        generatedStory = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("احضار مجدد", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (!isGenerating) {
                Button(
                    onClick = {
                        if (userPromptTheme.isNotBlank()) {
                            isGenerating = true
                            val prompt = "یک داستان تاریخی گوتیک کوتاه، هولناک و ترسناک درباره موضوع '$userPromptTheme' به زبان فارسی بنویس. داستان باید دارای نثری فخیم، سناریویی به شدت شگفت‌انگیز و تعلیقی باشد و حداکثر ۳ پاراگراف کوتاه داشته باشد."
                            viewModel.generateAILore(prompt) { result ->
                                generatedStory = result
                                isGenerating = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("دمیدن در ناقوس خلقت", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isGenerating) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف / خروج", color = Color(0xFF8B8496))
                }
            }
        }
    )
}

@Composable
fun UserStoryItemCard(
    submission: com.example.data.UserStorySubmission,
    index: Int,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.4f), cornerRadiusDp = 14f)
            .clickable { onRead() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0714))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed background image or atmospheric procedural canvas
            if (!submission.cover_image_url.isNullOrBlank()) {
                AsyncImage(
                    model = submission.cover_image_url,
                    placeholder = painterResource(id = R.drawable.img_horror_fallback_1788266589613),
                    error = painterResource(id = R.drawable.img_horror_fallback_1788266589613),
                    contentDescription = submission.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val defaultRes = when (index % 3) {
                    0 -> R.drawable.img_poster_1_1788266550537
                    1 -> R.drawable.img_poster_2_1788266563762
                    else -> R.drawable.img_poster_3_1788266577786
                }
                androidx.compose.foundation.Image(
                    painter = painterResource(id = defaultRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark transparent gradient overlay covering whole card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33000000),
                                Color(0x66000000),
                                Color(0xB3000000)
                            )
                        )
                    )
            )

            // Card content structure
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Tag badge + Rating + Views
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF8A1332).copy(alpha = 0.9f))
                            .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "روایت شما",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Rating & Views Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Star Rating Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xAA160C24))
                                .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", submission.rating),
                                color = Color(0xFFDEC595),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // View Count Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xAA160C24))
                                .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF8B8496),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val displayViews = if (submission.view_count >= 1000) "${submission.view_count / 1000}k" else "${submission.view_count}"
                            Text(
                                text = displayViews,
                                color = Color(0xFFD4C8E0),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Middle: Story Title & Summary
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = submission.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = submission.content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFFC7BED4),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom: Author info & Read Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "راوی: ${submission.author_name}",
                        color = Color(0xFF8B8496),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Prominent Read Action Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB8143F))
                            .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onRead() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "گشایش لوح",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSubmissionCard(submission: com.example.data.UserStorySubmission, index: Int) {
    UserStoryItemCard(submission = submission, index = index, onRead = {})
}
