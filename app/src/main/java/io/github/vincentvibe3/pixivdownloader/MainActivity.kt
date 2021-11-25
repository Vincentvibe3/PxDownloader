package io.github.vincentvibe3.pixivdownloader

import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.compose.runtime.livedata.observeAsState
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val loggedInState:MutableLiveData<Boolean> = MutableLiveData(CookieManager.getInstance().getCookie("https://www.pixiv.net") != null)

    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixivDownloaderTheme {
                // A surface container using the 'background' color from the theme
                var loggedIn = loggedInState.observeAsState()
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
//        loggedIn.value = cookies.value!=null
                    NavHost(navController = navController, startDestination = "home" ){
                        composable("Settings"){
                            Setting(loggedIn = loggedInState, loggedInState = loggedIn, navController)
                        }
                        composable("home"){
                            DefaultPreview(navController = navController)
                        }
//                        composable("download"){
//                            Download(navController)
//                        }
                    }
//                    DefaultPreview(loggedInState, loggedIn)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInState.value = CookieManager.getInstance().getCookie("https://www.pixiv.net") != null
        println(loggedInState.value)
    }

}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview(navController:NavController) {
    PixivDownloaderTheme {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val downloadSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, confirmStateChange = {
            if (it == ModalBottomSheetValue.Hidden){
                focusManager.clearFocus()
            }
            true
        })
        val coroutineScope = rememberCoroutineScope()
        ModalBottomSheetLayout(
            sheetState = downloadSheetState,
            sheetShape = MaterialTheme.shapes.large,
            sheetBackgroundColor = MaterialTheme.colors.background,
            scrimColor = Color(0x90000000),
            sheetContent = {
                Submit(downloadSheetState)

        }) {

            Scaffold(bottomBar = {
                BottomAppBar(cutoutShape = CircleShape, backgroundColor = MaterialTheme.colors.surface) {
                    Row(modifier = Modifier.fillMaxWidth() ,horizontalArrangement = Arrangement.End){
                        IconButton(onClick = {
                            val intent = Intent(context, SettingsActivity::class.java)
//                        startActivity(context, intent, Bundle.EMPTY)
                            navController.navigate("settings")
                        }) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Back")
                        }

                    }

                }
            },
                topBar = {
                    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = 0.dp) {
                        Text(text = "Queue", style = MaterialTheme.typography.h6, modifier = Modifier.padding(start = 24.dp, top = 24.dp))
                    }
                },
                floatingActionButton = {
                ExtendedFloatingActionButton(onClick = {
                    val intent = Intent(context, DownloadActivity::class.java)
//                startActivity(context, intent, Bundle.EMPTY)
//                navController.navigate("download")
                    coroutineScope.launch {
                        downloadSheetState.show()

                    }

                }, text = {
                    Text(text = "Download")
                }, icon = {
                    Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = "Back")
                })
            },
                floatingActionButtonPosition = FabPosition.Center,
                isFloatingActionButtonDocked = true
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Button(onClick = { coroutineScope.launch { downloadSheetState.show() } }) {
                        Text("Click to show sheet")
                    }

                }
            }
        }
    }
}