package com.dag.mypayandroid.feature.home.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.dag.mypayandroid.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    isVisible: Boolean,
    isSendMode: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { newValue ->
            newValue != SheetValue.PartiallyExpanded
        }
    )
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = (screenHeight * 2) / 3

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
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.height(sheetHeight),
                    onBackClick = { onDismiss() }
                )
            } else {
                ReceiveView(
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.height(sheetHeight),
                    onBackClick = { onDismiss() },
                    onContinueClick = { onDismiss() }
                )
            }
        }
    }
} 