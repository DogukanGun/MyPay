package com.dag.mypayandroid.feature.home.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dag.mypayandroid.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dag.mypayandroid.feature.home.presentation.CardView
import com.dag.mypayandroid.ui.theme.*

@Composable
fun SendView(
    navController: NavController,
    amount: String = "0.4325 SPOT",
    backgroundColor: Color = DarkBackground,
    modifier: Modifier = Modifier
) {
    // Animation states
    val nfcIconScale = remember { Animatable(1f) }
    val cardScale = remember { Animatable(1f) }
    
    LaunchedEffect(true) {
        while (true) {
            // NFC icon pulse animation
            nfcIconScale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
            nfcIconScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
            
            // Small card pulse
            cardScale.animateTo(
                targetValue = 1.02f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
            cardScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F1F))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Pay",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Amount Display
        Text(
            text = amount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // NFC Icon
        Icon(
            painter = painterResource(R.drawable.contactless),
            contentDescription = "NFC",
            tint = PrimaryColor,
            modifier = Modifier
                .size(48.dp)
                .scale(nfcIconScale.value)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Hold near NFC reader",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )

        // Card
        Box(
            modifier = Modifier
                .scale(cardScale.value)
                .padding(vertical = 16.dp)
        ) {
            CardView()
        }

        Spacer(modifier = Modifier.weight(1f))

        // Cancel Button (optional)
        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Cancel",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Preview
@Composable
fun SendViewPreview() {
    SendView(rememberNavController())
} 