package com.dag.mypayandroid.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dag.mypayandroid.R

enum class BlockchainChain(val displayName: String, val symbol: String) {
    ETHEREUM("Ethereum", "ETH"),
    SOLANA("Solana", "SOL");

    val iconRes: Int
        get() = when (this) {
            ETHEREUM -> R.drawable.ethereum_logo
            SOLANA -> R.drawable.solanalogo
        }
        
    val iconColor: Color
        get() = when (this) {
            ETHEREUM -> Color(0xFF627EEA)
            SOLANA -> Color(0xFFAB47BC)
        }
}

@Composable
fun ChainSelector(
    selectedChain: BlockchainChain,
    onChainChanged: (BlockchainChain) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BlockchainChain.values().forEach { chain ->
            ChainButton(
                chain = chain,
                isSelected = selectedChain == chain,
                onTap = { onChainChanged(chain) }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChainButton(
    chain: BlockchainChain,
    isSelected: Boolean,
    onTap: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.Yellow else Color(0xFFF5F5F5)
    val textColor = if (isSelected) Color.Black else Color.Black
    val borderColor = if (isSelected) Color.Yellow else Color(0xFFE0E0E0)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onTap() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chain logo
            Image(
                painter = painterResource(id = chain.iconRes),
                contentDescription = "${chain.displayName} logo",
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = chain.symbol,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Preview
@Composable
private fun ChainSelectorPreview() {
    var selectedChain by remember { mutableStateOf(BlockchainChain.SOLANA) }
    
    ChainSelector(
        selectedChain = selectedChain,
        onChainChanged = { selectedChain = it }
    )
}