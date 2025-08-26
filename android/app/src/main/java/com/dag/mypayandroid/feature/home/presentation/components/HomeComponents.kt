package com.dag.mypayandroid.feature.home.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dag.mypayandroid.base.components.CustomButton
import com.dag.mypayandroid.base.scroll.ReportScrollState
import com.dag.mypayandroid.ui.theme.*
import com.dag.mypayandroid.R
import com.dag.mypayandroid.feature.home.presentation.HomeVM
import com.dag.mypayandroid.feature.home.presentation.HomeVS
import androidx.compose.ui.res.stringResource

@Composable
fun HomeSuccessScreen(
    state: HomeVS.Success,
    askForPermission: Boolean,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    onReceive: () -> Unit,
    onSend: () -> Unit,
    onRefresh: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_view_available_balance),
                        color = primaryText,
                        fontSize = 14.sp
                    )

                    if (state.isLoadingBalance) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = PrimaryColor
                        )
                    } else {
                        Text(
                            text = state.balance.plus(" SOL") ?: stringResource(R.string.home_view_sol_balance, "0"),
                            color = primaryText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Display wallet address
                    state.walletAddress?.let { address ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (address.length > 20) address.take(8) + "..." + address.takeLast(8) else address,
                                color = primaryText,
                                fontSize = 14.sp,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }

                    // Show user email if available
                    state.userInfo?.let { userInfo ->
                        userInfo.email.let { email ->
                            if (email.isNotEmpty()) {
                                Text(
                                    text = email,
                                    color = primaryText,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.width(32.dp)
                ) {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.content_description_refresh)
                        )
                    }
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
                    text = stringResource(R.string.home_view_button_pay),
                    leadingIcon = painterResource(R.drawable.outgoing)
                ) {
                    onSend()
                }
                
                CustomButton(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SecondaryColor,
                    text = stringResource(R.string.home_view_button_receive),
                    leadingIcon = painterResource(R.drawable.incoming)
                ) {
                    onReceive()
                }
            }
        }

        // Portfolio Section
        item {
            Text(
                text = stringResource(R.string.home_view_portfolio),
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
                // Solana Card
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
                                text = stringResource(R.string.home_view_solana),
                                color = primaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (state.isLoadingBalance) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = PrimaryColor
                            )
                        } else {
                            Text(
                                text = state.balance.plus(" SOL") ?: stringResource(R.string.home_view_sol_balance, "0"),
                                color = primaryText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Additional Card - Could be other tokens
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
                                text = stringResource(R.string.home_view_usdc),
                                color = primaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = stringResource(R.string.home_view_usdc_balance, "0"),
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // User Information Section if available
        state.userInfo?.let { userInfo ->
            item {
                Text(
                    text = stringResource(R.string.home_view_account_information),
                    color = primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2F6))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        userInfo.name.let {
                            if (it.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_view_name),
                                        color = primaryText,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    )
                                    Text(
                                        text = it,
                                        color = primaryText,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    )
                                }
                            }
                        }
                        state.walletAddress?.let {
                            if (it.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_view_wallet),
                                        color = primaryText,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    )
                                    Text(
                                        text = it,
                                        color = primaryText,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    )
                                }
                            }
                        }
                        userInfo.verifier.let {
                            if (it.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_view_verifier),
                                        color = primaryText,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    )
                                    Text(
                                        text = it,
                                        color = primaryText,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BoxScope.HomeErrorView(
    state: HomeVS?,
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
                text = stringResource(R.string.home_view_error),
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
                    text = stringResource(R.string.home_view_retry),
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
            shouldShowPopup = true,
            walletAddress = "123456789abcdefghijklmnopqrstuvwxyz",
            balance = "123.45 SOL"
        ),
        askForPermission = false,
        animatedProgress = animatedProgress,
        onSend = {},
        onReceive = {},
        onRefresh = {}
    )
}