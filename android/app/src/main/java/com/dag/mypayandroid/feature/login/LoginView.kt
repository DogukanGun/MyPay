package com.dag.mypayandroid.feature.login

import android.os.Build
import android.widget.Space
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dag.mypayandroid.base.components.CustomTextField
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.ui.theme.gradientStart
import com.dag.mypayandroid.ui.theme.primaryText
import com.web3auth.core.Web3Auth

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun LoginView(
    viewModel: LoginVM = hiltViewModel(),
    web3Auth: Web3Auth,
    navController: NavHostController
){
    // Email input state
    var email by remember { mutableStateOf(TextFieldValue("")) }
    val loginState = viewModel.viewState.collectAsState()

    LaunchedEffect(true) {
        viewModel.initialise(web3Auth)
    }

    when(loginState.value) {
        is LoginVS.StartLogin -> {
            val currentState = loginState.value as LoginVS.StartLogin
            // Login Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Welcome to MyPay",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Text(
                        text = "Please enter your email to continue",
                        fontSize = 16.sp,
                        color = primaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    CustomTextField(
                        modifier = Modifier
                            .padding(bottom = 32.dp),
                        label = "Email Address",
                        isPassword = false
                    ) { email = email.copy(text = it) }

                }
                Button(
                    onClick = {
                        viewModel.login(web3Auth, email.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gradientStart
                    ),
                    enabled = !currentState.isLoading
                ) {
                    if (currentState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Continue",
                            fontSize = 16.sp,
                            color = primaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        LoginVS.StartHomePage -> {
            navController.navigate(Destination.HomeScreen)
        }
        null -> {

        }
    }
}