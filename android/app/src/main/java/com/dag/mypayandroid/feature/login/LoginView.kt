package com.dag.mypayandroid.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.ui.theme.primaryText
import com.dag.mypayandroid.R
import com.dag.mypayandroid.ui.theme.DarkBackground
import com.dag.mypayandroid.ui.theme.MyPayAndroidTheme

@Composable
fun LoginView(
    viewModel: LoginVM = hiltViewModel(),
    navController: NavHostController
){
    // Email input state
    val loginState = viewModel.viewState.collectAsState()

    when(loginState.value) {
        is LoginVS.StartLogin -> {
            val currentState = loginState.value as LoginVS.StartLogin
            // Login Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.login_screen_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(R.string.login_screen_text),
                        fontSize = 16.sp,
                        color = primaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.loginWithX()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBackground,
                        contentColor = Color.White
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Login via",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                painter = painterResource(R.drawable.x_logo_black),
                                contentDescription = "X Logo",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
        }

        LoginVS.StartHomePage -> {
            navController.navigate(Destination.HomeScreen)
        }
        null -> {

        }

        is LoginVS.Error -> TODO()
    }
}


@Preview
@Composable
fun LoginViewPreview(){
    MyPayAndroidTheme {
        LoginView(navController=rememberNavController())
    }
}