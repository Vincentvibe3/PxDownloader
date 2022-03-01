package io.github.vincentvibe3.pixivdownloader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.vincentvibe3.pixivdownloader.components.ActivityTopBar
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.CustomChromeClient
import io.github.vincentvibe3.pixivdownloader.utils.DownloadWorker


class Browse : ComponentActivity() {

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

internal class HtmlInterceptor(val context: Context) {
    @JavascriptInterface
    fun processHTML(html: String?, id:String?) {
        if (html != null && id != null) {
            val context = context
            val sharedprefs = context.getSharedPreferences("dlReq", Context.MODE_PRIVATE)
            sharedprefs.edit()
                .putString(id, html)
                .apply()
            val workData = Data.Builder()
                .putString("id", id)
                .build()
            val downloadWorkRequest: WorkRequest =
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(workData)
                    .addTag("video")
                    .addTag(id)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueue(downloadWorkRequest)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewDl(initId:String?){
    val id = remember {mutableStateOf(initId)}
    val context = LocalContext.current
    var webView:MutableState<WebView>? = null
    val initialUrl = if (initId==null){
        "https://www.pixiv.net"
    } else {
        "https://www.pixiv.net/ajax/illust/$initId/ugoira_meta"
    }
    val currentUrl = remember { mutableStateOf(initialUrl) }
    val history = remember<MutableList<String>> {
        mutableListOf()
    }
    val historyPos = remember {
        mutableStateOf(-1)
    }
    val navState = remember {
        mutableStateOf(0)
    }
    BackHandler {
        (context as Activity).finish()
    }
    PixivDownloaderTheme {
        Scaffold (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            bottomBar = {
                ActivityTopBar(
                    name = "",
                    elevate = true,
                    backIcon = {
                        Row() {
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(
                                onClick = {
                                    navState.value = -1
                                    webView?.let { goBack(history, historyPos.value, it.value) }
                                    historyPos.value--
                                },
                                enabled = historyPos.value!=0
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.height(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(onClick = {
                                navState.value = 1
                                webView?.let { goForward(history, historyPos.value, it.value) }
                                historyPos.value++
                            },
                                enabled = historyPos.value+1!=history.size
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = "Back",
                                    modifier = Modifier.height(20.dp)
                                )
                            }

                        }
                    }
                ) {
                    if (id.value == null) {
                        Row() {
                            TextButton(
                                onClick = {
                                    id.value = webView?.value?.url?.let { getInputId(it) }
                                    webView?.value?.loadUrl("https://www.pixiv.net/ajax/illust/${id.value}/ugoira_meta")
                                },
                                enabled = currentUrl.value.startsWith("https://www.pixiv.net/en/artworks")
                            ) {
                                Text(text = "Download")
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                    }
                }
            },
            topBar = {
                ActivityTopBar(
                    name = if (id.value == null) {"Browse"} else {"Fetch Data"},
                    elevate = true,
                ) {}
            },
            backgroundColor = colorResource(id = R.color.white)
        ) { padding ->
            val progress = remember { mutableStateOf(0) }
            webView = remember {
                mutableStateOf(WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.addJavascriptInterface(HtmlInterceptor(context), "HTMLOUT")
                    webViewClient = object:WebViewClient(){

                        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                            if (!isReload&&currentUrl.value!=url){
                                if (url != null) {
                                    currentUrl.value = url
                                }
                                if (url!="https://www.pixiv.net/ajax/illust/${id.value}/ugoira_meta"){
                                    if (url != null) {
                                        if (navState.value!=0){
                                            navState.value = 0
                                        } else {
                                            historyPos.value++
                                            (historyPos.value until history.size).forEach{
                                                history.removeAt(it)
                                            }
                                            history.add(url)

                                        }
                                    }
                                }
                            }
                            super.doUpdateVisitedHistory(view, url, isReload)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (id.value!=null&&url=="https://www.pixiv.net/ajax/illust/${id.value}/ugoira_meta"){
                                view?.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByTagName('pre')[0].textContent, ${id.value});")
                                id.value = null
                                if (initId == null){
                                    if (view != null) {
                                        currentUrl.value = history[historyPos.value]
                                        view.loadUrl(currentUrl.value)
                                    }
                                } else {
                                    (context as Activity).finish()
                                }

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
                    .fillMaxHeight()
                    .padding(padding),
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
                            it.loadUrl(initialUrl)
                            progress.value = it.progress
                        })
                }
            }
        }
    }
}

fun goBack(history:MutableList<String>, currentPos:Int, view:WebView){
    view.loadUrl(history[currentPos-1])
}

fun goForward(history:MutableList<String>, currentPos:Int, view:WebView){
    view.loadUrl(history[currentPos+1])
}