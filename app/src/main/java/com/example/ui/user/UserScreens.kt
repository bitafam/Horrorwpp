package com.example.ui.user

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.text.selection.DisableSelection
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.HorrorSoundManager
import com.example.util.NetworkUtils
import com.example.viewmodel.HorrorViewModel
import com.example.viewmodel.AppMode
import com.example.ads.AdiveryAdManager
import com.example.ads.AdiveryBottomBannerAd
import com.example.billing.MyketBillingManager

// ==========================================
// NAVIGATION DESTINATIONS
// ==========================================

enum class UserDestination {
    HOME,
    STORIES,
    GRIM_FORTUNES,
    SUBMIT_STORY,
    AI_STORIES,
    SETTINGS,
    SUBSCRIPTION
}

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

// ==========================================
// GAMING FANTASY CUSTOM CANVASES & ASYMMETRICAL BANNERS
// ==========================================

@Composable
fun GamingStoryBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "storyPulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )
    val flameY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameY"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark fantasy midnight obsidian to crimson gradient
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF040108), Color(0xFF190615), Color(0xFF07020B))
        )
        drawRect(bgGrad)

        // Mystical pulsating blood-red moon
        drawCircle(
            color = Color(0xFFFF1E56).copy(alpha = auraAlpha * 0.25f),
            radius = w * 0.35f,
            center = Offset(w * 0.8f, h * 0.35f)
        )
        drawCircle(
            color = Color(0xFF9E1B32),
            radius = w * 0.14f,
            center = Offset(w * 0.8f, h * 0.35f)
        )
        // Golden lunar rim
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = 0.6f),
            radius = w * 0.142f,
            center = Offset(w * 0.8f, h * 0.35f),
            style = Stroke(width = 2f)
        )

        // Spooky gothic fortress silhouette with high spires
        val fortPath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.65f)
            lineTo(w * 0.12f, h * 0.55f)
            lineTo(w * 0.18f, h * 0.35f) // Tower 1
            lineTo(w * 0.22f, h * 0.55f)
            lineTo(w * 0.35f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.22f) // Main Cathedral Spire
            lineTo(w * 0.46f, h * 0.52f)
            lineTo(w * 0.65f, h * 0.58f)
            lineTo(w * 0.72f, h * 0.42f)
            lineTo(w * 0.78f, h * 0.65f)
            lineTo(w, h * 0.65f)
            lineTo(w, h)
            close()
        }
        drawPath(fortPath, Color(0xFF030106))

        // Glowing stained glass gothic windows
        drawRoundRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(w * 0.41f, h * 0.38f + flameY),
            size = Size(w * 0.025f, h * 0.08f),
            cornerRadius = CornerRadius(w * 0.012f, w * 0.012f)
        )
        drawRoundRect(
            color = Color(0xFFFF1E56),
            topLeft = Offset(w * 0.17f, h * 0.45f),
            size = Size(w * 0.02f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
        )

        // Floating grimoire runes & particle dust
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.8f), 3f, Offset(w * 0.3f, h * 0.3f + flameY))
        drawCircle(Color(0xFFFF1E56).copy(alpha = 0.7f), 4f, Offset(w * 0.55f, h * 0.25f - flameY))
        drawCircle(Color(0xFFDEC595).copy(alpha = 0.9f), 2.5f, Offset(w * 0.68f, h * 0.45f + flameY))
        drawCircle(Color(0xFF8A2BE2).copy(alpha = 0.8f), 3.5f, Offset(w * 0.2f, h * 0.28f))
    }
}

@Composable
fun GamingScenarioBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scenarioPulse")
    val skullEyeGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyeGlow"
    )
    val mistShift by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mistShift"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark demonic red & abyssal black gradient
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF030104), Color(0xFF1D050D), Color(0xFF070104))
        )
        drawRect(bgGrad)

        // Perspective labyrinth lines to center abyss gate
        val gateStroke = 2.5f
        val lineCol = Color(0xFF4A0A17)
        drawLine(lineCol, Offset(0f, 0f), Offset(w * 0.45f, h * 0.45f), gateStroke)
        drawLine(lineCol, Offset(w, 0f), Offset(w * 0.55f, h * 0.45f), gateStroke)
        drawLine(lineCol, Offset(0f, h), Offset(w * 0.42f, h * 0.6f), gateStroke)
        drawLine(lineCol, Offset(w, h), Offset(w * 0.58f, h * 0.6f), gateStroke)

        // Portal of death arch
        drawRoundRect(
            color = Color(0xFF0A0205),
            topLeft = Offset(w * 0.38f, h * 0.25f),
            size = Size(w * 0.24f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )
        drawRoundRect(
            color = Color(0xFFB8143F).copy(alpha = skullEyeGlow * 0.5f),
            topLeft = Offset(w * 0.38f, h * 0.25f),
            size = Size(w * 0.24f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = 3f)
        )

        // Sinister skull eyes inside the portal
        drawCircle(Color(0xFFFF1A4D).copy(alpha = skullEyeGlow), w * 0.025f, Offset(w * 0.46f, h * 0.42f))
        drawCircle(Color(0xFFFF1A4D).copy(alpha = skullEyeGlow), w * 0.025f, Offset(w * 0.54f, h * 0.42f))
        drawCircle(Color(0xFFFFFFFF).copy(alpha = skullEyeGlow), w * 0.008f, Offset(w * 0.46f, h * 0.42f))
        drawCircle(Color(0xFFFFFFFF).copy(alpha = skullEyeGlow), w * 0.008f, Offset(w * 0.54f, h * 0.42f))

        // Toxic spectral green & crimson rune fire sparks
        drawCircle(Color(0xFF39FF14).copy(alpha = 0.8f), 3.5f, Offset(w * 0.2f + mistShift, h * 0.65f))
        drawCircle(Color(0xFF39FF14).copy(alpha = 0.6f), 2.5f, Offset(w * 0.8f - mistShift, h * 0.7f))
        drawCircle(Color(0xFFFF1A4D).copy(alpha = 0.9f), 4f, Offset(w * 0.5f, h * 0.2f + mistShift * 0.5f))
    }
}

@Composable
fun GamingGrimFortuneBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fortunePulse")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing)
        ),
        label = "zodiacRotate"
    )
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zodiacGlow"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deep gothic obsidian and blood crimson gradient
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF040108), Color(0xFF180510), Color(0xFF07010A))
        )
        drawRect(bgGrad)

        val center = Offset(w * 0.75f, h * 0.5f)

        // Mystical Crimson Aura glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF1E56).copy(alpha = glow * 0.4f), Color.Transparent),
                center = center,
                radius = w * 0.45f
            ),
            radius = w * 0.45f,
            center = center
        )

        // Rotating golden zodiac celestial rings
        drawCircle(
            color = Color(0xFFDEC595).copy(alpha = 0.65f),
            radius = w * 0.26f,
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = 0.45f),
            radius = w * 0.18f,
            center = center,
            style = Stroke(width = 1.5f)
        )

        // 8 Cosmic Star Points / Sun rays
        for (i in 0 until 8) {
            val rad = Math.toRadians((angle + i * 45).toDouble())
            val r1 = (w * 0.18f).toDouble()
            val r2 = (w * 0.26f).toDouble()
            val x1 = (center.x + r1 * Math.cos(rad)).toFloat()
            val y1 = (center.y + r1 * Math.sin(rad)).toFloat()
            val x2 = (center.x + r2 * Math.cos(rad)).toFloat()
            val y2 = (center.y + r2 * Math.sin(rad)).toFloat()
            drawLine(
                color = Color(0xFFFFD700).copy(alpha = 0.7f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f
            )
        }

        // Golden Eye of Providence at center
        drawCircle(Color(0xFFFFD700), w * 0.04f, center)
        drawCircle(Color(0xFF14050D), w * 0.02f, center)

        // Left side astrological constellations in warm gold and crimson
        drawCircle(Color(0xFFDEC595), 3.5f, Offset(w * 0.15f, h * 0.25f))
        drawCircle(Color(0xFFFF1E56).copy(alpha = 0.8f), 2.5f, Offset(w * 0.25f, h * 0.35f))
        drawCircle(Color(0xFFDEC595), 4f, Offset(w * 0.2f, h * 0.65f))
        drawCircle(Color(0xFFFFD700), 3f, Offset(w * 0.35f, h * 0.75f))
        drawLine(Color(0x55DEC595), Offset(w * 0.15f, h * 0.25f), Offset(w * 0.25f, h * 0.35f), 1.5f)
        drawLine(Color(0x55DEC595), Offset(w * 0.2f, h * 0.65f), Offset(w * 0.35f, h * 0.75f), 1.5f)
    }
}

