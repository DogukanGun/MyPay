package com.dag.mypayandroid.feature.home.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dag.mypayandroid.base.components.CustomButton
import com.dag.mypayandroid.base.scroll.ReportScrollState
import com.dag.mypayandroid.ui.theme.*
import com.dag.mypayandroid.R

@Composable
fun HomeSuccessScreen(
    state: HomeVS.Success,
    askForPermission: Boolean,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    onReceive: () -> Unit,
    onSend: () -> Unit
) {
    val scrollState = rememberLazyListState()

    // Monitor scroll state for bottom navigation bar
    val isScrolling = scrollState.isScrollInProgress
    var previousFirstVisibleIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    val isScrollingUp = remember(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        val isUp = when {
            scrollState.firstVisibleItemIndex < previousFirstVisibleIndex -> true
            scrollState.firstVisibleItemIndex > previousFirstVisibleIndex -> false
            scrollState.firstVisibleItemScrollOffset < previousScrollOffset -> true
            else -> false
        }

        previousFirstVisibleIndex = scrollState.firstVisibleItemIndex
        previousScrollOffset = scrollState.firstVisibleItemScrollOffset
        isUp
    }

    // Report scroll state to the central manager
    ReportScrollState(isScrolling = isScrolling, isScrollingUp = isScrollingUp)

    if (askForPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimePermissionDialog(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onPermissionDenied = {},
                onPermissionGranted = {}
            )
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .alpha(animatedProgress.value),
    ) {
        // Balance Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Available Balance",
                    color = secondaryText,
                    fontSize = 14.sp
                )
                Text(
                    text = "$112,340.00",
                    color = primaryText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "$10,240.00",
                        color = primaryText,
                        fontSize = 14.sp
                    )
                    Text(
                        text = " +12%",
                        color = Color(0xFF22C55E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomButton(
                    modifier = Modifier.weight(1f),
                    backgroundColor = PrimaryColor,
                    textColor = Color.Black,
                    text = "Pay",
                    leadingIcon = painterResource(R.drawable.outgoing)
                ) {
                    onSend()
                }
                
                CustomButton(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SecondaryColor,
                    text = "Receive",
                    leadingIcon = painterResource(R.drawable.incoming)
                ) {
                    onReceive()
                }
            }
        }

        // Portfolio Section
        item {
            Text(
                text = "Portfolio",
                color = primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Portfolio Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sbux Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2F6))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                color = PrimaryColor
                            ) { }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sbux",
                                color = primaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$80,30",
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+1.80 (+1.32%)",
                            color = Color(0xFF22C55E),
                            fontSize = 14.sp
                        )
                    }
                }

                // Nike Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2F6))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                color = SecondaryColor
                            ) { }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nike",
                                color = primaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$111,05",
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "-2.85 (-0.32%)",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BoxScope.HomeErrorView(
    state: HomeVS?,
    viewModel: HomeVM
) {
    Card(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(24.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = (state as HomeVS.Error).message,
                color = secondaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {  },
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = "Retry",
                    color = primaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun RuntimePermissionDialog(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                LocalContext.current,
                permission) != PackageManager.PERMISSION_GRANTED) {
            val requestLocationPermissionLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        onPermissionGranted()
                    } else {
                        onPermissionDenied()
                    }
                }
            SideEffect {
                requestLocationPermissionLauncher.launch(permission)
            }
        }
    }
}


@Preview
@Composable
fun HomeSuccessScreenPreview() {
    val animatedProgress = remember { Animatable(1f) }
    HomeSuccessScreen(
        state = HomeVS.Success(
            shouldShowPopup = true
        ),
        askForPermission = false,
        animatedProgress = animatedProgress,
        onSend = {},
        onReceive = {}
    )
}