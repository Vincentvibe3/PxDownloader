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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.vincentvibe3.pixivdownloader.components.NavigationTopBar
import io.github.vincentvibe3.pixivdownloader.components.SettingItem
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.checkCookies

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
                    name = "Enable NSFW",
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)},
                    description = "Enable downloading of NSFW content",
                    enable = loggedIn.value?.not()
                ) {
                    if (loggedIn.value==true){
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = "CheckMark")
                    }
                }
                SettingItem(
                    name = "Logout",
                    onClick = {
                        CookieManager.getInstance().removeAllCookies(null)
                        model.loginStatus.value = checkCookies() },
                    description = "Logout of Pixiv to disable NSFW downloading",
                    enable = loggedInState.value,
                    titleColor = Color(0xFFF25A5F)
                )
            }

        }
    }
}