@Composable
fun GamingSubmitStoryBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scribePulse")
    val candleFlame by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "candleFlame"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Vintage antique parchment and dark copper background
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF060308), Color(0xFF1C0D08), Color(0xFF090403))
        )
        drawRect(bgGrad)

        // Cursed ancient scroll parchment outline on right
        val scrollPath = Path().apply {
            moveTo(w * 0.6f, h * 0.15f)
            lineTo(w * 0.92f, h * 0.15f)
            lineTo(w * 0.9f, h * 0.85f)
            lineTo(w * 0.58f, h * 0.85f)
            close()
        }
        drawPath(scrollPath, Color(0xFF261309))
        drawPath(scrollPath, Color(0xFFDEC595).copy(alpha = 0.5f), style = Stroke(width = 2f))

        // Inscription lines on scroll
        for (i in 0 until 5) {
            val y = h * (0.28f + i * 0.11f)
            drawLine(
                color = Color(0xFF8C0E26).copy(alpha = 0.6f),
                start = Offset(w * 0.63f, y),
                end = Offset(w * 0.85f, y),
                strokeWidth = 2.5f
            )
        }

        // Blood Quill Pen tilted
        val quillPath = Path().apply {
            moveTo(w * 0.52f, h * 0.8f) // Quill tip
            lineTo(w * 0.35f, h * 0.2f) // Feather top
            lineTo(w * 0.42f, h * 0.35f)
            lineTo(w * 0.48f, h * 0.55f)
            close()
        }
        drawPath(quillPath, Color(0xFFB8143F))
        drawPath(quillPath, Color(0xFFFFD700).copy(alpha = 0.7f), style = Stroke(width = 1.5f))

        // Dripping blood drop from quill
        drawCircle(Color(0xFFFF1E56), 4.5f, Offset(w * 0.52f, h * 0.83f))

        // Candle on left with flickering flame
        drawRect(Color(0xFFDEC595), topLeft = Offset(w * 0.18f, h * 0.55f), size = Size(w * 0.04f, h * 0.3f))
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = 0.3f),
            radius = (w * 0.08f) * candleFlame,
            center = Offset(w * 0.2f, h * 0.48f)
        )
        drawCircle(
            color = Color(0xFFFF7700),
            radius = (w * 0.03f) * candleFlame,
            center = Offset(w * 0.2f, h * 0.48f)
        )
    }
}

@Composable
fun GamingAudioSettingsBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioPulse")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave3"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deep gothic obsidian and blood crimson gradient
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF040108), Color(0xFF190612), Color(0xFF06020A))
        )
        drawRect(bgGrad)

        // Gothic Gearwork rings on left in antique gold and crimson
        drawCircle(
            color = Color(0xFFDEC595).copy(alpha = 0.45f),
            radius = w * 0.22f,
            center = Offset(w * 0.2f, h * 0.5f),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color(0xFFB8143F).copy(alpha = 0.6f),
            radius = w * 0.12f,
            center = Offset(w * 0.2f, h * 0.5f),
            style = Stroke(width = 1.5f)
        )

        // Equalizer frequency soundwave bars on right in blood crimson and gothic gold
        val heights = listOf(wave1, wave2, wave3, wave1 * 0.8f, wave2 * 1.1f, wave3 * 0.7f, wave1 * 1.2f, wave2 * 0.6f)
        val barWidth = w * 0.035f
        val startX = w * 0.5f
        val spacing = w * 0.055f

        heights.forEachIndexed { i, factor ->
            val barH = (h * 0.65f) * factor.coerceIn(0.1f, 1.0f)
            val x = startX + i * spacing
            val topY = (h - barH) / 2f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF1E56), Color(0xFFB8143F), Color(0xFFDEC595))
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun GamingAiSummonerBannerCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "aiSummonPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aiPulse"
    )
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ),
        label = "spinAngle"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark astral neon vortex
        val bgGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFF040108), Color(0xFF1E031E), Color(0xFF06010C))
        )
        drawRect(bgGrad)

        val center = Offset(w * 0.8f, h * 0.5f)

        // Astral portal rings
        drawCircle(
            color = Color(0xFFFF1E56).copy(alpha = 0.35f),
            radius = (w * 0.28f) * pulse,
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color(0xFF8A2BE2).copy(alpha = 0.5f),
            radius = w * 0.18f,
            center = center,
            style = Stroke(width = 2f)
        )

        // Rotating Summoning Pentagram
        val pentagonRadius = (w * 0.18f).toDouble()
        val points = mutableListOf<Offset>()
        for (i in 0 until 5) {
            val a = Math.toRadians((spinAngle + i * 72 - 90).toDouble())
            points.add(Offset((center.x + pentagonRadius * Math.cos(a)).toFloat(), (center.y + pentagonRadius * Math.sin(a)).toFloat()))
        }
        for (i in 0 until 5) {
            val p1 = points[i]
            val p2 = points[(i + 2) % 5]
            drawLine(Color(0xFFFFD700).copy(alpha = 0.8f), p1, p2, 2f)
        }

        // Glowing core crystal
        drawCircle(Color(0xFFFFFFFF), w * 0.035f, center)
        drawCircle(Color(0xFFFF1E56).copy(alpha = 0.7f), w * 0.06f, center)

        // Ambient lightning spark lines on left
        drawLine(Color(0x888A2BE2), Offset(w * 0.1f, h * 0.3f), Offset(w * 0.25f, h * 0.45f), 2f)
        drawLine(Color(0x8800E5FF), Offset(w * 0.25f, h * 0.45f), Offset(w * 0.4f, h * 0.35f), 2f)
        drawLine(Color(0x88FF1E56), Offset(w * 0.4f, h * 0.35f), Offset(w * 0.55f, h * 0.6f), 2f)
    }
}

// ==========================================
// GAMING FANTASY TOP BAR COMPONENT
// ==========================================

