package com.kiranawala.presentation.components.modern

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiranawala.presentation.theme.KiranaColors

/**
 * Animated grocery cart loading indicator
 * Shows a bouncing cart with rotating wheels
 */
@Composable
fun GroceryCartLoadingAnimation(
    message: String = "Fetching your past orders...",
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated cart
        AnimatedGroceryCart(primaryColor = primaryColor)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Loading text
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnimatedGroceryCart(
    primaryColor: Color
) {
    // Bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "cart_animation")
    
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Wheel rotation
    val wheelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheel_rotation"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2 + bounceOffset
            
            // Cart body (basket)
            val cartPath = Path().apply {
                // Bottom left
                moveTo(centerX - 35f, centerY + 10f)
                // Bottom right
                lineTo(centerX + 35f, centerY + 10f)
                // Top right
                lineTo(centerX + 25f, centerY - 20f)
                // Top left
                lineTo(centerX - 25f, centerY - 20f)
                close()
            }
            
            drawPath(
                path = cartPath,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Cart grid lines (basket pattern)
            for (i in 1..2) {
                val yPos = centerY - 20f + (i * 10f)
                drawLine(
                    color = primaryColor,
                    start = Offset(centerX - 25f + (i * 5f), yPos),
                    end = Offset(centerX + 25f - (i * 5f), yPos),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Handle
            drawLine(
                color = primaryColor,
                start = Offset(centerX + 25f, centerY - 20f),
                end = Offset(centerX + 35f, centerY - 35f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Wheels (rotating)
            val leftWheelX = centerX - 20f
            val rightWheelX = centerX + 20f
            val wheelY = centerY + 20f
            val wheelRadius = 8f
            
            // Left wheel
            rotate(wheelRotation, Offset(leftWheelX, wheelY)) {
                drawCircle(
                    color = primaryColor,
                    radius = wheelRadius,
                    center = Offset(leftWheelX, wheelY),
                    style = Stroke(width = 3.dp.toPx())
                )
                // Wheel spokes
                drawLine(
                    color = primaryColor,
                    start = Offset(leftWheelX - wheelRadius, wheelY),
                    end = Offset(leftWheelX + wheelRadius, wheelY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(leftWheelX, wheelY - wheelRadius),
                    end = Offset(leftWheelX, wheelY + wheelRadius),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Right wheel
            rotate(wheelRotation, Offset(rightWheelX, wheelY)) {
                drawCircle(
                    color = primaryColor,
                    radius = wheelRadius,
                    center = Offset(rightWheelX, wheelY),
                    style = Stroke(width = 3.dp.toPx())
                )
                // Wheel spokes
                drawLine(
                    color = primaryColor,
                    start = Offset(rightWheelX - wheelRadius, wheelY),
                    end = Offset(rightWheelX + wheelRadius, wheelY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(rightWheelX, wheelY - wheelRadius),
                    end = Offset(rightWheelX, wheelY + wheelRadius),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Items in cart (bouncing groceries)
            val itemColors = listOf(
                primaryColor,
                primaryColor.copy(alpha = 0.7f),
                primaryColor.copy(alpha = 0.5f)
            )
            
            itemColors.forEachIndexed { index, color ->
                val xOffset = -15f + (index * 15f)
                drawCircle(
                    color = color,
                    radius = 6f - (index * 1f),
                    center = Offset(centerX + xOffset, centerY - 10f - (index * 3f))
                )
            }
        }
    }
}

/**
 * Alternative: Animated delivery scooter
 */
@Composable
fun DeliveryScooterAnimation(
    message: String = "Delivering your orders...",
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedScooter(primaryColor = primaryColor)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnimatedScooter(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "scooter_animation")
    
    // Wheel rotation
    val wheelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheel_rotation"
    )
    
    // Subtle vibration
    val vibration by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vibration"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2 + vibration
            
            // Scooter body
            drawLine(
                color = primaryColor,
                start = Offset(centerX - 30f, centerY - 10f),
                end = Offset(centerX + 20f, centerY - 10f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Handlebar
            drawLine(
                color = primaryColor,
                start = Offset(centerX - 25f, centerY - 10f),
                end = Offset(centerX - 25f, centerY - 30f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Wheels
            val frontWheelX = centerX + 20f
            val backWheelX = centerX - 30f
            val wheelY = centerY + 10f
            val wheelRadius = 10f
            
            // Back wheel
            rotate(wheelRotation, Offset(backWheelX, wheelY)) {
                drawCircle(
                    color = primaryColor,
                    radius = wheelRadius,
                    center = Offset(backWheelX, wheelY),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(backWheelX - wheelRadius, wheelY),
                    end = Offset(backWheelX + wheelRadius, wheelY),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Front wheel
            rotate(wheelRotation, Offset(frontWheelX, wheelY)) {
                drawCircle(
                    color = primaryColor,
                    radius = wheelRadius,
                    center = Offset(frontWheelX, wheelY),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(frontWheelX, wheelY - wheelRadius),
                    end = Offset(frontWheelX, wheelY + wheelRadius),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Delivery box
            drawRect(
                color = primaryColor,
                topLeft = Offset(centerX - 10f, centerY - 25f),
                size = androidx.compose.ui.geometry.Size(15f, 15f),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
