package io.github.vincentvibe3.pixivdownloader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import io.github.vincentvibe3.pixivdownloader.components.NavigationTopBar
import io.github.vincentvibe3.pixivdownloader.components.SettingItem
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.checkCookies

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NSFW(loggedInState: State<Boolean?>){
    val context = LocalContext.current
    val logInDialog = remember { mutableStateOf(false) }
    loggedInState.value?.let {
        Button(enabled = !it, onClick = {
            logInDialog.value = true
        }) {
            if (loggedInState.value == true) {
                Text(text = "NSFW Enabled")
            } else {
                Text(text = "Enable NSFW")
            }
        }
    }
    if (logInDialog.value) {
        AlertDialog(
            onDismissRequest = { logInDialog.value = false },
            title = { Text(text = "Enable NSFW?") },
            text = {
                Text("You will have to log in to Pixiv in the next step.\nOnce logged in you can close the browser.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                        logInDialog.value = false
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        logInDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Setting(loggedIn: MutableLiveData<Boolean>, loggedInState: State<Boolean?>, navController:NavController) {
    PixivDownloaderTheme {
        val context = LocalContext.current
        Scaffold(topBar = {
           NavigationTopBar(name = "Settings", location = "home",navController = navController)

        }
        ){
            Column(modifier = Modifier
                .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                SettingItem(
                    name = "Enable NSFW",
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)},
                    description = "Enable downloading of NSFW content",
                    enable = loggedInState.value?.not()
                ) {
                    if (loggedInState.value==true){
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = "CheckMark")
                    }
                }
                SettingItem(
                    name = "Logout",
                    onClick = {
                        CookieManager.getInstance().removeAllCookies(null)
                        loggedIn.value = checkCookies()},
                    description = "Logout of Pixiv to disable NSFW downloading",
                    enable = loggedInState.value,
                    titleColor = Color(0xFFF25A5F)
                )
            }

        }
    }
}