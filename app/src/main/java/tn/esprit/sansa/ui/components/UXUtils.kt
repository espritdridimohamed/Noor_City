package tn.esprit.sansa.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * A component that applies a staggered entrance animation (fade + slide up) 
 * to its content based on an index.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    delayStep: Int = 100,
    duration: Int = 500,
    content: @Composable () -> Unit
) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = index * delayStep,
                easing = LinearOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = index * delayStep,
                easing = LinearOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

/**
 * Alternative approach using graphicsLayer for more control and performance.
 */
@Composable
fun StaggeredItem(
    index: Int,
    delayStep: Int = 70,
    content: @Composable () -> Unit
) {
    var itemVisible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (itemVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = index * delayStep,
            easing = EaseOutExpo
        ),
        label = "alpha"
    )
    
    val translateY by animateFloatAsState(
        targetValue = if (itemVisible) 0f else 50f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = index * delayStep,
            easing = EaseOutExpo
        ),
        label = "translateY"
    )

    LaunchedEffect(Unit) {
        itemVisible = true
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = translateY
        }
    ) {
        content()
    }
}

/**
 * A reusable Shimmer effect modifier.
 */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this.background(brush)
}

/**
 * Skeleton for Technician/Citizen/Sensor Card
 */
@Composable
fun CardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar Skeleton
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer()
            )
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Name Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                // Subtitle Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}
