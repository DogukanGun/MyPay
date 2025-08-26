package com.dag.mypayandroid

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.dag.mypayandroid.base.bottomnav.BottomNavMessageManager
import com.dag.mypayandroid.base.bottomnav.BottomNavigationBar
import com.dag.mypayandroid.base.components.CustomAlertDialog
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.helper.security.NFCHelper
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.base.helper.system.IntentManager
import com.dag.mypayandroid.base.navigation.DefaultNavigationHost
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.base.navigation.getDestinationTitle
import com.dag.mypayandroid.base.scroll.LocalScrollStateManager
import com.dag.mypayandroid.base.scroll.ScrollStateManager
import com.dag.mypayandroid.ui.theme.Background
import com.dag.mypayandroid.ui.theme.DarkBackground
import com.dag.mypayandroid.ui.theme.MyPayAndroidTheme
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.Network
import com.web3auth.core.types.Web3AuthOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : FragmentActivity(), NfcAdapter.CreateNdefMessageCallback {

    private val currentRoute = mutableStateOf<String?>(null)
    private val mainVM: MainVM by viewModels()

    @Inject
    lateinit var alertDialogManager: AlertDialogManager

    @Inject
    lateinit var bottomNavMessageManager: BottomNavMessageManager

    @Inject
    lateinit var scrollStateManager: ScrollStateManager

    @Inject
    lateinit var defaultNavigator: DefaultNavigator

    @Inject
    lateinit var activityHolder: ActivityHolder

    companion object {
        lateinit var web3Auth: Web3Auth
    }

    @Inject
    lateinit var intentManager: IntentManager

    lateinit var nfcHelper: NFCHelper

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityHolder.setActivity(this)
        nfcHelper = NFCHelper(this)
        val showAlert = mutableStateOf(false)
        val alertDialogModel = mutableStateOf<AlertDialogModel?>(null)
        // Handle user signing in when app is not alive
        web3Auth = Web3Auth(
            Web3AuthOptions(
                clientId = BuildConfig.web3authKey,
                network = Network.SAPPHIRE_DEVNET,
                redirectUrl = "com.dag.mypayandroid://auth".toUri(),
            ), this
        )
        web3Auth.setResultUrl(intent?.data)
        // Call initialize() in onCreate() to check for any existing session.
        val sessionResponse: CompletableFuture<Void> = web3Auth.initialize()
        sessionResponse.whenComplete { _, error ->
            if (error == null) {
                println("PrivKey: " + web3Auth.getPrivkey())
                println("ed25519PrivKey: " + web3Auth.getEd25519PrivKey())
                println("Web3Auth UserInfo" + web3Auth.getUserInfo())
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
                // Ideally, you should initiate the login function here.
            }
        }
        // Initialize the lifecycle scope coroutine only after alertDialogManager is available
        if (::alertDialogManager.isInitialized && lifecycleScope.isActive) {
            lifecycleScope.launch {
                alertDialogManager.alertFlow.collect { model ->
                    alertDialogModel.value = model
                    showAlert.value = true
                }
            }
        }
        setContent {
            val scrollState = scrollStateManager.scrollState.collectAsState()
            CompositionLocalProvider(
                LocalScrollStateManager provides scrollStateManager
            ) {
                MyPayAndroidTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Background),
                            ) {
                                if (mainVM.isBottomNavActive(currentRoute.value)) {
                                    val title = currentRoute.value?.let { getDestinationTitle(it) } ?: ""
                                    TopAppBar(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = DarkBackground
                                        ),
                                        title = {
                                            Text(
                                                text = getDestinationTitle(title),
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        },
                                        actions = {
                                            IconButton(
                                                onClick = {
                                                    lifecycleScope.launch {
                                                        intentManager
                                                            .requestIntent(
                                                                com.dag.mypayandroid.base.data.Intent.Web3AuthLogout(web3Auth)
                                                            )
                                                        defaultNavigator.navigate(Destination.LoginScreen) {
                                                            launchSingleTop = true
                                                            popUpTo(0) { inclusive = true }
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                    contentDescription = "Sign Out",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    lifecycleScope.launch {
                                                        intentManager
                                                            .requestIntent(
                                                                com.dag.mypayandroid.base.data.Intent.Web3WalletManagement(web3Auth)
                                                            )
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.baseline_wallet),
                                                    contentDescription = "Sign Out",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    )
                                }

                                DefaultNavigationHost(
                                    navigator = defaultNavigator,
                                    modifier = Modifier.weight(1f),
                                    startDestination = Destination.Splash,
                                    web3Auth = web3Auth
                                ) {
                                    currentRoute.value = it.destination.route
                                        ?.split(".")?.last()
                                }

                                if (mainVM.isBottomNavActive(currentRoute.value)) {
                                    BottomNavigationBar(
                                        currentRoute = currentRoute.value,
                                        isScrolled = scrollState.value.isScrolling,
                                        messageManager = bottomNavMessageManager,
                                        onItemSelected = {
                                            lifecycleScope.launch {
                                                defaultNavigator.navigate(it) {
                                                    launchSingleTop = true
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        },
                                        onExpandClick = {
                                            scrollStateManager.toggle()
                                        }
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(showAlert.value && alertDialogModel.value != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .blur(16.dp)
                                    .zIndex(10f)
                            ) {
                                alertDialogModel.value?.let { model ->
                                    CustomAlertDialog(
                                        alertDialogModel = model,
                                        showAlert = showAlert,
                                        defaultNavigator = defaultNavigator
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Web3Auth.getCustomTabsClosed()) {
            Toast.makeText(this, "User closed the browser.", Toast.LENGTH_SHORT).show()
            web3Auth.setResultUrl(null)
            Web3Auth.setCustomTabsClosed(false)
        }
        if (nfcHelper.nfcAdapter != null) {
            nfcHelper.nfcAdapter?.enableForegroundDispatch(this, nfcHelper.pendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.nfcAdapter?.disableForegroundDispatch(this)
    }

    /**
     * This is the crucial callback! Android calls this method at the exact moment
     * it needs a message to send to another device.
     */
    @Deprecated("Deprecated in Java")
    override fun createNdefMessage(event: NfcEvent?): NdefMessage? {
        return nfcHelper.getAndClearMessageToSend()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        when(intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED-> {
                nfcHelper.handleIntent(intent)
            }
            else -> {
                web3Auth.setResultUrl(intent.data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityHolder.clearActivity()
    }

}