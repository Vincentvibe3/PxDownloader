package io.github.vincentvibe3.pixivdownloader

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import io.github.vincentvibe3.pixivdownloader.components.NavigationTopBar
import io.github.vincentvibe3.pixivdownloader.components.SettingItem
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme

@Composable
fun Settings(model:AppViewModel, loggedIn:State<Boolean?>, loggedInState: State<Boolean?>, navController:NavController) {
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
                    name = "Log In",
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)},
                    description = "Enables downloading NSFW art",
                    enable = loggedIn.value?.not()
                ) {
                    if (loggedIn.value==true){
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = "CheckMark")
                    }
                }
                SettingItem(
                    name = "Logout",
                    onClick = {
                        val intent = Intent(context, LogoutActivity::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY) },
                    description = "Logout of Pixiv to disable NSFW downloading",
                    enable = loggedInState.value,
                    titleColor = Color(0xFFF25A5F)
                )
            }

        }
    }
}