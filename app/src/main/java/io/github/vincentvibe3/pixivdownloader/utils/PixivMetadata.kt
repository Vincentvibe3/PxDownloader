package io.github.vincentvibe3.pixivdownloader.utils

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import io.github.vincentvibe3.pixivdownloader.serialization.UgoiraData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

object PixivMetadata {

    val PendingRequests = HashMap<String, String>()

    suspend fun fetchNext(id:String, context: Context){
        withContext(Dispatchers.IO){
            val data = PendingRequests[id]?.let { Json.decodeFromString<UgoiraData>(it) }
            PendingRequests.remove(id)
            if (data != null) {
                Download.getZip(id, data, context)
            }
        }
    }
}