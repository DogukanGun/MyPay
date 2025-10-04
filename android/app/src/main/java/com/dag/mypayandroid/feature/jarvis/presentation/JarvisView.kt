package com.dag.mypayandroid.feature.jarvis.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dag.mypayandroid.feature.jarvis.presentation.components.StatusCards
import com.dag.mypayandroid.feature.jarvis.presentation.components.VoiceWaveAnimation
import com.dag.mypayandroid.ui.theme.Background

@Composable
fun JarvisView(
    viewModel: JarvisVM = hiltViewModel()
) {
    val currentCard by viewModel.processHandler.currentCard.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val jarvisState by viewModel.viewState.collectAsState()
    val requestLocationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            viewModel.handleAudioPermissionResult(isGranted)
        }
    var isPressed by remember { mutableStateOf(false) }
    
    val waveAlpha by animateFloatAsState(
        targetValue = if (isListening) 1f else 0.8f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "wave_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val isActiveState = when (jarvisState) {
        is JarvisVS.Listening, is JarvisVS.Processing -> true
        else -> false
    }
    
    val pulseScale by animateFloatAsState(
        targetValue = if (isActiveState) 1.1f else 1f,
        animationSpec = if (isActiveState) {
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(300)
        },
        label = "pulse"
    )
    
    // Handle automatic backend sending when processing starts
    LaunchedEffect(jarvisState) {
        when (val state = jarvisState) {
            is JarvisVS.Processing -> {
                viewModel.processHandler.show("Handling...")
            }
            is JarvisVS.Ready -> {
                viewModel.sendToBackend()
            }
            is JarvisVS.Error -> {
                viewModel.processHandler.show("Error: ${state.message}", duration = 3000L)
            }
            is JarvisVS.AskPermission -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            is JarvisVS.Idle -> {
                viewModel.checkPermission()
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .scale(scale * pulseScale)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = {
                                when (jarvisState) {
                                    is JarvisVS.Listening -> {
                                        viewModel.stopListening()
                                    }
                                    is JarvisVS.Idle -> {
                                        viewModel.startListening()
                                    }
                                    else -> {
                                        // Do nothing if processing or in error state
                                    }
                                }
                            }
                        )
                    }
                    .graphicsLayer { alpha = waveAlpha }
                    .background(
                        Color(0xFF8EC5FC).copy(alpha = if (isListening) 0.1f else 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                VoiceWaveAnimation(
                    isActive = isActiveState,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = when (jarvisState) {
                    is JarvisVS.Listening -> "Listening..."
                    is JarvisVS.Processing -> "Handling..."
                    is JarvisVS.Error -> "Error occurred"
                    is JarvisVS.Ready -> "Ready to send"
                    else -> if (currentCard != null) "" else "Tap to speak with Jarvis"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color(0xFF374151),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable {
                    when (jarvisState) {
                        is JarvisVS.Listening -> {
                            viewModel.stopListening()
                        }
                        is JarvisVS.Idle -> {
                            viewModel.startListening()
                        }
                        else -> {
                            // Do nothing if processing or in error state
                        }
                    }
                }
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            StatusCards(
                cardData = currentCard,
                onCardDismiss = {
                    viewModel.processHandler.hide()
                }
            )
        }
    }
}