package io.github.vincentvibe3.pixivdownloader.utils

import com.android.volley.RequestQueue
import org.json.JSONObject

object Download {

    suspend fun download(id:String, queue: RequestQueue){
        val ugoiraMetadata = PixivMetadata.getUgoiraData(id, queue)
        if (ugoiraMetadata!=null){
            val jsonData = JSONObject(ugoiraMetadata)
            val error = jsonData.getBoolean("error")
            if (!error){
                jsonData.getJSONArray()
            }
        }
    }

}