package io.github.vincentvibe3.pixivdownloader

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
//    val context = LocalContext.current
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = if (!elevate){0.dp}else{5.dp}) {
        IconButton(onClick = {
            navController.navigate(location)
//            (context as Activity?)?.finish()
        }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = name, style = MaterialTheme.typography.h6)
    }
}

@Composable
fun ActivityTopBar(name:String, elevate:Boolean = false){
    val context = LocalContext.current
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = if (!elevate){0.dp}else{5.dp}) {
        IconButton(onClick = {
            (context as Activity?)?.finish()
        }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = name, style = MaterialTheme.typography.h6)
    }
}

@ExperimentalMaterialApi
@Composable
fun BottomDrawerTopBar(name: String, elevate: Boolean = false, state: BottomSheetState){
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = if (!elevate){0.dp}else{5.dp}) {
        IconButton(onClick = {
            coroutineScope.launch {
                state.collapse()
            }

        }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = name, style = MaterialTheme.typography.h6)
    }
}