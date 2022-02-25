package io.github.vincentvibe3.pixivdownloader.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

object PixivMetadata {

    val dataPool = ConcurrentHashMap<String, Pair<Boolean, String?>>()

    suspend fun getUgoiraData(id:String, queue:RequestQueue): String? {
        val request = StringRequest(Request.Method.GET,
            "https://www.pixiv.net/ajax/illust/$id/ugoira_meta",
            {
                dataPool[id] = Pair(true, it)
            },
            {
                dataPool[id] = Pair(false, it.message)
            }
        )
        queue.add(request)
        while (!dataPool.containsKey(id)){
            delay(10L)
        }
        println(dataPool[id]?.second)
        return dataPool[id]?.second
    }


}