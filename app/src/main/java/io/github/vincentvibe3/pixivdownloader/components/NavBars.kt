package io.github.vincentvibe3.pixivdownloader.components

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
@Preview
fun ActivityPreview(){
    ActivityTopBar(name = "Lorem ipsum"){
        TextButton(onClick = {

        }) {
            Text(text = "Download")
        }
    }
}

@Composable
fun NavigationTopBar(
    name:String,
    location:String,
    navController: NavController,
    elevate:Boolean = false
){
    TopAppBar(
        backgroundColor = MaterialTheme.colors.background,
        elevation = if (!elevate){
            0.dp
        }else{
            5.dp
        }
    ) {
        IconButton(onClick = {
            navController.navigate(location)
        }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colors.onBackground
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.h6,
            color=MaterialTheme.colors.onBackground
        )
    }
}

@Composable
fun ActivityTopBar(
    name:String,
    elevate:Boolean = false,
    backIcon: @Composable () -> Unit = @Composable {
        val context = LocalContext.current
        IconButton(onClick = {
            (context as Activity?)?.finish()
        }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint=MaterialTheme.colors.onBackground
            )
        } },
    action: @Composable () -> Unit = {},
){
    val context = LocalContext.current
    TopAppBar(
        backgroundColor = MaterialTheme.colors.background,
        elevation = if (!elevate){
            0.dp
        }else{
            5.dp
        }
    ) {
        backIcon()
        Text(
            text = name,
            style = MaterialTheme.typography.h6,
            color=MaterialTheme.colors.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ){
            action()
        }


    }
}