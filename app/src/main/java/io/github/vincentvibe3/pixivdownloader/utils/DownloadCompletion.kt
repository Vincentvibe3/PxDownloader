package io.github.vincentvibe3.pixivdownloader.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import java.io.File

class DownloadCompletion():BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id in Download.pendingDownloads.keys){
                val file = File("${Environment.DIRECTORY_DOWNLOADS}/${Download.pendingDownloads[id]}.zip")
                Download.pendingDownloads[id]?.let { Download.unzip(file, it) }
            }
        }
    }
}