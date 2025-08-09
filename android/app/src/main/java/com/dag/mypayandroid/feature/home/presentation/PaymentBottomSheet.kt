package com.dag.mypayandroid.feature.home.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.dag.mypayandroid.ui.theme.DarkBackground
import kotlin.compareTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    isVisible: Boolean,
    isSendMode: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = (screenHeight * 2) / 3

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
                .nestedScroll(object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return if (available.y < 0) {
                            Offset.Zero // block upward drag
                        } else {
                            Offset.Unspecified // allow downward drag
                        }
                    }
                }),
            containerColor = DarkBackground,
            dragHandle = null
        ) {
            if (isSendMode) {
                SendView(
                    navController = navController,
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.height(sheetHeight)
                )
            } else {
                ReceiveView(
                    navController = navController,
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.height(sheetHeight)
                )
            }
        }
    }
} 