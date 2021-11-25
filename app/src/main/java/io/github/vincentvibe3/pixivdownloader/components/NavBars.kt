package io.github.vincentvibe3.pixivdownloader.components

import android.app.Activity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun NavigationTopBar(name:String, location:String, navController: NavController,  elevate:Boolean = false){
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = if (!elevate){0.dp}else{5.dp}) {
        IconButton(onClick = {
            navController.navigate(location)
        }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onBackground)
        }
        Text(text = name, style = MaterialTheme.typography.h6, color=MaterialTheme.colors.onBackground)
    }
}

@Composable
fun ActivityTopBar(name:String, elevate:Boolean = false){
    val context = LocalContext.current
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = if (!elevate){0.dp}else{5.dp}) {
        IconButton(onClick = {
            (context as Activity?)?.finish()
        }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint=MaterialTheme.colors.onBackground)
        }
        Text(text = name, style = MaterialTheme.typography.h6, color=MaterialTheme.colors.onBackground)
    }
}