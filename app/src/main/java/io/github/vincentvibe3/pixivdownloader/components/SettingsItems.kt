package io.github.vincentvibe3.pixivdownloader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingItem(
    name:String,
    onClick:() -> Unit,
    description:String,
    enable:Boolean?,
    titleColor:Color=MaterialTheme.colors.onBackground,
    indicator:@Composable ()->Unit={}
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = true,
                onClick = if (enable==true){
                    onClick
                } else {
                    {}
                }
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
            .padding(24.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    color=titleColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color=Color.Gray,
                    style=MaterialTheme.typography.caption
                )
            }
            indicator()
        }
    }
}