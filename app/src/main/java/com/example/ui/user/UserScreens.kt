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
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
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

// Spooky Canvas Drawings for Castle, Tower, Windows, Corridors, and Tentacles!

@Composable
fun SpookyCastleCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark sky gradient with green mist
        val skyGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF030D08), Color(0xFF0A2416), Color(0xFF05120B))
        )
        drawRect(skyGrad)

        // Draw misty green glowing aura in center background
        drawCircle(
            color = Color(0x3344EE99),
            radius = w * 0.4f,
            center = Offset(w * 0.5f, h * 0.45f)
        )

        // Draw moon
        drawCircle(
            color = Color(0xFFE2F4EB),
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.28f)
        )

        // Draw mountain / hill silhouette
        val hillPath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.7f)
            quadraticTo(w * 0.25f, h * 0.65f, w * 0.5f, h * 0.72f)
            quadraticTo(w * 0.75f, h * 0.6f, w, h * 0.75f)
            lineTo(w, h)
            close()
        }
        drawPath(hillPath, Color(0xFF020604))

        // Draw gothic castle towers silhouette
        val castlePath = Path().apply {
            // Main gate building
            moveTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.42f, h * 0.55f)
            lineTo(w * 0.58f, h * 0.55f)
            lineTo(w * 0.58f, h * 0.72f)
            
            // Left Tower
            moveTo(w * 0.36f, h * 0.72f)
            lineTo(w * 0.36f, h * 0.45f)
            lineTo(w * 0.34f, h * 0.45f)
            lineTo(w * 0.39f, h * 0.32f) // Pointy roof
            lineTo(w * 0.44f, h * 0.45f)
            lineTo(w * 0.42f, h * 0.45f)
            lineTo(w * 0.42f, h * 0.72f)

            // Right Tower
            moveTo(w * 0.58f, h * 0.72f)
            lineTo(w * 0.58f, h * 0.45f)
            lineTo(w * 0.56f, h * 0.45f)
            lineTo(w * 0.61f, h * 0.32f) // Pointy roof
            lineTo(w * 0.66f, h * 0.45f)
            lineTo(w * 0.64f, h * 0.45f)
            lineTo(w * 0.64f, h * 0.72f)

            // Center Spire
            moveTo(w * 0.48f, h * 0.55f)
            lineTo(w * 0.48f, h * 0.38f)
            lineTo(w * 0.46f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.22f) // Main sharp tower tip
            lineTo(w * 0.54f, h * 0.38f)
            lineTo(w * 0.52f, h * 0.38f)
            lineTo(w * 0.52f, h * 0.55f)
        }
        drawPath(castlePath, Color(0xFF030A06))

        // Draw spooky pine tree silhouettes on the sides
        val treesPath = Path().apply {
            // Left Tree 1
            moveTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.05f, h * 0.72f)
            lineTo(w * 0.1f, h * 0.72f)
            lineTo(w * 0.03f, h * 0.58f)
            lineTo(w * 0.08f, h * 0.58f)
            lineTo(w * 0.02f, h * 0.45f) // top
            lineTo(w * 0.14f, h * 0.58f)
            lineTo(w * 0.12f, h * 0.58f)
            lineTo(w * 0.2f, h * 0.72f)
            lineTo(w * 0.17f, h * 0.72f)
            lineTo(w * 0.25f, h * 0.85f)

            // Right Tree 1
            moveTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.75f, h * 0.7f)
            lineTo(w * 0.79f, h * 0.7f)
            lineTo(w * 0.72f, h * 0.55f)
            lineTo(w * 0.76f, h * 0.55f)
            lineTo(w * 0.7f, h * 0.42f) // top
            lineTo(w * 0.82f, h * 0.55f)
            lineTo(w * 0.8f, h * 0.55f)
            lineTo(w * 0.88f, h * 0.7f)
            lineTo(w * 0.85f, h * 0.7f)
            lineTo(w * 0.95f, h * 0.85f)
        }
        drawPath(treesPath, Color(0xFF010402))

        // Draw a tiny yellow glowing gate window
        drawRect(
            color = Color(0xFFFFAA00),
            topLeft = Offset(w * 0.485f, h * 0.62f),
            size = Size(w * 0.03f, h * 0.04f)
        )
    }
}

