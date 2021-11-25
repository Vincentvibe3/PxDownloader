package io.github.vincentvibe3.pixivdownloader.utils

import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.runtime.MutableState

class CustomChromeClient(private val progress: MutableState<Int>):WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progress.value = newProgress
        println(progress.value)
    }

}