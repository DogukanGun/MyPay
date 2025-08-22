package com.dag.mypayandroid.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dag.mypayandroid.feature.settings.data.SettingCellData
import com.dag.mypayandroid.feature.settings.data.Settings


@Composable
fun SettingsView(
    settingsVM: SettingsVM = hiltViewModel()
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Settings.entries.size) { index->
                val setting = Settings.entries[index]
                SettingCell(
                    settingCellData = SettingCellData(
                        text = setting.name,
                        cellType = setting.cellType,
                        onClicked = {
                            settingsVM.executeSetting(setting)
                        },
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsViewPreview(){
    SettingsView()
}