@Composable
fun SpookyBellTowerCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark navy night gradient
        val nightGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF050811), Color(0xFF0B1220), Color(0xFF020408))
        )
        drawRect(nightGrad)

        // Silver moon
        drawCircle(
            color = Color(0xFFD9E2EC),
            radius = w * 0.15f,
            center = Offset(w * 0.75f, h * 0.3f)
        )
        // Shadow crescent
        drawCircle(
            color = Color(0xFF050811),
            radius = w * 0.14f,
            center = Offset(w * 0.68f, h * 0.28f)
        )

        // Ground hill
        drawOval(
            color = Color(0xFF010204),
            topLeft = Offset(-w * 0.1f, h * 0.8f),
            size = Size(w * 1.2f, h * 0.4f)
        )

        // Bell Tower Silhouette
        val towerPath = Path().apply {
            moveTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.45f) // Tower base wall
            lineTo(w * 0.35f, h * 0.45f)
            lineTo(w * 0.35f, h * 0.4f)  // Bell deck base
            lineTo(w * 0.65f, h * 0.4f)
            lineTo(w * 0.65f, h * 0.45f)
            lineTo(w * 0.62f, h * 0.45f)
            lineTo(w * 0.65f, h * 0.85f) // Right wall
            close()
            
            // Roof
            moveTo(w * 0.35f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.15f) // Peak
            lineTo(w * 0.65f, h * 0.35f)
            close()
        }
        drawPath(towerPath, Color(0xFF04060A))

        // Spooky dead branch/tree
        val branchPath = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            quadraticTo(w * 0.1f, h * 0.6f, w * 0.05f, h * 0.5f)
            moveTo(w * 0.12f, h * 0.72f)
            quadraticTo(w * 0.2f, h * 0.65f, w * 0.25f, h * 0.6f)
        }
        drawPath(branchPath, Color(0xFF010204), style = Stroke(width = 3f))
    }
}

@Composable
fun SpookyCorridorCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark grey-purple corridor background
        drawRect(Color(0xFF0F0E13))

        // Linear perspective walls lines
        val wallLineColor = Color(0xFF262130)
        val strokeW = 2f

        // Top left corner line
        drawLine(wallLineColor, Offset(0f, 0f), Offset(w * 0.45f, h * 0.45f), strokeW)
        // Bottom left corner line
        drawLine(wallLineColor, Offset(0f, h), Offset(w * 0.45f, h * 0.55f), strokeW)
        // Top right corner line
        drawLine(wallLineColor, Offset(w, 0f), Offset(w * 0.55f, h * 0.45f), strokeW)
        // Bottom right corner line
        drawLine(wallLineColor, Offset(w, h), Offset(w * 0.55f, h * 0.55f), strokeW)

        // End door frame
        drawRect(
            color = Color(0xFF050507),
            topLeft = Offset(w * 0.45f, h * 0.45f),
            size = Size(w * 0.1f, h * 0.1f)
        )

        // Spooky red wall eyes/mouths
        // Left mouths
        drawOval(Color(0xFFB8143F), Offset(w * 0.15f, h * 0.35f), Size(w * 0.15f, h * 0.05f))
        drawOval(Color(0xFFB8143F), Offset(w * 0.12f, h * 0.6f), Size(w * 0.18f, h * 0.06f))
        // Right mouths
        drawOval(Color(0xFFB8143F), Offset(w * 0.72f, h * 0.3f), Size(w * 0.14f, h * 0.05f))
        drawOval(Color(0xFFB8143F), Offset(w * 0.68f, h * 0.55f), Size(w * 0.16f, h * 0.06f))

        // Dim glowing green lamps on ceiling
        drawCircle(Color(0xFF62FFB4), w * 0.02f, Offset(w * 0.5f, h * 0.2f))
        drawCircle(Color(0xFF62FFB4), w * 0.03f, Offset(w * 0.5f, h * 0.1f))
    }
}

@Composable
fun SpookyWindowCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark teal glowing sky outside window
        val skyGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF061B1B), Color(0xFF144D4D), Color(0xFF082222))
        )
        drawRect(skyGrad)

        // Glowing fog orb
        drawCircle(
            color = Color(0xFF49A5A5).copy(alpha = 0.4f),
            radius = w * 0.35f,
            center = Offset(w * 0.5f, h * 0.45f)
        )

        // Shadowy figure silhouette standing outside
        val headR = w * 0.08f
        val bodyCenterY = h * 0.55f
        drawCircle(
            color = Color(0xFF030A0A),
            radius = headR,
            center = Offset(w * 0.5f, h * 0.35f)
        )
        val bodyPath = Path().apply {
            moveTo(w * 0.5f - headR, h * 0.35f + headR * 0.8f)
            lineTo(w * 0.5f + headR, h * 0.35f + headR * 0.8f)
            lineTo(w * 0.65f, h * 0.9f)
            lineTo(w * 0.35f, h * 0.9f)
            close()
        }
        drawPath(bodyPath, Color(0xFF030A0A))

        // Window frame
        val borderW = 12.dp.toPx()
        val borderStroke = Stroke(width = borderW)
        drawRect(Color(0xFF010404), style = borderStroke)

        // Window grid cross lines
        drawLine(Color(0xFF010404), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), 8.dp.toPx())
        drawLine(Color(0xFF010404), Offset(0f, h * 0.35f), Offset(w, h * 0.35f), 8.dp.toPx())
        drawLine(Color(0xFF010404), Offset(0f, h * 0.65f), Offset(w, h * 0.65f), 8.dp.toPx())
    }
}

