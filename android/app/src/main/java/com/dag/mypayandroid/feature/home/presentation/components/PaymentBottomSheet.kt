package com.dag.mypayandroid.feature.home.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dag.mypayandroid.ui.theme.DarkBackground
import com.dag.mypayandroid.feature.home.presentation.HomeVM
import androidx.hilt.navigation.compose.hiltViewModel
import com.dag.mypayandroid.feature.home.presentation.NFCPaymentState
import org.sol4k.PublicKey
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.dag.mypayandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    userWalletAddress: String,
    isVisible: Boolean,
    isSendMode: Boolean,
    nfcPaymentState: NFCPaymentState,
    selectedChain: BlockchainChain,
    onChainChanged: (BlockchainChain) -> Unit,
    onDismiss: () -> Unit,
    resetNFCPaymentState: () -> Unit,
    initiateNFCPayment: (Int, PublicKey) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { newValue ->
            newValue != SheetValue.PartiallyExpanded
        }
    )
    val context = LocalContext.current
    var sendViewTitle by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = (screenHeight * 2) / 3

    // Handle different NFC payment states
    LaunchedEffect(nfcPaymentState) {
        when (val state = nfcPaymentState) {
            is NFCPaymentState.RequestReceived -> {
                // Automatically handle received payment request
                if (!isSendMode) {
                    // Prompt user to confirm payment
                    // You might want to show a dialog or update UI
                }
            }
            is NFCPaymentState.Completed -> {
                // Payment completed, dismiss bottom sheet
                onDismiss()
                resetNFCPaymentState()
            }
            is NFCPaymentState.Error -> {
                // Handle error (show error message)
                Log.e("PaymentBottomSheet", "NFC Payment Error: ${state.message}")
            }
            else -> {}
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f),
            containerColor = DarkBackground,
            dragHandle = null
        ) {
            if (isSendMode) {
                SendView(
                    title = sendViewTitle,
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.height(sheetHeight),
                    userWalletAddress = userWalletAddress,
                    onBackClick = { onDismiss() }
                )
            } else {
                ReceiveView(
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.height(sheetHeight),
                    selectedChain = selectedChain,
                    onChainChanged = onChainChanged,
                    onBackClick = { onDismiss() },
                    onContinueClick = { amount, publicKey ->
                        onDismiss()
                        sendViewTitle = ContextCompat.getString(context,R.string.payment_bottom_sheet_send)
                        initiateNFCPayment(amount, publicKey)
                    }
                )
            }
        }
    }
} 