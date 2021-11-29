package io.github.vincentvibe3.pixivdownloader.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

object Download {

    val headers:HashMap<String, String> = hashMapOf(
        "Referer" to  "https://www.pixiv.net/",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language" to "en-CA,en-US;q=0.7,en;q=0.3",
        "Accept-Encoding" to "gzip, deflate, br",
        "DNT" to "1",
        "Connection" to "keep-alive",
        "Upgrade-Insecure-Requests" to "1",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "none",
        "Sec-Fetch-User" to "?1",
        "Sec-GPC" to "1",
        "Cache-Control" to "max-age=0",
        "TE" to "trailers"
    )

    suspend fun download(id:String, queue: RequestQueue, context: Context){
        val ugoiraMetadata = PixivMetadata.getUgoiraData(id, queue)
        if (ugoiraMetadata!=null){
            val jsonData = JSONObject(ugoiraMetadata)
            val error = jsonData.getBoolean("error")
            println(error)
            if (!error){
                val body = jsonData.getJSONObject("body")
                val imagesZip = body.getString("originalSrc")
                getZip(imagesZip, id, context)
            }
        }
    }

    @SuppressLint("Range")
    private fun getZip(url:String, id: String, context: Context){
        println(url)
        val downloadRequest = DownloadManager.Request(Uri.parse(url))
        for (header in headers.keys){
            downloadRequest.addRequestHeader(header, headers[header])
            println(header)
        }
        var cookies = CookieManager.getInstance().getCookie("https://www.pixiv.net")
        cookies = cookies ?: ""
        println("$cookies download")
        downloadRequest.addRequestHeader("Cookie", cookies)
            .setTitle("Pixiv $id")
            .setDescription("Downloading $id")
            .setNotificationVisibility(0)
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$id.zip")
        val dlManager = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
        val dlId = dlManager.enqueue(downloadRequest)
        val query = DownloadManager.Query().setFilterById(dlId)
        val cursor = dlManager.query(query)
        cursor.moveToFirst()
        println(dlId)
        println(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)))


    }

}