@file:OptIn(ExperimentalTextApi::class)

package com.dag.nexwallet.base.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

@Composable
@Preview
fun PreviewStarWarsCrawlScreen(){
    val crawlText = """
Episode IV

A NEW HOPE



It is a period of civil war.
Rebel spaceships, striking
from a hidden base, have won
their first victory against
the evil Galactic Empire.

During the battle, Rebel
spies managed to steal secret
plans to the Empire's
ultimate weapon, the DEATH
STAR, an armored space
station with enough power
to destroy an entire planet.

Pursued by the Empire's
sinister agents, Princess
Leia races home aboard her
starship, custodian of the
stolen plans that can save
her people and restore
freedom to the galaxy....
    """.trimIndent()

    StarWarsCrawlScreen(crawlText) {}
}


@Composable
fun StarWarsCrawlScreen(
    text: String,
    onDone: ()-> Unit
) {
    var hasAnimationFinished by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(true) } // Start animating immediately
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create audio manager
    val audioManager = remember { AudioManager(context) }
    
    // Start music when animation begins
    LaunchedEffect(Unit) {
        scope.launch {
            audioManager.playManifestMusic()
        }
    }
    
    // Clean up audio when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            audioManager.stopMusic()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Star field background
        StarField()

        if (isAnimating) {
            StarWarsCrawl(
                text = text,
                onAnimationEnd = {
                    isAnimating = false
                    hasAnimationFinished = true
                    audioManager.stopMusic() // Stop music when animation ends
                }
            )
        }

        if (hasAnimationFinished && !isAnimating) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "May the Force be with you",
                    color = Color.Yellow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))
                CustomButton(
                    backgroundColor = Color.Red,
                    text = "Start Again"
                )  { 
                    isAnimating = true
                    scope.launch {
                        audioManager.playManifestMusic()
                    }
                }
                CustomButton(
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    text = "Go Back"
                )  { onDone() }
            }
        }
    }
}

@Composable
fun StarWarsCrawl(
    text: String,
    onAnimationEnd: () -> Unit
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var startAnimation by remember { mutableStateOf(false) }

    // Start animation after 1 second delay
    LaunchedEffect(Unit) {
        delay(1000)
        startAnimation = true
    }

    val animationDuration = 35000 // 35 seconds for slower, more authentic feel

    val animationProgress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearEasing
        ),
        finishedListener = {
            if (startAnimation) {
                onAnimationEnd()
            }
        },
        label = "crawl_progress"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        drawStarWarsCrawl(
            text = text,
            progress = animationProgress,
            canvasSize = size,
            density = density,
            textMeasurer = textMeasurer
        )
    }
}

private fun DrawScope.drawStarWarsCrawl(
    text: String,
    progress: Float,
    canvasSize: Size,
    density: Density,
    textMeasurer: TextMeasurer
) {
    val lines = text.split("\n")
    val baseLineHeight = with(density) { 80.dp.toPx() } // Increased line spacing

    // Calculate total height of all text
    val totalTextHeight = lines.size * baseLineHeight

    // Animation starts with text below screen, ends with text above screen
    val startY = canvasSize.height + 400f
    val endY = -totalTextHeight - 600f
    val currentBaseY = startY + (endY - startY) * progress

    // Star Wars perspective parameters - the "sweet spot" is around 70% of screen height
    val sweetSpotY = canvasSize.height * 0.7f
    val vanishingPointY = canvasSize.height * 0.15f // Very high vanishing point

    lines.forEachIndexed { index, line ->
        if (line.trim().isEmpty()) return@forEachIndexed

        val lineY = currentBaseY + (index * baseLineHeight)

        // Skip if completely off screen
        if (lineY > canvasSize.height + 300f || lineY < vanishingPointY - 100f) {
            return@forEachIndexed
        }

        // Calculate dramatic perspective scale based on distance from sweet spot
        val distanceFromSweet = abs(lineY - sweetSpotY)
        val maxDistance = sweetSpotY - vanishingPointY

        val scale = when {
            lineY < vanishingPointY -> 0.1f // Very small at vanishing point
            lineY > canvasSize.height -> {
                // Text gets bigger as it approaches from bottom
                val distanceFromBottom = lineY - canvasSize.height
                (2.0f + distanceFromBottom * 0.01f).coerceAtMost(4.0f)
            }
            else -> {
                // Normal perspective calculation
                val normalizedDistance = distanceFromSweet / maxDistance
                (1.2f - normalizedDistance * 0.9f).coerceIn(0.15f, 2.5f)
            }
        }

        // Calculate opacity - fade at edges
        val alpha = when {
            lineY < vanishingPointY + 50f -> ((lineY - vanishingPointY) / 50f).coerceIn(0f, 1f)
            lineY > canvasSize.height - 100f -> ((canvasSize.height - lineY) / 100f).coerceIn(0f, 1f)
            else -> 1f
        }

        if (alpha <= 0.01f || scale <= 0.01f) return@forEachIndexed

        // Determine text properties with larger base sizes
        val isTitle = index == 0 // "Episode IV"
        val isSubtitle = index == 2 // "A NEW HOPE"
        val baseFontSize = when {
            isTitle -> 36f // Larger title
            isSubtitle -> 42f // Larger subtitle
            else -> 24f // Larger body text
        }

        val fontSize = with(density) { (baseFontSize * scale).sp }

        val textStyle = TextStyle(
            fontSize = fontSize,
            fontWeight = if (isTitle || isSubtitle) FontWeight.Bold else FontWeight.Normal,
            color = Color.Yellow.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            letterSpacing = if (isTitle || isSubtitle) 0.3.sp else 0.1.sp
        )

        // Measure the text with generous constraints
        val maxWidth = (canvasSize.width * 1.2f).toInt() // Allow wider text
        val textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(line),
            style = textStyle,
            constraints = Constraints(
                maxWidth = maxWidth
            )
        )

        // Calculate position with minimal perspective trapezoid effect
        val textWidth = textLayoutResult.size.width.toFloat()
        val centerX = canvasSize.width / 2f

        // Very subtle horizontal perspective shift only for very distant text
        val perspectiveXShift = when {
            lineY < vanishingPointY + 100f -> {
                val distanceFromVanishing = lineY - vanishingPointY
                (distanceFromVanishing * 0.02f).coerceIn(-20f, 20f)
            }
            else -> 0f
        }

        // Draw the text centered
        translate(
            left = centerX - (textWidth / 2f) + perspectiveXShift,
            top = lineY
        ) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset.Zero
            )
        }
    }
}

// Enhanced star field background
@Composable
fun StarField() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val random = Random(42) // Fixed seed for consistent stars

        // Multiple layers of stars for depth
        repeat(150) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val radius = random.nextFloat() * 1.5f + 0.3f
            val alpha = random.nextFloat() * 0.7f + 0.3f

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }

        // Brighter stars
        repeat(50) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val radius = random.nextFloat() * 2f + 1f

            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}