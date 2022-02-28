package io.github.vincentvibe3.pixivdownloader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import io.github.vincentvibe3.pixivdownloader.R
import io.github.vincentvibe3.pixivdownloader.serialization.UgoiraData
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream


object Download {

    val client = OkHttpClient()

    val pendingDownloads = HashMap<Long, String>()

    val headers:HashMap<String, String> = hashMapOf(
            "Host" to "i.pximg.net",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:97.0) Gecko/20100101 Firefox/97.0",
            "Accept" to "*/*",
            "Accept-Language" to "en-US,en;q=0.5",
            "Accept-Encoding" to "gzip, deflate, br",
            "Referer" to "https://www.pixiv.net/",
            "Origin" to "https://www.pixiv.net",
            "DNT" to "1",
            "Connection" to "keep-alive",
            "Sec-Fetch-Dest" to "empty",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Site" to "cross-site",
            "Sec-GPC" to "1",
            "TE" to "trailers",
    )

    fun getZip(id: String, data:UgoiraData, context: Context){
        val url = data.body.src
        val headerBuilder = Headers.Builder()
        for (header in headers){
            headerBuilder.add(header.key, header.value)
        }
        val cookie = CookieManager.getInstance().getCookie("https://www.pixiv.net")
        if (cookie!=null){
            headerBuilder.add("Cookie", cookie)
        }
        println(cookie)
        println(url)
        val dlHeaders = headerBuilder.build()
        val request = Request.Builder()
            .url(url)
            .headers(dlHeaders)
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("Download Failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    val bytes = response.body?.byteStream()
                    val file = File("${context.cacheDir.absolutePath}/$id.zip")
                    val outputStream = FileOutputStream(file)
                    val byteArray = ByteArray(1024)
                    if (bytes != null) {
                        while (true){
                            val read = bytes.read(byteArray)
                            if (read!=-1){
                                outputStream.write(byteArray, 0, read);
                            } else {
                                break
                            }
                        }
                    }
                    var builder = NotificationCompat.Builder(context, "Downloads")
                        .setSmallIcon(R.drawable.download_icon)
                        .setContentTitle("Download")
                        .setContentText("Download Complete")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    with(NotificationManagerCompat.from(context)) {
                        notify(id.toInt(), builder.build())
                    }
                    unzip(file, id, context)
                    VideoGenerator().generate("${context.cacheDir.absolutePath}/$id", data)

                }

            }

        })
    }

    fun unzip(file:File, id:String, context: Context){
        val target = "${context.cacheDir.absolutePath}/$id"
        File(target).mkdir()
        val zis = ZipInputStream(FileInputStream(file))
        var next = zis.nextEntry
        val buffer = ByteArray(1024)
        while (next!=null){
            val dest = File("$target/${next.name}")
            dest.delete()
            val out = FileOutputStream(dest)
            while (zis.read(buffer) > 0){
                out.write(buffer, 0, buffer.size)
            }
            out.close()
            next = zis.nextEntry
        }
        zis.closeEntry()
        zis.close()
        file.delete()
    }

}