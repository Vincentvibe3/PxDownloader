package io.github.vincentvibe3.pixivdownloader.utils

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.webkit.CookieManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.vincentvibe3.pixivdownloader.R
import io.github.vincentvibe3.pixivdownloader.serialization.UgoiraData
import kotlinx.coroutines.delay
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream


object Download {

    val client = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
        .build()

    var working = HashMap<String, Boolean>()

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

    suspend fun getZip(
        id: String,
        data: UgoiraData,
        context: Context,
        failureNotif: NotificationCompat.Builder
    ){
        working[id] = true
        var downloadNotif = NotificationCompat.Builder(context, "Downloads")
            .setSmallIcon(R.drawable.download_icon)
            .setContentTitle("Download")
            .setContentText("Downloading $id.zip")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        with(NotificationManagerCompat.from(context)) {
            notify(id.toInt(), downloadNotif.build())
        }
        val url = data.body!!.originalSrc
        val headerBuilder = Headers.Builder()
        for (header in headers){
            headerBuilder.add(header.key, header.value)
        }
        val cookie = CookieManager.getInstance().getCookie("https://www.pixiv.net")
        if (cookie!=null){
            headerBuilder.add("Cookie", cookie)
        }
        val dlHeaders = headerBuilder.build()
        val request = Request.Builder()
            .url(url)
            .headers(dlHeaders)
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                with(NotificationManagerCompat.from(context)) {
                    cancel(id.toInt())
                }
                with(NotificationManagerCompat.from(context)) {
                    cancel(id.toInt())
                    notify(-id.toInt(), failureNotif.build())
                }
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
                                outputStream.write(byteArray, 0, read)
                            } else {
                                break
                            }
                        }
                    }
                    unzip(file, id, context)

                    val success = VideoGenerator().generate("${context.cacheDir.absolutePath}/$id", data, "${context.filesDir}", id, context)

                    with(NotificationManagerCompat.from(context)) {
                        cancel(id.toInt())
                    }
                    if (success){
                        saveFileUsingMediaStore(context, "${context.filesDir}/$id.webm","$id.webm")
                        var completedNotif = NotificationCompat.Builder(context, "Downloads")
                            .setSmallIcon(R.drawable.download_icon)
                            .setContentTitle("Download")
                            .setContentText("File saved to downloads as $id.webm")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        with(NotificationManagerCompat.from(context)) {
                            notify(-id.toInt(), completedNotif.build())
                        }
                    } else {
                        with(NotificationManagerCompat.from(context)) {
                            notify(-id.toInt(), failureNotif.build())
                        }
                    }
                    cleanup(context, id)


                } else {
                    with(NotificationManagerCompat.from(context)) {
                        cancel(id.toInt())
                        notify(-id.toInt(), failureNotif.build())
                    }
                }
                working[id] = false
            }
        })
        while (working[id] == true){
            delay(1000L)
        }
        working.remove(id)
    }

    fun cleanup(context: Context, id:String){
        File("${context.cacheDir.absolutePath}/$id.zip").delete()
        File("${context.cacheDir.absolutePath}/$id").deleteRecursively()
        File("${context.filesDir}/$id.webm").delete()
    }

    private fun saveFileUsingMediaStore(context: Context, path: String, fileName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/webm")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            File(path).inputStream().use { input ->
                resolver.openOutputStream(uri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
        }
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
    }


}