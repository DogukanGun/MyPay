package com.dag.mypayandroid.feature.jarvis.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

data class StatusCardData(
    val message: String,
    val duration: Long? = null,
    val condition: (() -> Boolean)? = null,
    val backgroundColor: Color? = null
)

@Composable
fun StatusCards(
    modifier: Modifier = Modifier,
    cardData: StatusCardData?,
    onCardDismiss: () -> Unit = {}
) {
    var isVisible by remember(cardData) { mutableStateOf(cardData != null) }
    var currentCard by remember { mutableStateOf(cardData) }
    
    LaunchedEffect(cardData) {
        if (cardData != null) {
            currentCard = cardData
            isVisible = true
            
            when {
                cardData.condition != null -> {
                    while (isVisible && cardData.condition.invoke()) {
                        delay(500)
                    }
                    isVisible = false
                    delay(300)
                    onCardDismiss()
                }
                cardData.duration != null -> {
                    delay(cardData.duration)
                    isVisible = false
                    delay(300)
                    onCardDismiss()
                }
                else -> {
                    delay(5000)
                    isVisible = false
                    delay(300)
                    onCardDismiss()
                }
            }
        } else {
            isVisible = false
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LayeredCards(
                message = currentCard?.message ?: "",
                backgroundColor = currentCard?.backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun LayeredCards(
    message: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .offset(y = 16.dp)
                .scale(0.85f)
                .zIndex(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .offset(y = 8.dp)
                .scale(0.9f)
                .zIndex(2f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .zIndex(3f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
    }
}