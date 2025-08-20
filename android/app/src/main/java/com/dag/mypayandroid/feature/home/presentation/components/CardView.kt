package com.dag.mypayandroid.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dag.mypayandroid.R
import com.dag.mypayandroid.ui.theme.DarkBackground

@Composable
fun CardView(
    walletAddress: String = "He2Azv8NoLgZZKUUfn6sZpB6g5tAH1YoYa3hZoS31T2R"
) {
    var rowHeight by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .height(128.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        rowHeight = size.height
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.solanalogo),
                    contentDescription = "solana",
                    tint = Color.Unspecified,
                    modifier = Modifier.height((rowHeight).dp)
                )
                Text(
                    walletAddress.slice(1..8) +
                            "..." +
                            walletAddress.slice(walletAddress.length - 8..walletAddress.length - 1),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
@Preview
fun CardViewPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        CardView()
    }
}