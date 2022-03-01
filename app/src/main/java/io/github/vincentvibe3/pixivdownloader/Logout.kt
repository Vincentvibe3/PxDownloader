package io.github.vincentvibe3.pixivdownloader

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.vincentvibe3.pixivdownloader.components.ActivityTopBar
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.CustomChromeClient
import io.github.vincentvibe3.pixivdownloader.utils.checkAppleCookie
import io.github.vincentvibe3.pixivdownloader.utils.checkGoogleCookie
import io.github.vincentvibe3.pixivdownloader.utils.checkTwitterCookie


class LogoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixivDownloaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = colorResource(R.color.white)
                ) {
                    Logout()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
@Preview(showBackground = true)
fun Logout(){
    val context = LocalContext.current
    val logoutUrls = arrayListOf(
        "https://www.pixiv.net/",
    )
    if (checkAppleCookie()){
        logoutUrls.add("https://appleid.apple.com/")
    }
    if (checkGoogleCookie()){
        logoutUrls.add("https://accounts.google.com")
    }
    if (checkTwitterCookie()){
        logoutUrls.add("https://twitter.com/")
    }
    val currentPage = remember {
        mutableStateOf(0)
    }
    var webView: MutableState<WebView>? = null
    val initialOpen = remember {
        mutableStateOf(true)
    }
    PixivDownloaderTheme {
        Scaffold (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            topBar = {
                ActivityTopBar(name = "Logout", true) {
                    if (!initialOpen.value){
                        TextButton(onClick = {
                            if (currentPage.value+1 == logoutUrls.size){
                                (context as Activity).finish()
                            } else {
                                currentPage.value++
                            }

                        }) {
                            Text(text = "I have logged out")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                }
            },
            backgroundColor = colorResource(id = R.color.white)
        ) {
            if (initialOpen.value) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("You will have to manually")
                        Text("logout of services you used to login")
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { initialOpen.value = false }) {
                        Text(text = "OK", color = MaterialTheme.colors.onPrimary)
                    }
                }
            } else {
                val progress = remember { mutableStateOf(0) }
                val url = logoutUrls[currentPage.value]
                webView = remember {
                    mutableStateOf(WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = WebViewClient()
                        val settings = this.settings
                        settings.javaScriptEnabled = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Mobile Safari/537.36"
                        this.settings.loadWithOverviewMode = true
                        this.settings.useWideViewPort = true
                        CookieManager.getInstance().setAcceptCookie(true)
                        this.webChromeClient = CustomChromeClient(progress)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    })
                }
                val webProgress = rememberSwipeRefreshState(progress.value != 100)
                LinearProgressIndicator(
                    progress = progress.value.toFloat() / 100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = if (progress.value != 100) {
                        MaterialTheme.colors.primary
                    } else {
                        MaterialTheme.colors.background
                    }
                )
                SwipeRefresh(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    state = webProgress,
                    onRefresh = {
                        webView!!.value.reload()
                    },
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(ScrollState(0))
                    ) {
                        AndroidView(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            factory = {
                                webView!!.value
                            },
                            update = {
                                it.loadUrl(url)
                                progress.value = it.progress
                            })
                    }
                }
            }
        }
    }
}