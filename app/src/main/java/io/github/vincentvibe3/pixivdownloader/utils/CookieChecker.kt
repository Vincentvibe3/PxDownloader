package io.github.vincentvibe3.pixivdownloader.utils

import android.webkit.CookieManager
import io.github.vincentvibe3.pixivdownloader.AppViewModel
import okhttp3.*
import okio.IOException

fun checkCookies(viewModel: AppViewModel) {
    val cookies = CookieManager.getInstance().getCookie("https://www.pixiv.net")
    if (cookies==null){
        viewModel.loginStatus.postValue(false)
    } else {
        val request = Request.Builder()
            .url("https://www.pixiv.net/en/")
            .addHeader("Cookie", cookies)
            .build()
        Download.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = response.body?.string()
                    val ok = body?.contains("var dataLayer = [{login: 'no'")?.not() ?: false
                    viewModel.loginStatus.postValue(ok)
                }
            }
        })
    }
}

fun checkAppleCookie():Boolean{
    val cookies = CookieManager.getInstance().getCookie(".apple.com")
    return cookies.isNullOrEmpty().not()
}

fun checkTwitterCookie():Boolean{
    val cookies = CookieManager.getInstance().getCookie(".twitter.com")
    return cookies.isNullOrEmpty().not()
}

fun checkGoogleCookie():Boolean{
    val cookies = CookieManager.getInstance().getCookie("accounts.google.com")
    return cookies.isNullOrEmpty().not()
}