@Composable
fun SpookyTentaclesCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dim radioactive green glow floor/background
        val bgGrad = Brush.radialGradient(
            colors = listOf(Color(0xFF0E1A17), Color(0xFF040605)),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.8f
        )
        drawRect(bgGrad)

        // Draw creepy glowing ceiling grid lights
        drawLine(Color(0xFF55FFAA), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h * 0.2f), 4.dp.toPx())

        // Multiple winding purple monster tentacles extending upwards
        val tentacleColor = Color(0xFF4C1C5C)
        val tentacleShadow = Color(0xFF1E0A26)

        // Tentacle 1 - Left curving to center
        val p1 = Path().apply {
            moveTo(w * 0.1f, h)
            cubicTo(w * 0.2f, h * 0.8f, w * 0.05f, h * 0.6f, w * 0.45f, h * 0.4f)
            cubicTo(w * 0.55f, h * 0.35f, w * 0.5f, h * 0.25f, w * 0.48f, h * 0.2f)
        }
        drawPath(p1, tentacleColor, style = Stroke(width = 16f, cap = StrokeCap.Round))
        drawPath(p1, Color(0xFF8E3B9F), style = Stroke(width = 6f, cap = StrokeCap.Round)) // highlights

        // Tentacle 2 - Right curving to center
        val p2 = Path().apply {
            moveTo(w * 0.9f, h)
            cubicTo(w * 0.75f, h * 0.75f, w * 0.88f, h * 0.55f, w * 0.55f, h * 0.45f)
            cubicTo(w * 0.45f, h * 0.42f, w * 0.4f, h * 0.3f, w * 0.43f, h * 0.22f)
        }
        drawPath(p2, tentacleColor, style = Stroke(width = 18f, cap = StrokeCap.Round))
        drawPath(p2, Color(0xFF8E3B9F), style = Stroke(width = 8f, cap = StrokeCap.Round))

        // Tentacle 3 - Center ground loop
        val p3 = Path().apply {
            moveTo(w * 0.5f, h)
            quadraticTo(w * 0.3f, h * 0.75f, w * 0.55f, h * 0.65f)
            quadraticTo(w * 0.75f, h * 0.58f, w * 0.62f, h * 0.48f)
        }
        drawPath(p3, tentacleShadow, style = Stroke(width = 20f, cap = StrokeCap.Round))
        drawPath(p3, tentacleColor, style = Stroke(width = 14f, cap = StrokeCap.Round))
    }
}

@Composable
fun SpookySilhouettedPathCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark grey fog background
        val fogGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F1215), Color(0xFF2E353B), Color(0xFF0F1215))
        )
        drawRect(fogGrad)

        // Misty moon glow
        drawCircle(
            color = Color(0x22F3F2F7),
            radius = w * 0.3f,
            center = Offset(w * 0.5f, h * 0.4f)
        )

        // Spooky silhouetted figure in center
        drawCircle(Color(0xFF080B0D), w * 0.07f, Offset(w * 0.5f, h * 0.52f))
        val bodyPath = Path().apply {
            moveTo(w * 0.43f, h * 0.59f)
            lineTo(w * 0.57f, h * 0.59f)
            lineTo(w * 0.62f, h * 0.9f)
            lineTo(w * 0.38f, h * 0.9f)
            close()
        }
        drawPath(bodyPath, Color(0xFF080B0D))

        // Spooky barren trees on the sides
        val leftTree = Path().apply {
            moveTo(w * 0.15f, h)
            lineTo(w * 0.18f, h * 0.3f)
            moveTo(w * 0.17f, h * 0.6f)
            quadraticTo(w * 0.05f, h * 0.5f, 0f, h * 0.45f)
            moveTo(w * 0.18f, h * 0.45f)
            quadraticTo(w * 0.28f, h * 0.35f, w * 0.35f, h * 0.3f)
        }
        drawPath(leftTree, Color(0xFF050708), style = Stroke(width = 4f))

        val rightTree = Path().apply {
            moveTo(w * 0.85f, h)
            lineTo(w * 0.82f, h * 0.35f)
            moveTo(w * 0.83f, h * 0.65f)
            quadraticTo(w * 0.95f, h * 0.55f, w, h * 0.5f)
            moveTo(w * 0.82f, h * 0.5f)
            quadraticTo(w * 0.72f, h * 0.4f, w * 0.65f, h * 0.35f)
        }
        drawPath(rightTree, Color(0xFF050708), style = Stroke(width = 4f))
    }
}

