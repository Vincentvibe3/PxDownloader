package io.github.vincentvibe3.pixivdownloader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme

class SettingsActivity : ComponentActivity() {

    private val loggedInState:MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixivDownloaderTheme {
                var loggedIn = loggedInState.observeAsState()
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                    color = MaterialTheme.colors.background
                ) {
//                    Setting(loggedInState, loggedIn)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInState.value = true//cookiesState.value != null
        println(loggedInState.value)
    }
}

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
        Scaffold(topBar = {

           NavigationTopBar(name = "Settings", location = "home",navController = navController)

        }
        ){
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                Button(onClick = {
                    CookieManager.getInstance().removeAllCookies(null)
                    loggedIn.value = false//CookieManager.getInstance().getCookie("https://www.pixiv.net")!=null
                }) {
                    Text(text = loggedInState.value.toString())
                }
                NSFW(loggedInState)
            }

        }
    }
}