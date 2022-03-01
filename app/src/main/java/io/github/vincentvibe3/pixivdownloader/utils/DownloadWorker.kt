package io.github.vincentvibe3.pixivdownloader.utils

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.github.vincentvibe3.pixivdownloader.R
import io.github.vincentvibe3.pixivdownloader.serialization.UgoiraData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DownloadWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    override fun onStopped() {
        val id = inputData.getString("id")
        if (id != null) {
            Download.cleanup(applicationContext, id)
        }
        val sharedprefs = applicationContext.getSharedPreferences("dlReq", Context.MODE_PRIVATE)
        sharedprefs.edit()
            .remove(id)
            .apply()
        super.onStopped()
    }

    override fun doWork(): Result {
        val context = applicationContext
        val sharedprefs = context.getSharedPreferences("dlReq", Context.MODE_PRIVATE)
        val id = inputData.getString("id")
        val json = sharedprefs.getString(id, null)
        if (json?.contains("\"error\":true") == false) {
            val data = json.let { Json.decodeFromString<UgoiraData>(it) }
            if (id != null) {
                var failureNotif = NotificationCompat.Builder(context, "Downloads")
                    .setSmallIcon(R.drawable.download_icon)
                    .setContentTitle("Failed")
                    .setContentText("Failed to download $id")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                if (data.error) {
                    with(NotificationManagerCompat.from(context)) {
                        notify(-id.toInt(), failureNotif.build())
                    }
                    return Result.failure()
                }

                runBlocking {
                    launch {
                        Download.getZip(id, data, context, failureNotif)
                    }.join()
                }
                sharedprefs.edit()
                    .remove(id)
                    .apply()
                return Result.success()
            }
        }
        val failureNotif = NotificationCompat.Builder(context, "Downloads")
            .setSmallIcon(R.drawable.download_icon)
            .setContentTitle("Failed")
            .setContentText("Failed to download $id")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (id != null) {
                notify(-id.toInt(), failureNotif.build())
            }
        }
        sharedprefs.edit()
            .remove(id)
            .apply()
        // Indicate whether the work finished successfully with the Result
        return Result.failure()
    }
}
