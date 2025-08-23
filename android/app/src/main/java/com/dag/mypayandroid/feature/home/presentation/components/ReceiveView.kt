package com.dag.mypayandroid.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.dag.mypayandroid.base.components.CustomButton
import com.dag.mypayandroid.base.components.CustomTextField
import com.dag.mypayandroid.ui.theme.*
import org.sol4k.PublicKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveView(
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onContinueClick: (amount: Int, publicKey: PublicKey) -> Unit
) {
    var amount by remember { mutableIntStateOf(0) }
    var recipient by remember { mutableStateOf("") }
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
                onClick = onBackClick,
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
                text = "Convert",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryText,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Amount Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currency Selector
                    Card(
                        modifier = Modifier
                            .weight(0.4f)
                            .clickable { /* Handle currency selection */ },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    tint = Color.Unspecified,
                                    painter = painterResource(R.drawable.solanalogo),
                                    contentDescription = "Solana"
                                )
                                Text(
                                    text = "SOL",
                                    color = primaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select currency",
                                tint = primaryText
                            )
                        }
                    }

                    // Amount Input
                    CustomTextField(
                        modifier = Modifier.weight(0.8f),
                        label = "Amount",
                        onlyNumberKeyboard = true,
                        onTextChange = { amount = it.toInt() }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Recipient",
                        onTextChange = { recipient = it }
                    )
                }
            }
        }

        // Available Balance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available balance",
                    color = primaryText
                )
                Text(
                    text = "$112,340.00",
                    color = primaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Exchange Fee
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exchange fee",
                    color = primaryText
                )
                Text(
                    text = "$20",
                    color = primaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        CustomButton(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = PrimaryColor,
            text = "Continue",
            enabled = amount != 0,
            textColor = Color.Black
        ) {
            onContinueClick(amount, PublicKey(recipient))
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
@Preview
fun ReceiveViewPreview() {
    ReceiveView(
        DarkBackground,
        onBackClick = {}
    ) { amount, publicKey -> }
}