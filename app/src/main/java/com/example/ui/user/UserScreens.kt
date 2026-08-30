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
import com.example.data.RealStory
import com.example.data.TimeMirrorContent
import com.example.data.WrongChoiceScenario
import com.example.ui.theme.*
import com.example.viewmodel.HorrorViewModel

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
    var selectedTab by remember { mutableIntStateOf(0) }
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showAiStoryGeneratorDialog by remember { mutableStateOf(false) }

    val timeMirrors by viewModel.timeMirrorList.collectAsState()
    val realStories by viewModel.realStoriesList.collectAsState()
    val scenarios by viewModel.scenariosList.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var activeReadingStory by remember { mutableStateOf<RealStory?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Scaffold(
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
                                title = "آینه زمان",
                                icon = Icons.Default.DateRange,
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
                                    activeReadingStory = selected
                                }
                            )
                            1 -> GothicCalendarTimeMirror(timeMirrors, viewModel)
                            2 -> BeautifulSubmitStoryScreen(viewModel) {
                                selectedTab = 0 // Navigate back to stories tab
                            }
                            3 -> WrongChoiceSection(scenarios)
                            4 -> GorgeousSettingsScreen(viewModel, onOpenAdminLogin)
                        }
                    }
                }
            }
        }
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
    var storyTab by remember { mutableIntStateOf(0) } // 0 = داستان‌های واقعی, 1 = داستان‌های شما (اعترافات)
    var showSubmitDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MODERN SPOOKY HERO BANNER HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 14f)
                .clickable { onLogoClick() }
        ) {
            ModernSpookyBannerCanvas(modifier = Modifier.fillMaxSize())
            
            // Vignette shadow over base
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xBB000000))
                        )
                    )
            )

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
                        fontSize = 30.sp,
                        color = Color(0xFFDEC595),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
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

        Spacer(modifier = Modifier.height(20.dp))

        // TAB SWITCHER & ACTION TRIGGER BUTTON ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF100B1A), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val t0 = storyTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (t0) Color(0xFFB8143F) else Color.Transparent)
                        .clickable { storyTab = 0 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "روایات واقعی",
                        color = if (t0) Color.White else Color(0xFF8B8496),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                val t1 = storyTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (t1) Color(0xFFB8143F) else Color.Transparent)
                        .clickable { storyTab = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اعترافات شما",
                        color = if (t1) Color.White else Color(0xFF8B8496),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Premium crimson red add button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB8143F))
                    .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { showSubmitDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "افزودن روایت",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // THE STORY LISTINGS OR CONFESSIONS BOX
        if (storyTab == 0) {
            if (realStories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFB8143F))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    realStories.forEachIndexed { index, story ->
                        StoryItemCard(
                            story = story,
                            index = index,
                            onRead = { onStoryRead(story) }
                        )
                    }
                }
            }
        } else {
            // USER STORIES / CONFESSIONS LAYOUT
            val userSubmissions by viewModel.userSubmissionsList.collectAsState(initial = emptyList())

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.3f), cornerRadiusDp = 12f)
                        .background(Color(0xFF0F0918))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF170E24), CircleShape)
                                .border(1.dp, Color(0xFFB8143F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color(0xFFB8143F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "بایگانی اعترافات ارسالی شما",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFDEC595),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "اسرار و داستان‌های ترسناکی که با ما در میان می‌گذارید، ابتدا بررسی و تطهیر خواهند شد و سپس در طومار کتیبه‌ها برای سایر بازدیدکنندگان عمارت وحشت به نمایش درمی‌آیند.\nرازهای پنهان خود را در کادر زیر برای کاتبان ارسال کنید.",
                            textAlign = TextAlign.Center,
                            color = Color(0xFF8B8496),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFB8143F))
                                .clickable { showSubmitDialog = true }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "ثبت اعتراف جدید",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Dynamic display of user-submitted stories
                userSubmissions.forEachIndexed { idx, sub ->
                    UserSubmissionCard(submission = sub, index = idx)
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

@Composable
fun StoryItemCard(
    story: RealStory,
    index: Int,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .gothicBorder(borderColor = Color(0xFF2B1C3D), cornerRadiusDp = 12f)
            .clickable { onRead() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0714)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant modern custom-rendered canvas for thumbnail
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                when (index % 3) {
                    0 -> SpookyBellTowerCanvas(modifier = Modifier.matchParentSize())
                    1 -> SpookyWindowCanvas(modifier = Modifier.matchParentSize())
                    else -> SpookyCorridorCanvas(modifier = Modifier.matchParentSize())
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Narrative Texts (translated beautifully)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!story.author.isNullOrBlank()) {
                    Text(
                        text = "روایت کاتب: ${story.author}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = story.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF8B8496),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "قرائت لوح گرانبها",
                        color = Color(0xFFDEC595),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: THE MIRROR OF TIME (آینه زمان با تقویم گوتیک)
// ==========================================

@Composable
fun GothicCalendarTimeMirror(timeMirrors: List<TimeMirrorContent>, viewModel: HorrorViewModel) {
    var selectedDateKey by remember { mutableStateOf("1405-06-26") }
    var currentMonthYear by remember { mutableStateOf("مهر ماه / OCTOBER") }

    val aiGeneratedDays = remember { mutableStateMapOf<String, TimeMirrorContent>() }
    var generatingLoreForDate by remember { mutableStateOf<String?>(null) }

    val activeNarrative = timeMirrors.find { it.date_key == selectedDateKey }
        ?: aiGeneratedDays[selectedDateKey]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
    ) {
        // High-end Mirror crack background overlay
        MirrorCracksCanvas(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TITLE HEADER WITH PARCHMENT MOTIF
            item {
                Text(
                    text = "آینه زمان",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Color(0xFFDEC595),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ـ مـنـعـکــس‌کـنـنــدهٔ اســرار زمـان‌های گـذشـتــه ـ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )
            }

            // GOTHIC SOLID PARCHMENT CALENDAR CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 10f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDEC595) // Gorgeous vintage gold/sand parchment color
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header with custom next/prev month arrows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF553F1B),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = currentMonthYear,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF3B2A10),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 15.sp
                                )
                            )
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = Color(0xFF553F1B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Persian Weekdays header row (RTL structure)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { d ->
                                Text(
                                    text = d,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF553F1B),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Divider(color = Color(0xFF553F1B).copy(alpha = 0.3f), thickness = 1.dp)

                        // 31 days mapping matching layout sequence
                        val totalDays = 31
                        val startingOffset = 0
                        val totalCells = totalDays + startingOffset

                        var cellIndex = 0
                        while (cellIndex < totalCells) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0..6) {
                                    val currentCell = cellIndex + col
                                    val dayNum = currentCell - startingOffset + 1
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentCell in startingOffset until totalCells) {
                                            val dateStr = "1405-06-${String.format("%02d", dayNum)}"
                                            val isSelected = selectedDateKey == dateStr
                                            
                                            val labelUnder = when (dayNum) {
                                                3 -> "ارواح"
                                                6 -> "سکوت"
                                                7 -> "چشم‌سوم"
                                                9 -> "کلاغ‌ها"
                                                10 -> "برزخ"
                                                12 -> "ارواح"
                                                13 -> "کلاغ‌ها"
                                                16, 17, 19, 21 -> "کلاغ‌ها"
                                                22 -> "برزخ"
                                                25 -> "خالی"
                                                28 -> "سرخ"
                                                else -> null
                                            }
                                            
                                            val isBellHighlighted = dayNum == 26 || dayNum == 27
                                            
                                            val cellBg = when {
                                                isBellHighlighted -> Color(0xFF130E1C)
                                                isSelected -> Color(0xFFB8143F).copy(alpha = 0.35f)
                                                else -> Color.Transparent
                                            }
                                            
                                            val textCol = when {
                                                isBellHighlighted -> Color(0xFFDEC595)
                                                dayNum == 28 -> Color(0xFFB8143F)
                                                else -> Color(0xFF3B2A10)
                                            }

                                             Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(1.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(cellBg)
                                                    .border(
                                                        if (isBellHighlighted) BorderStroke(1.5.dp, Color(0xFFDEC595))
                                                        else BorderStroke(0.dp, Color.Transparent),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { selectedDateKey = dateStr },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                // Fixed height upper box to align numbers horizontally and vertically
                                                Box(
                                                    modifier = Modifier.height(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = dayNum.toString(),
                                                        color = textCol,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        fontFamily = FontFamily.Serif
                                                    )
                                                }

                                                // Fixed height lower box to align labels/badges horizontally and vertically
                                                Box(
                                                    modifier = Modifier.height(10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isBellHighlighted) {
                                                        Text(
                                                            text = if (dayNum == 26) "ناقوس" else "عزا",
                                                            fontSize = 6.sp,
                                                            color = Color(0xFFDEC595),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else if (labelUnder != null) {
                                                        Text(
                                                            text = labelUnder,
                                                            fontSize = 7.sp,
                                                            color = if (dayNum == 28) Color(0xFFB8143F) else Color(0xFF553F1B).copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                cellIndex += 7
                            }
                        }
                    }
                }
            }

            // DYNAMIC NARRATIVE PREVIEW CARD AT BOTTOM
            item {
                if (activeNarrative == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.2f), cornerRadiusDp = 12f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color(0xFFDEC595).copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "کتیبه مفقوده برای این روز مرموز",
                                color = Color(0xFFDEC595),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "در لوح‌های تاریخی عمارت هیچ رویداد ثبت‌شده‌ای برای این تاریخ یافت نشد.",
                                color = Color(0xFF8B8496),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.3f), cornerRadiusDp = 12f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black)
                                ) {
                                    SpookyBellTowerCanvas(modifier = Modifier.fillMaxSize())
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "مکاشفه تاریخ منتخب",
                                        color = Color(0xFFDEC595),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activeNarrative.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = activeNarrative.narrative,
                                        color = Color(0xFF8B8496),
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDEC595))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val parts = selectedDateKey.split("-")
                                    val dNum = parts.lastOrNull() ?: "۲۶"
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "مورخ",
                                            color = Color(0xFF3B2A10),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$dNum مهر",
                                            color = Color(0xFF3B2A10),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var showFullDialog by remember { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFB8143F))
                                        .clickable { showFullDialog = true }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "قرائت کتیبه زمان",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (showFullDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showFullDialog = false },
                                        containerColor = Color(0xFF0F0918),
                                        title = {
                                            Text(
                                                text = activeNarrative.title,
                                                color = Color(0xFFDEC595),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        },
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 250.dp)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                Text(
                                                    text = activeNarrative.narrative,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = { showFullDialog = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("بستن لوح", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDEC595)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("۲۷", color = Color(0xFF3B2A10), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E1428)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("۲۸", color = Color(0xFFB8143F), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFB8143F), modifier = Modifier.size(10.dp))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF100B1A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8B8496), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: SCENARIOS (انتخاب سناریو و بازی تعاملی)
// ==========================================

@Composable
fun WrongChoiceSection(scenarios: List<WrongChoiceScenario>) {
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

            // 2-COLUMN PREMIUM PORTRAIT CARD POSTERS
            if (scenarios.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFB8143F))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(scenarios) { index, sc ->
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
        InteractiveGamePlay(scenario = activeScenario!!) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
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
                        onValueChange = { content = it },
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
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.submitUserStory(title, content, author.ifBlank { "ناشناس" }) { success ->
                                if (success) submitted = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ثبت در طومار", color = Color.White, fontWeight = FontWeight.Bold)
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
            if (!submitted) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = Color(0xFF8B8496))
                }
            }
        }
    )
}

