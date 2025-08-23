package com.dag.mypayandroid.feature.home.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.dag.mypayandroid.feature.home.presentation.components.HomeErrorView
import com.dag.mypayandroid.feature.home.presentation.components.HomeSuccessScreen
import com.dag.mypayandroid.feature.home.presentation.components.PaymentBottomSheet
import com.dag.mypayandroid.ui.theme.*
import com.web3auth.core.Web3Auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    web3Auth: Web3Auth,
    viewModel: HomeVM = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()
    val askForPermission by viewModel.askForPermission.collectAsState()
    val nfcPaymentState by viewModel.nfcPaymentState.collectAsState()

    // Animation properties
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(true) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(true) {
        viewModel.fetchUserDataAfterAuth(web3Auth)
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
                HomeErrorView(state)
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
                    },
                    onRefresh = {
                        viewModel.fetchUserDataAfterAuth(web3Auth)
                    }
                )

                PaymentBottomSheet(
                    isVisible = showPaymentSheet,
                    isSendMode = isSendMode,
                    onDismiss = { showPaymentSheet = false },
                    nfcPaymentState = nfcPaymentState,
                    resetNFCPaymentState = { viewModel.resetNFCPaymentState() },
                    initiateNFCPayment = { amount, pk -> viewModel.initiateNFCPayment(amount,pk) }
                )
            }

            HomeVS.LoggedOut -> {
                // Reset to login screen if logged out
                LaunchedEffect(Unit) {
                    viewModel.resetToLoginState()
                }
            }
        }
    }
}