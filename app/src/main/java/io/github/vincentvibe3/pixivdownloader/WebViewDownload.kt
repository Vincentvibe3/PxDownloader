package io.github.vincentvibe3.pixivdownloader

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.vincentvibe3.pixivdownloader.components.ActivityTopBar
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.CustomChromeClient
import io.github.vincentvibe3.pixivdownloader.utils.PixivMetadata


class WebViewDownload : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        val id = bundle?.getString("id")
        setContent {
            PixivDownloaderTheme {
                Surface(
                    color = colorResource(R.color.white)
                ) {
                    WebViewDl(id)
                }
            }
        }
    }
}

internal class HtmlInterceptor(private val id: MutableState<String?>) {
    @JavascriptInterface
    fun processHTML(html: String?) {
        val idValue = id.value
        if (html != null && idValue != null) {
            PixivMetadata.PendingRequests[idValue] = html
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewDl(initId:String?){
    val id = remember {mutableStateOf(initId)}
    var webView:MutableState<WebView>? = null
    PixivDownloaderTheme {
        Scaffold (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            topBar = {
                ActivityTopBar(
                    name = if (id.value == null) {"Browse"} else {"Fetch Data"},
                    elevate = true
                ) {
                    if (id.value == null) {
                        TextButton(onClick = {
                            id.value = webView?.value?.url?.let { getInputId(it) }
                            val idVal = id.value
                        }) {
                            Text(text = "Download")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            },
            backgroundColor = colorResource(id = R.color.white)
        ) {
            val progress = remember { mutableStateOf(0) }
            val context = LocalContext.current
            val url = if (id.value==null){
                "https://www.pixiv.net"
            } else {
                "https://www.pixiv.net/ajax/illust/${id.value}/ugoira_meta"
            }
            webView = remember {
                mutableStateOf(WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.addJavascriptInterface(HtmlInterceptor(id), "HTMLOUT")
                    webViewClient = object:WebViewClient(){
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (id.value!=null&&url=="https://www.pixiv.net/ajax/illust/${id.value}/ugoira_meta"){
                                this@apply.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByTagName('pre')[0].textContent);")
                                (context as Activity).finish()
                            }

                        }
                    }
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