// ==========================================
// SYSTEM PLAY SCENARIO SCREEN
// ==========================================

@Composable
fun InteractiveGamePlay(scenario: WrongChoiceScenario, onBack: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    var endingState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFDEC595),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(
                        borderColor = when (endingState) {
                            "DEAD" -> Color(0xFFB8143F)
                            "SURVIVED" -> Color(0xFF2D936C)
                            else -> Color(0xFFDEC595)
                        },
                        cornerRadiusDp = 12f
                    )
                    .background(Color(0xFF0F0918))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (endingState != null) {
                        Text(
                            text = when (endingState) {
                                "SURVIVED" -> "شما موفق به نجات روح خود شدید!"
                                "DEAD" -> "روح شما اسیر سیاهچال‌های قلعه شد..."
                                else -> "سرنوشت نامعلوم ماورایی..."
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (endingState == "DEAD") Color(0xFFE63956) else Color(0xFF2D936C),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = when (endingState) {
                                "SURVIVED" -> "با اتکا به عقل و احتیاط کامل از طلسم دیرینه عمارت وحشت نجات یافتید، اما صدای نجواها هرگز شما را رها نخواهد کرد..."
                                "DEAD" -> "تصمیم شوم شما را به چنگ ارواح تشنه قلعه گوتیک فرستاد. شما تسلیم تاریکی ابدی شدید!"
                                else -> "در اعماق سرد سیاهچال‌ها سرگردان ماندید بدون راه فرار یا امید نجات."
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "گذرگاه شوم: تصمیم‌گیری مرحله $currentStep",
                            color = Color(0xFFDEC595),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (currentStep == 1) scenario.description
                            else "صداهای موحش و لرزان از دالان‌های روبه‌رو به گوش می‌رسد و شعلهٔ مشعل‌ها به رنگ سرخ متمایل شده است. کدام انتخاب را ادامه می‌دهید؟",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                lineHeight = 28.sp,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }

        if (endingState == null) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DecisionButton(
                    text = "۱. ورود به دالان غربی تاریک قلعه",
                    onClick = { currentStep++ }
                )
                DecisionButton(
                    text = "۲. پیشروی مستقیم به سمت طنین ناقوس",
                    onClick = { currentStep++ }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB8143F))
                        .clickable { endingState = listOf("SURVIVED", "DEAD", "MYSTERY").random() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("مواجهه ناگهانی با مرگبارترین فرجام", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFB8143F))
                    .clickable(onClick = onBack)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("بازگشت به معبد فرجام‌ها", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DecisionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0918))
            .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFD4C8E0),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp)
        )
    }
}

