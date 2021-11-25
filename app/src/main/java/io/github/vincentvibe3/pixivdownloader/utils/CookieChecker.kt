package io.github.vincentvibe3.pixivdownloader.utils

import android.webkit.CookieManager

fun checkCookies(): Boolean {
    val cookies = CookieManager.getInstance().getCookie("https://www.pixiv.net")
    return if (cookies==null){
        false
    } else {
        println(cookies)
        cookies.contains("PHPSESSID=")&&cookies.contains("first_visit_datetime")
    }
}