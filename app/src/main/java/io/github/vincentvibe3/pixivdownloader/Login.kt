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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.CustomChromeClient

import androidx.compose.foundation.layout.*
import io.github.vincentvibe3.pixivdownloader.components.ActivityTopBar


class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixivDownloaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = colorResource(R.color.white)
                ) {
                    Login()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
@Preview(showBackground = true)
fun Login(){
    PixivDownloaderTheme {
        Scaffold (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            topBar = {
                ActivityTopBar(name = "Login", true)
            },
            backgroundColor = colorResource(id = R.color.white)
        ) {
            val progress = remember { mutableStateOf(0) }
            val context = LocalContext.current
            val url = "https://accounts.pixiv.net/login?lang=en0"
            val webView = remember {
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
            if (webView.value.url =="https://www.pixiv.net/en/"){
                (context as Activity).finish()
            }
            LinearProgressIndicator(
                progress = progress.value.toFloat() / 100,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = if (progress.value!=100){
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
                    webView.value.reload()
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
                            webView.value
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