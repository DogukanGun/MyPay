package com.dag.mypayandroid.feature.home.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dag.mypayandroid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavController,
    viewModel: HomeVM = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()
    val askForPermission by viewModel.askForPermission.collectAsState()
    val shouldShowPopup by viewModel.shouldShowPopup.collectAsState()
    val context = LocalContext.current
    val packageManager = context.packageManager

    // Animation properties
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(true) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    // Bottom sheet state
    var showPaymentSheet by remember { mutableStateOf(false) }
    var isSendMode by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when (state) {
            null, HomeVS.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    color = gradientStart,
                    strokeWidth = 4.dp
                )
            }
            
            is HomeVS.Error -> {
                HomeErrorView(state, viewModel)
            }
            
            is HomeVS.Success -> {
                HomeSuccessScreen(
                    state = (state as HomeVS.Success),
                    askForPermission = askForPermission,
                    animatedProgress = animatedProgress,
                    onSend = { 
                        isSendMode = true
                        showPaymentSheet = true 
                    },
                    onReceive = { 
                        isSendMode = false
                        showPaymentSheet = true 
                    }
                )
            }

            HomeVS.LoggedOut -> {
                // Handle in LaunchedEffect
            }
        }

        PaymentBottomSheet(
            isVisible = showPaymentSheet,
            isSendMode = isSendMode,
            onDismiss = { showPaymentSheet = false }
        )
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    HomeView(
        navController = rememberNavController()
    )
}