@Composable
fun GamingTopBar(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.AutoAwesome,
    badgeText: String? = null,
    onBack: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Surface(
        color = Color(0xFF07040C),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFFB8143F).copy(alpha = 0.6f),
                        Color(0xFFDEC595).copy(alpha = 0.8f),
                        Color(0xFFB8143F).copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                shape = RectangleShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Section (RTL): Back button + Title & Subtitle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onBack()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF160B24),
                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.6f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت به خانه",
                            tint = Color(0xFFDEC595),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            color = Color(0xFFDEC595),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        )
                        if (badgeText != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFB8143F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Color(0xFF8B8496),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Left Section (RTL): Trailing Actions or Home button
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Surface(
                        onClick = {
                            HorrorSoundManager.playScenarioChoiceSound()
                            onBack()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF12081E),
                        border = BorderStroke(1.dp, Color(0xFF2B1C3D)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "خانه",
                                tint = Color(0xFF8B8496),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// BRAND NEW GOTHIC GAMING HOME HUB (صفحه اصلی)
// ==========================================

@Composable
fun GothicGamingHomeScreen(
    viewModel: HorrorViewModel,
    onNavigate: (UserDestination) -> Unit,
    onLogoClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "homePulse")
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040207))
    ) {
        // High-end ambient background cracks canvas
        MirrorCracksCanvas(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TOP GAMING HUD STATUS BAR
            Surface(
                color = Color(0xFF0C0716),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HorrorSoundManager.playClickSound()
                            onLogoClick()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "عـمـــارت وحـشـــت",
                            color = Color(0xFFDEC595),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "HORROR REALM • HUB",
                            color = Color(0xFF8B8496),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // ==========================================
            // ASYMMETRICAL BENTO GRID OF GAME PORTAL CARDS
            // ==========================================

            // CARD 1: LARGE HERO SPAN - STORIES ARCHIVE (روایات واقعی و اسرار ماوراء)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .gothicBorder(borderColor = Color(0xFFB8143F).copy(alpha = 0.7f), cornerRadiusDp = 14f)
                    .clickable {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onNavigate(UserDestination.STORIES)
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E040A))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_poster_1_1788266550537),
                        contentDescription = "پوستر کتابخانه روایات",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient vignette
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x33000000), Color(0x990E040A), Color(0xFA0E040A))
                                )
                            )
                    )

                    // Content overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFB8143F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("آرشیو کهن روایات", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                color = Color(0xFF380714),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.7f))
                            ) {
                                Text("داغ‌ترین بخش", color = Color(0xFFDEC595), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }

                        Column {
                            Text(
                                text = "کتابخانه روایات تسخیرشده",
                                color = Color(0xFFDEC595),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "مجموعه مستند وقایع ماوراءالطبیعه، ارواح، طلسم‌ها و اعترافات کاربران",
                                color = Color(0xFFEDE8F5),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    color = Color(0xFF380714),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.7f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("گشایش کتیبه‌ها", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ASYMMETRICAL DUAL ROW 1: AI STORIES (TALL POSTER) + GRIM FORTUNES (MEDIUM POSTER)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CARD 2: AI STORIES (داستان‌های هوش تاریکی)
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(230.dp)
                        .gothicBorder(borderColor = Color(0xFFB8143F).copy(alpha = 0.7f), cornerRadiusDp = 14f)
                    .clickable {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onNavigate(UserDestination.AI_STORIES)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E040A))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ai_story_poster_1_1788531305066),
                            contentDescription = "پوستر داستان‌های هوش تاریکی",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x22000000), Color(0x880E040A), Color(0xF50E040A))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color(0xFFB8143F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("هوش تاریکی", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }

                            Column {
                                Text(
                                    text = "داستان‌های هوش تاریکی",
                                    color = Color(0xFFDEC595),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "قصه‌های ماورایی و دلهره‌آور",
                                    color = Color(0xFFEDE8F5),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF380714),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "مطالعه قصه‌ها ❯",
                                        color = Color(0xFFDEC595),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD 3: GRIM FORTUNES (طالع شوم ۱۲ ماه)
                Card(
                    modifier = Modifier
                        .weight(1.0f)
                        .height(230.dp)
                        .gothicBorder(borderColor = Color(0xFFB8143F).copy(alpha = 0.7f), cornerRadiusDp = 14f)
                    .clickable {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onNavigate(UserDestination.GRIM_FORTUNES)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E040A))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_poster_3_1788266577786),
                            contentDescription = "پوستر طالع شوم",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x22000000), Color(0x880E040A), Color(0xF50E040A))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color(0xFFB8143F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("طالع شوم", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }

                            Column {
                                Text(
                                    text = "طالع شوم ماهانه",
                                    color = Color(0xFFDEC595),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "۱۲ ماه در فلک تاریک",
                                    color = Color(0xFFEDE8F5),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF380714),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "مشاهده طالع ❯",
                                        color = Color(0xFFDEC595),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ASYMMETRICAL DUAL ROW 2: SUBMIT STORY (کتیبه نگارش) + SOUND & SETTINGS (تنظیمات)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CARD 4: SUBMIT STORY (نگارش و ارسال رازها)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .gothicBorder(borderColor = Color(0xFFB8143F).copy(alpha = 0.7f), cornerRadiusDp = 14f)
                    .clickable {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onNavigate(UserDestination.SUBMIT_STORY)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E040A))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nightmare_crypt_banner_1789721206204),
                            contentDescription = "پوستر ارسال روایت",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x22000000), Color(0x990E040A), Color(0xF50E040A))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color(0xFFB8143F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("ارسال روایت", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }

                            Column {
                                Text(
                                    text = "ارسال روایت و راز",
                                    color = Color(0xFFDEC595),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "حکاکی تجربه در کتیبه‌ها",
                                    color = Color(0xFFEDE8F5),
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF380714),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "نگارش راز ❯",
                                        color = Color(0xFFDEC595),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD 5: SETTINGS (تنظیمات)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .gothicBorder(borderColor = Color(0xFFB8143F).copy(alpha = 0.7f), cornerRadiusDp = 14f)
                    .clickable {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onNavigate(UserDestination.SETTINGS)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E040A))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_poster_2_1788266563762),
                            contentDescription = "پوستر تنظیمات",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x22000000), Color(0x990E040A), Color(0xF50E040A))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color(0xFFB8143F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("تنظیمات", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }

                            Column {
                                Text(
                                    text = "تنظیمات",
                                    color = Color(0xFFDEC595),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "تنظیمات کلی برنامه",
                                    color = Color(0xFFEDE8F5),
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF380714),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "پیکربندی ❯",
                                        color = Color(0xFFDEC595),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CARD 6: VIP PERMANENT MEMBERSHIP & NO ADS BANNER
            val isPremiumUser by viewModel.isPremiumUser.collectAsState()
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(
                        borderColor = if (isPremiumUser) Color(0xFF2ECC71).copy(alpha = 0.8f) else Color(0xFFDEC595).copy(alpha = 0.8f),
                        cornerRadiusDp = 14f
                    )
                    .clickable {
                        HorrorSoundManager.playScenarioChoiceSound()
                        onNavigate(UserDestination.SUBSCRIPTION)
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremiumUser) Color(0xFF071B0E) else Color(0xFF19091F)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPremiumUser) Color(0xFF12381F) else Color(0xFF380724)
                            )
                            .border(
                                1.5.dp,
                                if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPremiumUser) "عضویت طلایی و دائمی عمارت" else "نسخه دائمی و بدون تبلیغات",
                                color = if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isPremiumUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFB8143F),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "VIP",
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isPremiumUser) "تمامی امکانات باز و تبلیغات حذف شده‌اند 👑" else "حذف ۱۰۰٪ تبلیغات، دسترسی به ارسال داستان و امکانات ویژه",
                            color = Color(0xFFEDE8F5).copy(alpha = 0.8f),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            val context = LocalContext.current
            val myketPackage = "ir.mservices.market"
            val appPackageName = "com.apps.hororhouse"

            // ROW: RATE ON MYKET + SHARE APP WITH MYKET LINK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // BUTTON 1: RATE ON MYKET
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .gothicBorder(borderColor = Color(0xFFFFD700).copy(alpha = 0.5f), cornerRadiusDp = 12f)
                        .clickable {
                            HorrorSoundManager.playClickSound()
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("myket://comment?id=$appPackageName")
                                    setPackage(myketPackage)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$appPackageName"))
                                    context.startActivity(fallback)
                                } catch (ex: Exception) {
                                    android.widget.Toast.makeText(context, "خطا در باز کردن مایکت", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140F1E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E2210))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "امتیاز",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "امتیاز به برنامه",
                                color = Color(0xFFFFD700),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ثبت دیدگاه در مایکت",
                                color = Color(0xFFC5B6D4),
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // BUTTON 2: SHARE APP WITH MYKET LINK
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.5f), cornerRadiusDp = 12f)
                        .clickable {
                            HorrorSoundManager.playClickSound()
                            val shareAppText = """
🏰 اپلیکیشن «عمارت وحشت» | دنیای اسرارآمیز روایات واقعی و وحشت ماوراءالطبیعه

🔮 قابلیت‌های هیجان‌انگیز:
• خواندن صدها داستان ترسناک، رازهای کهن و طلسم‌ها
• احضار کاتب ارواح (داستان‌نویس پیشرفته هوش مصنوعی)
• طالع شوم و سرنوشت تاریک متولدین هر ماه
• ثبت و انتشار داستان‌های ترسناک شخصی شما

🩸 همین حالا رایگان از مایکت دریافت کنید:
https://myket.ir/app/com.apps.hororhouse
                            """.trimIndent()
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareAppText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "معرفی عمارت وحشت به دوستان"))
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140F1E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF271338))
                                .border(1.dp, Color(0xFFDEC595).copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "اشتراک‌گذاری",
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "معرفی به دوستان",
                                color = Color(0xFFDEC595),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اشتراک لینک مایکت",
                                color = Color(0xFFC5B6D4),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // SINGLE ROW: CHECK FOR UPDATE IN MYKET
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFF5C338A).copy(alpha = 0.8f), cornerRadiusDp = 12f)
                    .clickable {
                        HorrorSoundManager.playClickSound()
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("myket://details?id=$appPackageName")
                                setPackage(myketPackage)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$appPackageName"))
                                context.startActivity(fallback)
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "خطا در اتصال به مایکت", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0818))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF28123C))
                            .border(1.dp, Color(0xFF9B51E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "بروزرسانی",
                            tint = Color(0xFFC58BF2),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "بررسی بروزرسانی در مایکت",
                                color = Color(0xFFEDE4F5),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF381552),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFF9B51E0).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "مایکت",
                                    color = Color(0xFFC58BF2),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "بررسی انتشار نسخه جدید و تغییرات تاریک عمارت وحشت",
                            color = Color(0xFF8B8496),
                            fontSize = 9.5.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFC58BF2),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FOOTER SIGNATURE
            Text(
                text = "ـ عمارت وحشت | بقا متعلق به کسی است که مسیر تاریکی را بشناسد ـ",
                color = Color(0xFF8B8496).copy(alpha = 0.6f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

// ==========================================
// USER MAIN SCREEN ROUTER (REFACTORED WITH ZERO BOTTOM NAVIGATION)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(
    viewModel: HorrorViewModel,
    billingManager: MyketBillingManager
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val destinationStack = remember { mutableStateListOf(UserDestination.HOME) }
    val currentDestination = destinationStack.lastOrNull() ?: UserDestination.HOME
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showPremiumRequiredDialog by remember { mutableStateOf(false) }

    val isPremium by viewModel.isPremiumUser.collectAsState()
    val isOnline by viewModel.isNetworkOnline.collectAsState()
    val grimFortunes by viewModel.grimFortunesList.collectAsState()
    val realStories by viewModel.realStoriesList.collectAsState()
    val aiStories by viewModel.aiStoriesList.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var activeReadingStory by remember { mutableStateOf<RealStory?>(null) }
    var activeReadingAiStory by remember { mutableStateOf<AiStory?>(null) }

    fun navigateTo(dest: UserDestination) {
        if (dest == UserDestination.SUBMIT_STORY && !isPremium) {
            showPremiumRequiredDialog = true
            return
        }
        if (dest == UserDestination.GRIM_FORTUNES && !isPremium && activity != null) {
            AdiveryAdManager.showAppOpenAdIfAvailable(
                activity = activity,
                isPremium = false,
                onComplete = {
                    if (destinationStack.lastOrNull() != dest) {
                        destinationStack.add(dest)
                    }
                }
            )
            return
        }
        if (dest == UserDestination.HOME) {
            destinationStack.clear()
            destinationStack.add(UserDestination.HOME)
        } else {
            if (destinationStack.lastOrNull() != dest) {
                destinationStack.add(dest)
            }
        }
    }

    fun popBack() {
        if (activeReadingStory != null) {
            activeReadingStory = null
        } else if (activeReadingAiStory != null) {
            activeReadingAiStory = null
        } else if (destinationStack.size > 1) {
            destinationStack.removeAt(destinationStack.lastIndex)
        } else {
            showExitDialog = true
        }
    }

    // Intercept system back button to smoothly navigate back step-by-step or show exit dialog
    BackHandler(enabled = true) {
        popBack()
    }

    fun handleReadRegularStory(story: RealStory) {
        if (!NetworkUtils.isOnline(context)) {
            showNoInternetDialog = true
            return
        }
        if (isPremium) {
            activeReadingStory = story
            return
        }
        val shouldShowAd = viewModel.onReadRegularOrAiStory()
        if (shouldShowAd && activity != null) {
            AdiveryAdManager.showAppOpenAdIfAvailable(
                activity = activity,
                isPremium = false,
                onComplete = {
                    activeReadingStory = story
                }
            )
        } else {
            activeReadingStory = story
        }
    }

    fun handleReadUserStory(story: RealStory) {
        if (!NetworkUtils.isOnline(context)) {
            showNoInternetDialog = true
            return
        }
        if (isPremium) {
            activeReadingStory = story
            return
        }
        if (activity != null) {
            AdiveryAdManager.showAppOpenAdIfAvailable(
                activity = activity,
                isPremium = false,
                onComplete = {
                    activeReadingStory = story
                }
            )
        } else {
            activeReadingStory = story
        }
    }

    fun handleReadAiStory(story: AiStory) {
        if (isPremium) {
            activeReadingAiStory = story
            return
        }
        val shouldShowAd = viewModel.onReadRegularOrAiStory()
        if (shouldShowAd && activity != null) {
            AdiveryAdManager.showAppOpenAdIfAvailable(
                activity = activity,
                isPremium = false,
                onComplete = {
                    activeReadingAiStory = story
                }
            )
        } else {
            activeReadingAiStory = story
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF040207))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                    } else if (activeReadingAiStory != null) {
                        AiStoryReaderScreen(
                            story = activeReadingAiStory!!,
                            viewModel = viewModel,
                            onBack = { activeReadingAiStory = null }
                        )
                    } else {
                        when (currentDestination) {
                            UserDestination.HOME -> {
                                GothicGamingHomeScreen(
                                    viewModel = viewModel,
                                    onNavigate = { dest -> navigateTo(dest) }
                                )
                            }
                            UserDestination.STORIES -> {
                                BeautifulStoriesDashboard(
                                    realStories = realStories,
                                    viewModel = viewModel,
                                    onLogoClick = {},
                                    onStoryRead = { selected -> handleReadRegularStory(selected) },
                                    onUserStoryRead = { selected -> handleReadUserStory(selected) },
                                    onOpenSubscription = { navigateTo(UserDestination.SUBSCRIPTION) },
                                    onBack = { popBack() }
                                )
                            }
                            UserDestination.GRIM_FORTUNES -> {
                                GrimFortuneScreen(
                                    grimFortunes = grimFortunes,
                                    viewModel = viewModel,
                                    onBack = { popBack() }
                                )
                            }
                            UserDestination.SUBMIT_STORY -> {
                                BeautifulSubmitStoryScreen(
                                    viewModel = viewModel,
                                    onBack = { popBack() },
                                    onSubmissionComplete = { navigateTo(UserDestination.STORIES) }
                                )
                            }
                            UserDestination.AI_STORIES -> {
                                AiStoriesUserSection(
                                    aiStories = aiStories,
                                    viewModel = viewModel,
                                    onStoryRead = { selected -> handleReadAiStory(selected) },
                                    onBack = { popBack() }
                                )
                            }
                            UserDestination.SETTINGS -> {
                                GorgeousSettingsScreen(
                                    viewModel = viewModel,
                                    onNavigate = { dest -> navigateTo(dest) },
                                    onBack = { popBack() }
                                )
                            }
                            UserDestination.SUBSCRIPTION -> {
                                SubscriptionScreen(
                                    viewModel = viewModel,
                                    billingManager = billingManager,
                                    onBack = { popBack() }
                                )
                            }
                        }
                    }
                }

                // Offline floating warning banner if network is disconnected
                if (!isOnline) {
                    Surface(
                        color = Color(0xFF4A0A17),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("عدم اتصال به اینترنت", color = Color(0xFFEDE8F5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { viewModel.loadUserData() }) {
                                Text("اتصال مجدد 🔄", color = Color(0xFFFFD700), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Bottom Banner Ad (Visible only on free version across all screens, centered at bottom)
            AdiveryBottomBannerAd(isPremium = isPremium)
        }
    }

    // Premium Story Submission Required Dialog
    if (showPremiumRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumRequiredDialog = false },
            containerColor = Color(0xFF14081E),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF280B18))
                        .border(1.5.dp, Color(0xFFDEC595), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "ارسال داستان مختص اعضای ویژه 👑",
                    color = Color(0xFFDEC595),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "در نسخه رایگان، امکان ارسال داستان وجود ندارد. با خرید اشتراک دائمی، قابلیت ارسال روایات با نام اختصاصی شما باز شده و تمامی تبلیغات برنامه نیز برای همیشه محو خواهند شد.",
                    color = Color(0xFFEDE8F5).copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumRequiredDialog = false
                        navigateTo(UserDestination.SUBSCRIPTION)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("خرید اشتراک دائمی", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPremiumRequiredDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("فعلاً نه", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color(0xFF14081E),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF380714))
                        .border(1.5.dp, Color(0xFFB8143F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFFF1E56),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "میخوای بری؟! ",
                    color = Color(0xFFDEC595),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "آیا مطمئنی که می‌خواهی از عمارت وحشت خارج شوی؟",
                    color = Color(0xFFEDE8F5).copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("آره", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("نه", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        )
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
}

// ==========================================
// TAB 1: STORIES DASHBOARD (REAL & USER STORIES)
// ==========================================

@Composable
fun BeautifulStoriesDashboard(
    realStories: List<RealStory>,
    viewModel: HorrorViewModel,
    onLogoClick: () -> Unit,
    onStoryRead: (RealStory) -> Unit,
    onUserStoryRead: ((RealStory) -> Unit)? = null,
    onOpenSubscription: (() -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    val isPremium by viewModel.isPremiumUser.collectAsState()
    var storyTab by remember { mutableIntStateOf(0) } // 0 = داستان‌های واقعی, 1 = داستان‌های شما
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showPremiumPromptDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = جدیدترین‌ها, 1 = داغ‌ترین‌ها, 2 = محبوب‌ترین‌ها ♥️
    
    // User Stories tab state
    var userSearchQuery by remember { mutableStateOf("") }
    var userFilterIndex by remember { mutableIntStateOf(0) } // 0 = جدیدترین‌ها, 1 = داغ‌ترین‌ها, 2 = محبوب‌ترین‌ها ♥️
    
    var realStoriesLimit by remember { mutableIntStateOf(20) }
    var userStoriesLimit by remember { mutableIntStateOf(20) }

    LaunchedEffect(searchQuery, selectedFilterIndex) {
        realStoriesLimit = 20
    }

    LaunchedEffect(userSearchQuery, userFilterIndex) {
        userStoriesLimit = 20
    }

    var isAmbientPlaying by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
    ) {
        GamingTopBar(
            title = "کتابخانه روایات تسخیرشده",
            subtitle = "آرشیو داستان‌های واقعی و اعترافات",
            icon = Icons.Default.MenuBook,
            badgeText = null,
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
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
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 14f)
                    .clickable { onLogoClick() }
            ) {
                // Cinematic Universal Horror Banner (Haunted Gothic Manor under Blood Moon)
                Image(
                    painter = painterResource(id = R.drawable.img_universal_horror_banner_1789721194695),
                    contentDescription = "بنر عمارت وحشت",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Rich Vignette gradient overlay for depth & readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x33030106),
                                    Color(0x66000000),
                                    Color(0xEE05020A),
                                    Color(0xFF030106)
                                )
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
                    val displayedStories = remember(filteredStories, realStoriesLimit) {
                        filteredStories.take(realStoriesLimit)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        displayedStories.forEachIndexed { index, story ->
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

                        if (filteredStories.size > realStoriesLimit) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    HorrorSoundManager.playClickSound()
                                    realStoriesLimit += 20
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F112E)),
                                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مشاهده داستان‌های بیشتر (نمایش ${displayedStories.size} از ${filteredStories.size})",
                                        color = Color(0xFFDEC595),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
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
                        val displayedUserSubmissions = remember(filteredUserSubmissions, userStoriesLimit) {
                            filteredUserSubmissions.take(userStoriesLimit)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            displayedUserSubmissions.forEachIndexed { idx, sub ->
                                UserStoryItemCard(
                                    submission = sub,
                                    index = idx,
                                    onRead = {
                                        HorrorSoundManager.playPageTurnSound()
                                        viewModel.incrementSubmissionViews(sub.id)
                                        if (onUserStoryRead != null) {
                                            onUserStoryRead(sub.toRealStory())
                                        } else {
                                            onStoryRead(sub.toRealStory())
                                        }
                                    }
                                )
                            }

                            if (filteredUserSubmissions.size > userStoriesLimit) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        HorrorSoundManager.playClickSound()
                                        userStoriesLimit += 20
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F112E)),
                                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "مشاهده داستان‌های بیشتر (نمایش ${displayedUserSubmissions.size} از ${filteredUserSubmissions.size})",
                                            color = Color(0xFFDEC595),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
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
                    if (isPremium) {
                        showSubmitDialog = true
                    } else {
                        showPremiumPromptDialog = true
                    }
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
}

    if (showSubmitDialog) {
        SubmitStoryDialog(viewModel) {
            showSubmitDialog = false
        }
    }

    if (showPremiumPromptDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumPromptDialog = false },
            containerColor = Color(0xFF14081E),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF280B18))
                        .border(1.5.dp, Color(0xFFDEC595), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFDEC595),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "ارسال داستان فقط برای اعضای طلایی 👑",
                    color = Color(0xFFDEC595),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "ارسال داستان و روایات کاربران تنها برای کاربرانی که اشتراک دائمی تهیه کرده‌اند فعال است. با تهیه اشتراک تمامی تبلیغات برنامه نیز برای شما حذف خواهد شد.",
                    color = Color(0xFFEDE8F5).copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumPromptDialog = false
                        onOpenSubscription?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("خرید اشتراک دائمی", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPremiumPromptDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("فعلاً نه", color = Color(0xFFDEC595), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
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
            val defaultRes = remember(index) {
                when (index % 3) {
                    0 -> R.drawable.img_poster_1_1788266550537
                    1 -> R.drawable.img_poster_2_1788266563762
                    else -> R.drawable.img_poster_3_1788266577786
                }
            }
            AsyncImage(
                model = story.cover_url,
                placeholder = painterResource(id = defaultRes),
                error = painterResource(id = defaultRes),
                contentDescription = story.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

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
fun GrimFortuneScreen(
    grimFortunes: List<GrimFortune>,
    viewModel: HorrorViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isPremium by viewModel.isPremiumUser.collectAsState()

    val handleBack = {
        if (activity != null && !isPremium) {
            AdiveryAdManager.showAppOpenAdIfAvailable(activity, isPremium) {
                onBack()
            }
        } else {
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    var selectedMonthIndex by remember { mutableIntStateOf(1) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val activeFortune = grimFortunes.find { it.month_index == selectedMonthIndex }
    val monthNames = HorrorViewModel.PERSIAN_MONTHS

    val shareFortuneText = if (activeFortune != null) {
        """
🔮 فال و طالع شوم متولدین ماه «${activeFortune.month_name}»:

«${activeFortune.title}»
${if (!activeFortune.omen_poem.isNullOrBlank()) "\n📜 «${activeFortune.omen_poem}»\n" else ""}
فرجام: ${activeFortune.doom_level ?: "بسیار شوم"}
پیش‌گویی جادوگر: ${activeFortune.fortune_text}

━━━━━━━━━━━━━━━━━━━━
💀 برای دریافت طالع سایر ماه‌ها و خواندن صدها روایت ترسناک، اپلیکیشن «عمارت وحشت» را از مایکت دریافت کنید:
https://myket.ir/app/com.apps.hororhouse
""".trimIndent()
    } else ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
    ) {
        GamingTopBar(
            title = "طالع شوم معبد جادوگری",
            subtitle = "پیش‌گویی ماهانه ارواح و کواکب تاریک",
            icon = Icons.Default.AutoAwesome,
            badgeText = monthNames.getOrNull(selectedMonthIndex - 1) ?: "طالع",
            onBack = handleBack
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // High-end Gothic ambient crack canvas background
            MirrorCracksCanvas(modifier = Modifier.fillMaxSize())

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // FULL-WIDTH HERO BANNER: GOTHIC CRYPT & ANCIENT MYSTIC RELIC
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    // Full-width Haunted Chamber Altar / Crypt background image
                    Image(
                        painter = painterResource(id = R.drawable.img_haunted_chamber_banner_1789721276056),
                        contentDescription = "محراب اسرارآمیز عمارت وحشت",
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

                    // Floating Mystic Relic Medallion & Titles
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
                                        text = "محراب طالع شوم",
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

                        // Center: Ancient Occult Relic Medallion portrait
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFDEC595), CircleShape)
                                .background(Color(0xFF0F081C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_gothic_occult_relic_1789721236578),
                                contentDescription = "مدالیون کهن عمارت وحشت",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Bottom Title & Subtitle inside banner
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "طالع شوم فلک تاریک",
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
                                text = "ـ نـدای شـوم کـتـیـبـه‌هـا و طـالـع تاریک مـاهـانـه ـ",
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

            // DISCLAIMER FOR ENTERTAINMENT
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1B1124),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF533B6B))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFDEC595),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "توجه: این فال و طالع شوم صرفاً جنبه سرگرمی، داستانی و فضاسازی داشته و نباید به عنوان واقعیت تلقی شود.",
                                color = Color(0xFFC5B6D4),
                                fontSize = 11.sp,
                                lineHeight = 17.sp
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

                                // Sharing Fortune Button with CTA and Ad
                                Button(
                                    onClick = {
                                        HorrorSoundManager.playClickSound()
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareFortuneText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری طالع شوم"))
                                        if (activity != null) {
                                            AdiveryAdManager.showAppOpenAdIfAvailable(activity, isPremium) {}
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B184F)),
                                    border = BorderStroke(1.dp, Color(0xFFDEC595)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFDEC595), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("اشتراک‌گذاری این طالع", color = Color(0xFFDEC595), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Single Clean Action: Choose/Change birth month
                                OutlinedButton(
                                    onClick = { showMonthPicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.7f)),
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
                                        HorrorSoundManager.playClickSound()
                                        showMonthPicker = false
                                        if (!isPremium && activity != null) {
                                            AdiveryAdManager.showAppOpenAdIfAvailable(
                                                activity = activity,
                                                isPremium = false,
                                                onComplete = {
                                                    selectedMonthIndex = monthIdx
                                                }
                                            )
                                        } else {
                                            selectedMonthIndex = monthIdx
                                        }
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

// AI Stories section is modularized in AiStoriesUserScreens.kt

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
// SINGLE STORY READER VIEW WITH ADJ STYLING
// ==========================================

@Composable
fun StoryReaderScreen(
    story: RealStory,
    viewModel: HorrorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isPremium by viewModel.isPremiumUser.collectAsState()
    
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

    LaunchedEffect(story.id) {
        if (NetworkUtils.isOnline(context)) {
            if (isUserSub) {
                viewModel.incrementSubmissionViews(story.id)
            } else {
                viewModel.incrementStoryViews(story.id)
            }
        }
    }

    val fontSize by viewModel.fontSize.collectAsState()
    val selectedFontIndex by viewModel.selectedFontIndex.collectAsState()

    val existingVote = remember(story.id) { viewModel.getUserVote(story.id) }
    var userRatingGiven by remember(story.id) {
        mutableIntStateOf(if (existingVote > 0f) existingVote.toInt() else 0)
    }
    var ratingSubmitted by remember(story.id) {
        mutableStateOf(existingVote > 0f)
    }

    var showReportDialog by remember { mutableStateOf(false) }

    val activeFontPreset = HorrorFontPresets.getOrNull(selectedFontIndex) ?: HorrorFontPresets[0]
    val bgColor = Color(0xFF09040F)
    val textColor = Color(0xFFEDE4F5)

    val shareSynopsis = remember(story.content) {
        if (story.content.length > 180) story.content.take(180) + "..." else story.content
    }
    val shareMessage = """
📖 بخشی از روایت ترسناک «${story.title}»:

«$shareSynopsis»

━━━━━━━━━━━━━━━━━━━━
🩸 برای مطالعه کامل این داستان و صدها روایت وحشت، برنامه «عمارت وحشت» را از مایکت دریافت کنید یا عنوان «${story.title}» را در برنامه جستجو کنید:
https://myket.ir/app/com.apps.hororhouse
""".trimIndent()

    if (showReportDialog) {
        ReportStoryDialog(
            storyId = story.id,
            storyTitle = story.title,
            storyAuthor = story.author ?: "کاتبان عمارت وحشت",
            storyType = if (isUserSub) "USER" else "REAL",
            viewModel = viewModel,
            onDismiss = { showReportDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // TOP APP BAR
        Surface(
            color = Color(0xFF06030B),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .border(1.dp, Color(0xFF2E1A3F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Color(0xFFDEC595)
                    )
                }

                Text(
                    text = story.title,
                    color = Color(0xFFDEC595),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = activeFontPreset.fontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Report Button
                    IconButton(
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            showReportDialog = true
                        }
                    ) {
                        Icon(
                            Icons.Default.ReportProblem,
                            contentDescription = "گزارش تخلف یا محتوای نامناسب",
                            tint = Color(0xFFFF4D4D)
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            HorrorSoundManager.playClickSound()
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری روایت"))
                            if (activity != null) {
                                AdiveryAdManager.showAppOpenAdIfAvailable(activity, isPremium) {}
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری", tint = Color(0xFFDEC595))
                    }
                }
            }
        }

        // READING CONTROLS STRIP
        Surface(
            color = Color(0xFF0C0714),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF261536))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Size Adjusters
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("اندازه قلم:", color = Color(0xFF8B8496), fontSize = 11.sp)
                        IconButton(
                            onClick = { if (fontSize > 12f) viewModel.setFontSize(fontSize - 1f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("A-", color = Color(0xFFDEC595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${fontSize.toInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { if (fontSize < 26f) viewModel.setFontSize(fontSize + 1f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("A+", color = Color(0xFFDEC595), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Font Family Selector Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(HorrorFontPresets) { preset ->
                        val isSel = preset.id == selectedFontIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF3B184F) else Color(0xFF140B1E))
                                .border(1.dp, if (isSel) Color(0xFFFF1E56) else Color(0xFF2E1A3F), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setFontFamily(preset.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset.name,
                                color = if (isSel) Color(0xFFFF1E56) else Color.White,
                                fontSize = 10.sp,
                                fontFamily = preset.fontFamily,
                                fontWeight = if (isSel) FontWeight.Bold else preset.fontWeight,
                                fontStyle = preset.fontStyle
                            )
                        }
                    }
                }
            }
        }

        val synopsisText = remember(story.content) {
            if (story.content.length > 180) story.content.take(180) + "..." else null
        }

        // READING CONTENT
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ATMOSPHERIC POSTER HERO BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.6f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AiStoryPosterGraphic(
                            posterUrl = story.cover_url,
                            storyId = story.id,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x99000000),
                                            Color(0xFF09040F)
                                        )
                                    )
                                )
                        )
                        // Story Title overlay at bottom of poster
                        Text(
                            text = story.title,
                            color = Color(0xFFDEC595),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = activeFontPreset.fontFamily,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        )
                    }
                }
            }

            // STORY META & ATTRIBUTION HEADER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12081E)),
                    border = BorderStroke(1.dp, Color(0xFF381A54)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔮 سبک: ${story.tags ?: if (isUserSub) "روایت کاربر" else "وحشت واقعی"}",
                                color = Color(0xFFDEC595),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = Color(0xFF260D18),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFDEC595).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF8B8496), modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "${liveStory.view_count} بازدید",
                                        color = Color(0xFFE0DAE8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✍️ مؤلف: ${story.author ?: "کاتبان عمارت وحشت"}",
                                color = Color(0xFFDEC595).copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            TextButton(
                                onClick = { showReportDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.ReportProblem,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("گزارش محتوا", color = Color(0xFFFF6B6B), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // SYNOPSIS (BLOCKQUOTE)
            if (!synopsisText.isNullOrBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF160920).copy(alpha = 0.7f))
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFDEC595).copy(alpha = 0.8f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "« $synopsisText »",
                            color = Color(0xFFDEC595),
                            fontSize = (fontSize - 1).sp,
                            fontFamily = activeFontPreset.fontFamily,
                            fontWeight = activeFontPreset.fontWeight,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = activeFontPreset.letterSpacing,
                            lineHeight = (fontSize * 1.5f).sp
                        )
                    }
                }
            }

            // FULL STORY CONTENT
            item {
                DisableSelection {
                    Text(
                        text = story.content,
                        color = textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * activeFontPreset.lineHeightMultiplier).sp,
                        fontFamily = activeFontPreset.fontFamily,
                        fontWeight = activeFontPreset.fontWeight,
                        fontStyle = activeFontPreset.fontStyle,
                        letterSpacing = activeFontPreset.letterSpacing,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // RATING AND FEEDBACK BOX (OVERHAULED 5-STAR SYSTEM)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12081E)),
                    border = BorderStroke(1.dp, Color(0xFFB8143F).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (ratingSubmitted) "⭐ امتیاز ثبت‌شده شما برای این داستان" else "به این داستان چه امتیازی می‌دهید؟",
                            color = Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        // 5 Stars Row (Empty by default, Filled on rating)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                val isSelected = star <= userRatingGiven
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
                                    enabled = !ratingSubmitted,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "ستاره $star",
                                        tint = if (isSelected) Color(0xFFFFD700) else Color(0xFF6B587E),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        // Rating Guide Hint
                        Text(
                            text = "راهنما: ۱ ستاره (ضعیف) ←──→ ۵ ستاره (شاهکار)",
                            color = Color(0xFF8B8496),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(color = Color(0xFF2A153E), thickness = 0.8.dp)

                        // Clear Distinction: User's Vote vs Community Average
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Rating Status
                            if (ratingSubmitted) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "رأی شما: $userRatingGiven ستاره",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "هنوز رأی نداده‌اید",
                                    color = Color(0xFF8B8496),
                                    fontSize = 11.sp
                                )
                            }

                            // Community Average
                            Text(
                                text = "📊 میانگین کاربران: ${String.format(java.util.Locale.US, "%.1f", liveStory.rating)} از ۵ (${liveStory.rating_count} رأی)",
                                color = Color(0xFFDEC595),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // SOCIAL NETWORK SHARING BUTTONS
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
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = android.net.Uri.parse("https://t.me/share/url?url=${android.net.Uri.encode("https://myket.ir/app/com.apps.hororhouse")}&text=${android.net.Uri.encode(shareMessage)}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری در تلگرام"))
                                        }
                                        if (activity != null) {
                                            AdiveryAdManager.showAppOpenAdIfAvailable(activity, isPremium) {}
                                        }
                                    }
                                    .padding(vertical = 8.dp),
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
                                        if (activity != null) {
                                            AdiveryAdManager.showAppOpenAdIfAvailable(activity, isPremium) {}
                                        }
                                    }
                                    .padding(vertical = 8.dp),
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
                                        try {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("روایت عمارت وحشت", shareMessage)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "متن و لینک داستان کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {}
                                        if (activity != null) {
                                            AdiveryAdManager.showAppOpenAdIfAvailable(activity, isPremium) {}
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("کپی متن و لینک 🔗", color = Color(0xFFDEC595), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Return to list button
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A0A17)),
                            border = BorderStroke(1.dp, Color(0xFFFF1E56)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("بازگشت به فهرست داستان‌ها", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeautifulSubmitStoryScreen(
    viewModel: HorrorViewModel,
    onBack: () -> Unit = {},
    onSubmissionComplete: () -> Unit
) {
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
    ) {
        GamingTopBar(
            title = "کتیبه ارسال رازها",
            subtitle = "ثبت روایات، اعترافات و وقایع ماورایی",
            icon = Icons.Default.Edit,
            badgeText = "ارسال لوح",
            onBack = onBack
        )

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



@Composable
fun GorgeousSettingsScreen(
    viewModel: HorrorViewModel,
    onNavigate: (UserDestination) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val selectedFontIndex by viewModel.selectedFontIndex.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()

    val activeFontPreset = HorrorFontPresets.getOrNull(selectedFontIndex) ?: HorrorFontPresets[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030106))
    ) {
        GamingTopBar(
            title = "تنظیمات",
            subtitle = "تنظیمات کلی برنامه و قلم‌ها",
            icon = Icons.Default.Settings,
            badgeText = "تنظیمات",
            onBack = onBack
        )

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
                text = "تنظیمات قلم و ظاهر داستان",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFFDEC595)
                )
            )
            Text(
                text = "ـ شخصی‌سازی اندازه و نوع قلم برای خوانش راحت‌تر روایات ترسناک ـ",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF8B8496),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // LIVE PREVIEW CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFFDEC595), cornerRadiusDp = 12f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140B1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("پیش‌نمایش زنده قلم:", color = Color(0xFFDEC595), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${fontSize.toInt()} sp - ${activeFontPreset.name}", color = Color(0xFFFF1E56), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0512), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF381A54), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "در دل تاریکی شب، نجوایی از اعماق عمارت به گوش می‌رسید... این یک نمونه متن برای بررسی خوانایی و زیبایی قلم [${activeFontPreset.name}] در عمارت وحشت است.",
                            color = Color(0xFFEDE4F5),
                            fontSize = fontSize.sp,
                            fontFamily = activeFontPreset.fontFamily,
                            fontWeight = activeFontPreset.fontWeight,
                            fontStyle = activeFontPreset.fontStyle,
                            letterSpacing = activeFontPreset.letterSpacing,
                            lineHeight = (fontSize * activeFontPreset.lineHeightMultiplier).sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // FONT FAMILY SELECTION CARD (5 DISTINCT PRESETS)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.3f), cornerRadiusDp = 12f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "انتخاب قلم (فونت و آرایش متنی)",
                        color = Color(0xFFDEC595),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    HorrorFontPresets.forEach { preset ->
                        val isSelected = selectedFontIndex == preset.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF2E1225) else Color(0xFF181024))
                                .border(1.dp, if (isSelected) Color(0xFFFF1E56) else Color(0xFF2E1A3F), RoundedCornerShape(8.dp))
                                .clickable { viewModel.setFontFamily(preset.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.name,
                                color = if (isSelected) Color(0xFFFF1E56) else Color.White,
                                fontSize = 13.sp,
                                fontFamily = preset.fontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else preset.fontWeight,
                                fontStyle = preset.fontStyle
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setFontFamily(preset.id) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF1E56))
                            )
                        }
                    }
                }
            }

            // FONT SIZE SLIDER CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(borderColor = Color(0xFFDEC595).copy(alpha = 0.3f), cornerRadiusDp = 12f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0918)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "اندازه قلم متن داستان",
                        color = Color(0xFFDEC595),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Slider(
                        value = fontSize,
                        onValueChange = { viewModel.setFontSize(it) },
                        valueRange = 12f..26f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF1E56),
                            activeTrackColor = Color(0xFFFF1E56),
                            inactiveTrackColor = Color(0xFF2E1A3F)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کوچک (12sp)", color = Color(0xFF8B8496), fontSize = 10.sp)
                        Text("پیش‌فرض (16sp)", color = Color(0xFF8B8496), fontSize = 10.sp)
                        Text("بزرگ (26sp)", color = Color(0xFF8B8496), fontSize = 10.sp)
                    }
                }
            }

            // VIP SUBSCRIPTION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .gothicBorder(
                        borderColor = if (isPremiumUser) Color(0xFF2ECC71).copy(alpha = 0.8f) else Color(0xFFDEC595).copy(alpha = 0.8f),
                        cornerRadiusDp = 12f
                    )
                    .clickable {
                        HorrorSoundManager.playClickSound()
                        onNavigate(UserDestination.SUBSCRIPTION)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremiumUser) Color(0xFF071B0E) else Color(0xFF19091F)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPremiumUser) Color(0xFF12381F) else Color(0xFF380724)
                            )
                            .border(
                                1.dp,
                                if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPremiumUser) "عضویت طلایی و دائمی (فعال)" else "خرید اشتراک دائمی (نسخه ویژه)",
                            color = if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPremiumUser) "تمامی امکانات باز و تبلیغات برنامه حذف هستند 👑" else "حذف تبلیغات، امکان ارسال داستان و عضویت VIP",
                            color = Color(0xFFEDE4F5).copy(alpha = 0.8f),
                            fontSize = 10.5.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isPremiumUser) Color(0xFF2ECC71) else Color(0xFFDEC595),
                        modifier = Modifier.size(16.dp)
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
                text = "احضار کاتب ارواح (هوش تاریکی)",
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
            val defaultRes = remember(index) {
                when (index % 3) {
                    0 -> R.drawable.img_poster_1_1788266550537
                    1 -> R.drawable.img_poster_2_1788266563762
                    else -> R.drawable.img_poster_3_1788266577786
                }
            }
            AsyncImage(
                model = submission.cover_url,
                placeholder = painterResource(id = defaultRes),
                error = painterResource(id = defaultRes),
                contentDescription = submission.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

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

@Composable
fun ReportStoryDialog(
    storyId: String,
    storyTitle: String,
    storyAuthor: String,
    storyType: String,
    viewModel: HorrorViewModel,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚠️ گزارش محتوای نامناسب",
                color = Color(0xFFDEC595),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "در صورتی که این داستان خلاف قوانین، دارای محتوای توهین‌آمیز یا نامناسب است، لطفاً دلیل خود را بنویسید تا کاتبان عمارت آن را بررسی و حذف کنند.",
                    color = Color(0xFFEDE8F5),
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Right
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("علت گزارش", color = Color(0xFF8B8496), fontSize = 11.sp) },
                    placeholder = { Text("مثال: حاوی واژگان نامناسب، نقض کپی‌رایت و...", color = Color(0xFF4C4556), fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 4,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, textAlign = TextAlign.Right),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB8143F),
                        unfocusedBorderColor = Color(0xFFDEC595).copy(alpha = 0.4f),
                        focusedLabelColor = Color(0xFFB8143F),
                        unfocusedLabelColor = Color(0xFFDEC595),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        android.widget.Toast.makeText(context, "لطفاً دلیل گزارش را بنویسید.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSending = true
                    viewModel.submitStoryReport(
                        com.example.data.StoryReport(
                            story_id = storyId,
                            story_title = storyTitle,
                            story_author = storyAuthor,
                            story_type = storyType,
                            reason = reason
                        )
                    ) { success ->
                        isSending = false
                        if (success) {
                            android.widget.Toast.makeText(context, "گزارش شما با موفقیت به ادمین ارسال شد.", android.widget.Toast.LENGTH_LONG).show()
                            onDismiss()
                        } else {
                            android.widget.Toast.makeText(context, "خطا در ارسال گزارش! لطفاً اتصال خود را بررسی کنید.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8143F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("ارسال گزارش", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color(0xFFDEC595), fontSize = 12.sp)
            }
        },
        containerColor = Color(0xFF140C22),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, Color(0xFFDEC595).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    )
}
