package com.dag.mypayandroid.feature.settings.data

enum class Settings(val cellType: SettingCellType) {
    PRIVATE_KEY(SettingCellType.EXTERNAL_LINK),
    FEEDBACK_FORM(SettingCellType.EXTERNAL_LINK),
    LEGAL(SettingCellType.EXTERNAL_LINK)
}

enum class SettingCellType {
    SWITCH,
    EXTERNAL_LINK,
    INFO
}

data class SettingCellData(
    var text: String,
    var cellType: SettingCellType,
    var onClicked: () -> Unit,
    var onValueChange: ((value: Any) -> Unit)? = null
)