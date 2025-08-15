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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dag.mypayandroid.base.components.CustomTextField
import com.dag.mypayandroid.ui.theme.*
import com.web3auth.core.Web3Auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavController,
    web3Auth: Web3Auth,
    viewModel: HomeVM = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()
    val askForPermission by viewModel.askForPermission.collectAsState()
    val shouldShowPopup by viewModel.shouldShowPopup.collectAsState()
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    // Email input state 
    var email by remember { mutableStateOf(TextFieldValue("")) }
    
    // Animation properties
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(true) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        
        // Initialize the ViewModel to check login state
        viewModel.initialise()
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
            is HomeVS.LoginRequired -> {
                val loginState = state as HomeVS.LoginRequired
                
                // Login Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome to MyPay",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    
                    Text(
                        text = "Please enter your email to continue",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    CustomTextField(
                        modifier = Modifier,
                        label = "Email Address",
                        isPassword = false
                    ) { email = email.copy(text = it) }

                    Button(
                        onClick = {
                            viewModel.login(web3Auth, email.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradientStart
                        ),
                        enabled = !loginState.isLoading
                    ) {
                        if (loginState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Continue")
                        }
                    }
                }
            }
            
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
                
                PaymentBottomSheet(
                    isVisible = showPaymentSheet,
                    isSendMode = isSendMode,
                    onDismiss = { showPaymentSheet = false }
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