// ==========================================
// SINGLE STORY READER VIEW WITH ADJ STYLING
// ==========================================

@Composable
fun StoryReaderScreen(story: RealStory, onBack: () -> Unit) {
    var fontSizeMultiplier by remember { mutableFloatStateOf(16f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                }
                Text(
                    text = "لوح عتیقه روایات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFDEC595),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Premium Font Size adjustment controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0918), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("اندازه قلم کتیبه:", color = Color(0xFF8B8496), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("کوچک" to 14f, "متوسط" to 17f, "بزرگ" to 22f).forEach { (label, value) ->
                        val isCurrent = fontSizeMultiplier == value
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) Color(0xFFB8143F) else Color(0xFF170E24))
                                .clickable { fontSizeMultiplier = value }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(label, color = if (isCurrent) Color.White else Color(0xFF8B8496), fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 12f)
                    .background(Color(0xFF0F0918))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFFE63956),
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeMultiplier + 4f).sp,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    if (!story.author.isNullOrBlank()) {
                        Text(
                            text = "راوی باستانی: ${story.author}",
                            color = Color(0xFFDEC595),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Divider(color = Color(0xFFDEC595).copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = story.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            lineHeight = (fontSizeMultiplier * 1.8f).sp,
                            fontSize = fontSizeMultiplier.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFB8143F))
                .clickable(onClick = onBack)
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
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
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
                        onValueChange = { content = it },
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
                            if (title.isNotBlank() && content.isNotBlank()) {
                                viewModel.submitUserStory(title, content, author.ifBlank { "ناشناس" }) { success ->
                                    if (success) {
                                        submitted = true
                                        onSubmissionComplete()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
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
fun UserSubmissionCard(submission: com.example.data.UserStorySubmission, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.25f), cornerRadiusDp = 12f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1E1428), CircleShape)
                            .border(1.dp, Color(0xFFB8143F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اعتراف مورخ پاییز شوم",
                        color = Color(0xFF8B8496),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2D936C).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تطهیر شده",
                        color = Color(0xFF2D936C),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = submission.title,
                color = Color(0xFFDEC595),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = submission.content,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "کاتب: ${submission.author_name}",
                    color = Color(0xFFDEC595).copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
