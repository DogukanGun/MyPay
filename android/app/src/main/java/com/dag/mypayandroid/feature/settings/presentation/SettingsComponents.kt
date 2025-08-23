package com.dag.mypayandroid.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dag.mypayandroid.feature.settings.data.SettingCellData
import com.dag.mypayandroid.feature.settings.data.SettingCellType
import com.dag.mypayandroid.ui.theme.Background
import com.dag.mypayandroid.ui.theme.DarkBackground

@Composable
fun SettingCell(
    modifier: Modifier = Modifier,
    settingCellData: SettingCellData
) {

    var isChecked by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = settingCellData.text,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        when(settingCellData.cellType) {
            SettingCellType.SWITCH -> {
                Switch(
                    checked = isChecked,
                    onCheckedChange = { newValue ->
                        isChecked = newValue
                        settingCellData.onValueChange?.let { it(newValue) }
                                      },
                )
            }
            SettingCellType.EXTERNAL_LINK -> {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = settingCellData.onClicked
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }       
            }
            SettingCellType.INFO -> {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = settingCellData.onClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SettingCellPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(2) { index->
                SettingCell(
                    modifier = Modifier,
                    settingCellData = SettingCellData(
                        text = "test",
                        cellType = if (index == 1) SettingCellType.EXTERNAL_LINK else SettingCellType.SWITCH ,
                        onClicked = {},
                        onValueChange = {}
                    )
                )
            }
        }
    }
}