// Custom Premium Gothic Frame Modifiers

fun Modifier.gothicBorder(
    borderColor: Color = Color(0xFFD49E34), // Antique Gold
    alpha: Float = 0.4f,
    cornerRadiusDp: Float = 16f
) = this.drawBehind {
    val r = cornerRadiusDp.dp.toPx()
    val strokeWidth = 1.5.dp.toPx()
    val gold = borderColor.copy(alpha = alpha)
    
    // Fine round rect border
    drawRoundRect(
        color = gold,
        size = size,
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = strokeWidth)
    )

    // Gothic corner L-accents
    val accentLen = 12.dp.toPx()
    val accentStroke = 2.5.dp.toPx()
    
    // Top-Left corner
    drawLine(gold, Offset(0f, 0f), Offset(accentLen, 0f), accentStroke)
    drawLine(gold, Offset(0f, 0f), Offset(0f, accentLen), accentStroke)

    // Top-Right corner
    drawLine(gold, Offset(size.width, 0f), Offset(size.width - accentLen, 0f), accentStroke)
    drawLine(gold, Offset(size.width, 0f), Offset(size.width, accentLen), accentStroke)

    // Bottom-Left corner
    drawLine(gold, Offset(0f, size.height), Offset(accentLen, size.height), accentStroke)
    drawLine(gold, Offset(0f, size.height), Offset(0f, size.height - accentLen), accentStroke)

    // Bottom-Right corner
    drawLine(gold, Offset(size.width, size.height), Offset(size.width - accentLen, size.height), accentStroke)
    drawLine(gold, Offset(size.width, size.height), Offset(size.width, size.height - accentLen), accentStroke)
}

// Crack Mirror Canvas for background overlays
@Composable
fun MirrorCracksCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val color = Color(0x33F3F2F7) // Semi-transparent silver/ash
        val strokeWidth = 1.8f

        // Mirror break focal point (top center-right)
        val fx = width * 0.78f
        val fy = height * 0.15f

        val paths = listOf(
            listOf(fx to fy, width * 0.52f to height * 0.32f, width * 0.35f to height * 0.58f, width * 0.12f to height * 0.85f),
            listOf(fx to fy, width * 0.88f to height * 0.48f, width * 0.96f to height * 0.88f),
            listOf(fx to fy, width * 0.58f to height * 0.12f, width * 0.22f to height * 0.08f),
            listOf(width * 0.52f to height * 0.32f, width * 0.58f to height * 0.68f, width * 0.48f to height * 0.92f),
            listOf(width * 0.35f to height * 0.58f, width * 0.18f to height * 0.52f, width * 0.03f to height * 0.72f),
            listOf(fx to fy, width * 0.95f to height * 0.1f)
        )

        paths.forEach { points ->
            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]
                drawLine(
                    color = color,
                    start = Offset(start.first * width, start.second * height),
                    end = Offset(end.first * width, end.second * height),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

// Map Pathway for drawing winding dotted line trail in "THE GARDEN OF STORIES"
@Composable
fun MapPathCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val goldPath = Color(0x3BD49E34) // Distinct semi-transparent gold trail
        
        val stroke = Stroke(
            width = 4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        )
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.38f) // Starts under Abyssal Keep castle
            cubicTo(
                w * 0.18f, h * 0.45f, // Winding left to Whispering Woods / Oak of Dread
                w * 0.82f, h * 0.62f, // Winding right near Lamentation River
                w * 0.5f, h * 0.82f  // Ends at The Hollow
            )
        }
        drawPath(path, goldPath, style = stroke)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(viewModel: HorrorViewModel, onOpenAdminLogin: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var logoTapCount by remember { mutableIntStateOf(0) }

    val timeMirrors by viewModel.timeMirrorList.collectAsState()
    val realStories by viewModel.realStoriesList.collectAsState()
    val scenarios by viewModel.scenariosList.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Screen reader states
    var activeReadingStory by remember { mutableStateOf<RealStory?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                // Elegant Dark Red glowing Gothic Nav bar exactly as shown in the screenshot
                Surface(
                    color = Color(0xFF060408),
                    modifier = Modifier
                        .background(Color(0xFF010003))
                        .border(BorderStroke(0.5.dp, Color(0xFF1E1629)))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Column {
                        Divider(color = Color(0xFF1C1326), thickness = 1.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CinematicNavTab(
                                title = "داستان‌ها",
                                icon = Icons.Default.AutoStories,
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            CinematicNavTab(
                                title = "آینه زمان",
                                icon = Icons.Default.HourglassEmpty,
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            CinematicNavTab(
                                title = "سناریوها",
                                icon = Icons.Default.Explore,
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                            CinematicNavTab(
                                title = "نقشه عمارت",
                                icon = Icons.Default.Map,
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF0A050F))
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BloodGlow
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
                            1 -> GothicCalendarTimeMirror(timeMirrors)
                            2 -> WrongChoiceSection(scenarios)
                            3 -> ImmersiveMapDashboard(
                                onNavigateTimeMirror = { selectedTab = 1 },
                                onNavigateWrongChoice = { selectedTab = 2 },
                                onNavigateStories = { selectedTab = 0 },
                                onLogoClick = {
                                    logoTapCount++
                                    if (logoTapCount >= 7) {
                                        logoTapCount = 0
                                        onOpenAdminLogin()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CinematicNavTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) Color(0xFFE63956) else Color(0xFF6B5B95)

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp)
        ) {
            // Glow behind the icon if selected
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFE63956).copy(alpha = 0.25f), CircleShape)
                )
            }
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.Serif
            )
        )
    }
}

