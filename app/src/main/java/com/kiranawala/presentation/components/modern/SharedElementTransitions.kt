package com.kiranawala.presentation.components.modern

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared Element Transition Utilities for 2025 Modern Design
 * 
 * Features:
 * - Smooth scale and fade transitions
 * - Spring-based animations for natural feel
 * - Configurable duration and easing
 */

/**
 * Shared element transition key for store cards
 */
fun getStoreCardTransitionKey(storeId: String) = "store_card_$storeId"
fun getStoreImageTransitionKey(storeId: String) = "store_image_$storeId"
fun getStoreNameTransitionKey(storeId: String) = "store_name_$storeId"

/**
 * Enter transition for store detail screen
 */
@OptIn(ExperimentalAnimationApi::class)
fun storeDetailEnterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + slideInVertically(
        initialOffsetY = { it / 4 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
}

/**
 * Exit transition for store list screen
 */
@OptIn(ExperimentalAnimationApi::class)
fun storeListExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(200, easing = FastOutLinearInEasing)
    ) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(200, easing = FastOutLinearInEasing)
    )
}

/**
 * Pop enter transition (back navigation)
 */
@OptIn(ExperimentalAnimationApi::class)
fun storeListPopEnterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    )
}

/**
 * Pop exit transition (back navigation)
 */
@OptIn(ExperimentalAnimationApi::class)
fun storeDetailPopExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(200, easing = FastOutLinearInEasing)
    ) + slideOutVertically(
        targetOffsetY = { it / 4 },
        animationSpec = tween(200, easing = FastOutLinearInEasing)
    )
}

/**
 * Crossfade transition for content changes
 */
@Composable
fun CrossfadeContent(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit
) {
    Crossfade(
        targetState = targetState,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        modifier = modifier,
        label = "content-crossfade"
    ) { state ->
        content(state)
    }
}

/**
 * Animated visibility with slide and fade
 */
@Composable
fun AnimatedSlideInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Scale and fade animation for cards
 */
@Composable
fun AnimatedCardContent(
    visible: Boolean,
    delay: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ),
        modifier = modifier
    ) {
        content()
    }
}

