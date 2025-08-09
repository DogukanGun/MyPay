package com.dag.mypayandroid.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    innerPadding:Dp = 0.dp,
    shape: Shape = RoundedCornerShape(32.dp),
    textColor:Color = Color.White,
    text: String,
    trailingIcon: Painter? = null,
    leadingIcon: Painter? = null,
    enabled: Boolean = true,
    onClick:()->Unit
){
    if (trailingIcon != null && leadingIcon != null) {
        throw Exception("cannot set trailing and leading icon in same time")
    }
    Button(
        modifier = modifier,
        shape = shape,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            leadingIcon?.let {
                Icon(painter = it,"leading icon")
            }
            Text(
                modifier = Modifier.padding(innerPadding),
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
            trailingIcon?.let {
                Icon(painter = it,"trailing icon")
            }
        }
    }
}

@Composable
@Preview
fun CustomButtonPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center
    ) {
        CustomButton(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            backgroundColor = Color.Red,
            text = "Test"
        ){}
    }
}