// SCREEN 1: BEAUTIFUL STORIES DASHBOARD (REAL & USER STORIES)
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
            .background(Color(0xFF030005))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dynamic Cinematic Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 14f)
                .clickable { onLogoClick() }
        ) {
            // Interactive background canvas
            SpookySilhouettedPathCanvas(modifier = Modifier.fillMaxSize())
            
            // Dark vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xDD000000))
                        )
                    )
            )

            // Centered Title Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ـ روایات واقعی و اعترافات هولناک سرگردان ـ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector and Submission Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Tab Row (داستان‌های واقعی / داستان‌های شما)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF130E1C), RoundedCornerShape(10.dp))
                    .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                val t0 = storyTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (t0) Color(0xFFB8143F) else Color.Transparent)
                        .clickable { storyTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "داستان‌های واقعی",
                        color = if (t0) Color.White else Color(0xFF8B8496),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                val t1 = storyTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (t1) Color(0xFFB8143F) else Color.Transparent)
                        .clickable { storyTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "داستان‌های شما",
                        color = if (t1) Color.White else Color(0xFF8B8496),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Plus Crimson Button: Submit confession
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFB8143F))
                    .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { showSubmitDialog = true }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ثبت داستان",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area based on Selected Sub-tab
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
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
            // User Confessions section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.2f), cornerRadiusDp = 10f)
                    .background(Color(0xFF0F0E13))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF130E1C), CircleShape)
                            .border(1.dp, Color(0xFFB8143F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = Color(0xFFB8143F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "اعترافات ارسالی شما",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "داستان‌ها و اعترافاتی که ارسال کرده‌اید، پس از بررسی و تأیید نهایی ناظران، در این لوح تاریخی برای عموم نمایش داده خواهند شد.\nشما نیز می‌توانید راز خود را با بقیه به اشتراک بگذارید.",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF8B8496),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB8143F))
                            .clickable { showSubmitDialog = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ثبت اولین اعتراف مرموز",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
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
            .gothicBorder(borderColor = Color(0xFF2E243D), cornerRadiusDp = 10f)
            .clickable { onRead() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Thumbnail Vector Canvas next to text
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                when (index % 3) {
                    0 -> SpookyBellTowerCanvas(modifier = Modifier.matchParentSize())
                    1 -> SpookyWindowCanvas(modifier = Modifier.matchParentSize())
                    else -> SpookyCorridorCanvas(modifier = Modifier.matchParentSize())
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Narrative Texts
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
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!story.author.isNullOrBlank()) {
                    Text(
                        text = "کاتب: ${story.author}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = story.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 18.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFF8B8496),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "قرائت لوح عتیقه",
                        color = Color(0xFFDEC595),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

// MAP COMPOSABLE (NOW TAB 3)
@Composable
fun ImmersiveMapDashboard(
    onNavigateTimeMirror: () -> Unit,
    onNavigateWrongChoice: () -> Unit,
    onNavigateStories: () -> Unit,
    onLogoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030005))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ELEGANT SERIF HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogoClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "نقشه عمارت سایه‌ها",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFFDEC595), // Vintage gold/cream
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ـ کاوش در بخش‌های پنهان قلعه تاریخی ـ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // THE IMMERSIVE MEDIEVAL MAP FRAME
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f) // Beautiful tall vintage map frame
                    .gothicBorder(borderColor = Color(0xFF2E243D), cornerRadiusDp = 18f)
                    .background(Color(0xFF0A0710))
            ) {
                // Castle graphic filling the upper part
                SpookyCastleCanvas(modifier = Modifier.matchParentSize())

                // Dash connects trail paths
                MapPathCanvas(modifier = Modifier.matchParentSize())

                // Interactive map markings overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Node 1: The Whispering Woods (Top center-left)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "The Whispering Woods",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color(0xFFDEC595),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "ـ جـنـگـل نجـواگـر ـ",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0x99DEC595), fontSize = 10.sp)
                            )
                        }
                    }

                    // Node 2: - THE NORTH - THE ABYSSAL KEEP (Center banner below castle)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ـ THE NORTH ـ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFDEC595),
                                letterSpacing = 1.sp,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "THE ABYSSAL KEEP",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFDEC595),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "دژ اعـمــاق",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFB8143F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Bottom region with more nodes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Mid-Left Node: The Oak of Dread
                        Column {
                            Text(
                                text = "The Oak of Dread",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFDEC595),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Text(
                                text = "بـلـوط هـراس",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8B8496), fontSize = 9.sp)
                            )
                        }

                        // Mid-Right Node: Lamentation River
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Lamentation River",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFDEC595),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Text(
                                text = "رود عـزا",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8B8496), fontSize = 9.sp)
                            )
                        }
                    }

                    // Bottom Node: The Hollow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "The Hollow",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFDEC595),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Text(
                                text = "مـغــاک",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8B8496), fontSize = 9.sp)
                            )
                        }
                    }

                    // BOTTOM ACTION BAR IN THE MAP FRAME
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Solid Red Pill Button: ENTER NORTH
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color(0xFFB8143F))
                                .clickable { onNavigateWrongChoice() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ورود به دژ اعماق",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }

                        // Right Icons: PROFILE and SETTINGS
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // STORIES (Tab 0)
                            Column(
                                modifier = Modifier
                                    .clickable { onNavigateStories() },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = Color(0xFFDEC595),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "کتیبه‌ها",
                                    color = Color(0xFFDEC595),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            }

                            // TIME MIRROR (Tab 1)
                            Column(
                                modifier = Modifier
                                    .clickable { onNavigateTimeMirror() },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFFDEC595),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "آینه زمان",
                                    color = Color(0xFFDEC595),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// SCREEN 2: THE MIRROR OF TIME (آینه زمان با تقویم گوتیک)
@Composable
fun GothicCalendarTimeMirror(timeMirrors: List<TimeMirrorContent>) {
    var selectedDateKey by remember { mutableStateOf("1405-06-27") }
    var currentMonthYear by remember { mutableStateOf("مهر ماه / OCTOBER") }

    val activeNarrative = timeMirrors.find { it.date_key == selectedDateKey }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030005))
    ) {
        // Mirror break background overlay
        MirrorCracksCanvas(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TITLE HEADER
            item {
                Text(
                    text = "آینه زمان",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFFDEC595),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ـ آیـنــه جـنـون‌آمـیــز تـقـویم زمان ـ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8B8496),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )
            }

            // GOTHIC YELLOW PARCHMENT CALENDAR
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 8f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDEC595) // Solid vintage parchment yellow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Selector Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChevronRight, // Arrow right for RTL next month
                                contentDescription = null,
                                tint = Color(0xFF553F1B),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = currentMonthYear,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF3B2A10),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.sp
                                )
                            )
                            Icon(
                                Icons.Default.ChevronLeft, // Arrow left for RTL prev month
                                contentDescription = null,
                                tint = Color(0xFF553F1B),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Weekdays Row (RTL sequence)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه").forEach { d ->
                                Text(
                                    text = d,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF553F1B),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        Divider(color = Color(0xFF553F1B).copy(alpha = 0.2f), thickness = 1.dp)

                        // 31 Days grid mapping
                        val totalDays = 31
                        val startingOffset = 0 // Offset
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
                                            
                                            // Special tags and labels matching the screenshot
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
                                                isBellHighlighted -> Color(0xFF130E1C) // Dark purple-black for highlighted item
                                                isSelected -> Color(0xFFB8143F).copy(alpha = 0.3f)
                                                else -> Color.Transparent
                                            }
                                            
                                            val textCol = when {
                                                isBellHighlighted -> Color(0xFFDEC595) // Golden text inside dark highlighted box
                                                dayNum == 28 -> Color(0xFFB8143F) // Red day 28
                                                else -> Color(0xFF3B2A10)
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(1.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(cellBg)
                                                    .border(
                                                        if (isBellHighlighted) BorderStroke(1.dp, Color(0xFFDEC595))
                                                        else BorderStroke(0.dp, Color.Transparent),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable { selectedDateKey = dateStr },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    color = textCol,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Serif
                                                )
                                                
                                                if (isBellHighlighted) {
                                                    Text(
                                                        text = if (dayNum == 26) "ناقوس" else "عزا",
                                                        fontSize = 5.sp,
                                                        color = Color(0xFFDEC595),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                } else if (labelUnder != null) {
                                                    Text(
                                                        text = labelUnder,
                                                        fontSize = 6.sp,
                                                        color = if (dayNum == 28) Color(0xFFB8143F) else Color(0xFF553F1B).copy(alpha = 0.8f)
                                                    )
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

            // TODAY'S STORY CARD OVERLAY
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gothicBorder(borderColor = Color(0xFF1E1629), cornerRadiusDp = 10f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0713)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Right Box: Spooky Bell Tower Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black)
                            ) {
                                SpookyBellTowerCanvas(modifier = Modifier.fillMaxSize())
                            }

                            // Middle: Story Title and text
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = "روایت امروز",
                                    color = Color(0xFF8B8496),
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = activeNarrative?.title ?: "ناقوس شیون و زاری",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeNarrative?.narrative ?: "در جنگل بلک‌وود، ناقوس معبد گوتیک فقط برای محکومین به صدا در می‌آید...",
                                    color = Color(0xFF8B8496),
                                    fontSize = 10.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Left Box: Parchment Today Date
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDEC595))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "امروز",
                                        color = Color(0xFF3B2A10),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "شنبه، ۲۶ مهر",
                                        color = Color(0xFF3B2A10),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // BOTTOM ACTION BUTTONS INSIDE CARD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Huge Red Read Story Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFB8143F))
                                    .clickable {
                                        // Auto-clicks or guides
                                    }
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "قرائت لوح عتیقه",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Row of Days sequence
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDEC595)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("۲۷", color = Color(0xFF3B2A10), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1C1326)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("۲۸", color = Color(0xFFB8143F), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFB8143F), modifier = Modifier.size(8.dp))
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF130E1C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8B8496), modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// SCREEN 3: SCENARIOS (انتخاب سناریو و بازی تعاملی)
@Composable
fun WrongChoiceSection(scenarios: List<WrongChoiceScenario>) {
    var activeScenario by remember { mutableStateOf<WrongChoiceScenario?>(null) }
    var selectedCategory by remember { mutableStateOf("داغ‌ترین") }

    val categories = listOf("داغ‌ترین", "علوم غریبه", "روانی", "هیولاها")

    if (activeScenario == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030005))
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SCENARIOS HEADER
            Text(
                text = "سناریوهای شوم",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFFDEC595)
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "ـ سرنوشت خود را انتخاب کنید (تولید شده توسط هوش مصنوعی) ـ",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFDEC595).copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // HORIZONTAL CATEGORY ROW (GOTHIC PILLS)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSel = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) Color(0xFFB8143F) else Color(0xFF130E1C))
                            .border(
                                1.dp,
                                if (isSel) Color(0xFFDEC595) else Color(0xFF2E243D),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) Color.White else Color(0xFF8B8496),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2-COLUMN SCENARIO POSTER GRID
            if (scenarios.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFB8143F))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
            .aspectRatio(0.68f) // Spooky portrait poster aspect ratio
            .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.3f), cornerRadiusDp = 10f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Render specific vector illustration based on the poster index
            when (index) {
                1 -> SpookyCorridorCanvas(modifier = Modifier.fillMaxSize())
                2 -> SpookyWindowCanvas(modifier = Modifier.fillMaxSize())
                3 -> SpookyTentaclesCanvas(modifier = Modifier.fillMaxSize())
                else -> SpookySilhouettedPathCanvas(modifier = Modifier.fillMaxSize())
            }

            // Dark vignette overlay at bottom of the cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xAA010003), Color(0xFF010003))
                        )
                    )
            )

            // Index tag top-right
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
                    .background(Color(0xCC010003), CircleShape)
                    .border(0.5.dp, Color(0xFFDEC595), CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = Color(0xFFDEC595),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }

            // Spooky Titles and selective text details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = sc.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sc.description,
                        color = Color(0xFF8B8496),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Outlined Red button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .border(1.dp, Color(0xFFB8143F), RoundedCornerShape(4.dp))
                            .clickable { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "احضار سناریو",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// STORY SUBMISSION DIALOG
@Composable
fun SubmitStoryDialog(viewModel: HorrorViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0E13),
        title = { Text("ارسال داستان یا اعتراف جدید", color = Color(0xFFDEC595), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (submitted) {
                    Text(
                        text = "داستان شما با موفقیت ثبت شد و پس از بررسی منتشر خواهد شد.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان داستان") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2E243D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFDEC595),
                            unfocusedLabelColor = Color(0xFF8B8496)
                        )
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("متن داستان / اعتراف") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB8143F),
                            unfocusedBorderColor = Color(0xFF2E243D),
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
                            unfocusedBorderColor = Color(0xFF2E243D),
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
                    Text("ثبت اعتراف")
                }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)), shape = RoundedCornerShape(8.dp)) {
                    Text("بستن")
                }
            }
        },
        dismissButton = {
            if (!submitted) {
                TextButton(onClick = onDismiss) { Text("انصراف", color = Color(0xFF8B8496)) }
            }
        }
    )
}

// SYSTEM PLAY SCREEN
@Composable
fun InteractiveGamePlay(scenario: WrongChoiceScenario, onBack: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    var endingState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030005))
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
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(
                        borderColor = when (endingState) {
                            "DEAD" -> Color(0xFFB8143F)
                            "SURVIVED" -> Color(0xFF2D936C)
                            else -> Color(0xFFDEC595)
                        },
                        cornerRadiusDp = 10f
                    )
                    .background(Color(0xFF0F0E13))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (endingState != null) {
                        Text(
                            text = when (endingState) {
                                "SURVIVED" -> "شما موفق به بقا شدید"
                                "DEAD" -> "روح شما اسیر سیاهچال شد"
                                else -> "سرنوشت مرموز ابدی"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (endingState == "DEAD") Color(0xFFE63956) else Color(0xFF2D936C),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = when (endingState) {
                                "SURVIVED" -> "با گرفتن تصمیمی هوشمندانه از چنگ مرگ فرار کردید، اما به یاد داشته باشید که سایه‌ها هرگز فراموش نمی‌کنند..."
                                "DEAD" -> "پیشروی شما قطع شد. در دل دیوارهای قلعه حل شدید تا داستانی دیگر برای بقیه قربانی‌ها باشید!"
                                else -> "راه فراری نیافتید اما راز سیاه‌چال را با خود به گور بردید."
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "گذرگاه انتخاب: مرحله $currentStep",
                            color = Color(0xFFDEC595),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = if (currentStep == 1) scenario.description
                            else "صداهای شیون عجیبی از دالان‌های روبه‌رو به گوش می‌رسد و مشعل‌های روی دیوار در حال انجماد و خاموشی هستند. کدام مسیر را انتخاب می‌کنید؟",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                lineHeight = 30.sp
                            )
                        )
                    }
                }
            }
        }

        if (endingState == null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DecisionButton(
                    text = "۱. ورود به تاریکی دالان غربی",
                    onClick = { currentStep++ }
                )
                DecisionButton(
                    text = "۲. پیشروی مستقیم به برج ناقوس",
                    onClick = { currentStep++ }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFB8143F))
                        .clickable { endingState = listOf("SURVIVED", "DEAD", "MYSTERY").random() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("روبه‌رو شدن با سایه مرگبار", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFB8143F))
                    .clickable(onClick = onBack)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("بازگشت به معبد انتخاب‌ها", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DecisionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F0E13))
            .border(0.5.dp, Color(0xFFDEC595).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF8B8496),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp)
        )
    }
}

// SINGLE STORY READER VIEW
@Composable
fun StoryReaderScreen(story: RealStory, onBack: () -> Unit) {
    var fontSizeMultiplier by remember { mutableFloatStateOf(16f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030005))
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
                    text = "لوح عتیقه",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Font Sizer Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0E13), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("تنظیم اندازه قلم:", color = Color(0xFF8B8496), fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("کوچک" to 14f, "متوسط" to 17f, "بزرگ" to 21f).forEach { (label, value) ->
                        val isCurrent = fontSizeMultiplier == value
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCurrent) Color(0xFFB8143F) else Color(0xFF130E1C))
                                .clickable { fontSizeMultiplier = value }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(label, color = if (isCurrent) Color.White else Color(0xFF8B8496), fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 10f)
                    .background(Color(0xFF0F0E13))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            text = "راوی / منبع: ${story.author}",
                            color = Color(0xFF8B8496),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(color = Color(0xFFDEC595).copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(6.dp))
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

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFB8143F))
                .clickable(onClick = onBack)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("بستن لوح عتیقه", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
