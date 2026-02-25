package com.ibmst.recitation.player

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.lang.IllegalStateException
import kotlin.Throws

class Downloader(private val downloadManager: DownloadManager, private val context: Context) {
    // private final String URL_PREFIX = "http://leskoranen.no/public/admin_assets/audios/";
    // private final String URL_PREFIX = "http://tanzil.net/res/audio/afasy/";
    private val appUrlPrefix = "https://everyayah.com/data/Alafasy_64kbps/"
    private val EXTERNAL_STORANGE = Environment.DIRECTORY_DOWNLOADS + "/"

    @Throws(IllegalStateException::class)
    fun downloadFile(fileName: String): Long {
        val link = getFileURL(fileName)
        val request = DownloadManager.Request(
            Uri.parse(link),
        )
        request.setDescription("Downloading... $fileName")
        request.setTitle(fileName)
        return try {
            val file = File(context.getExternalFilesDir(EXTERNAL_STORANGE), fileName)
            request.setDestinationUri(Uri.fromFile(file))
            downloadManager.enqueue(request)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            throw e
        }
    }

    fun isDownloaded(id: Long): Boolean {
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndex = cursor
                .getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                return true
            }
        }
        return false
    }

    fun getFileURL(fillname: String): String {
        return appUrlPrefix + fillname
    }

    fun doesFileExist(fileName: String?): Boolean {
        val file = File(context.getExternalFilesDir(EXTERNAL_STORANGE), fileName)
        return file.exists()
    }

    fun deleteFile(fileName: String?) {
        if (doesFileExist(fileName)) {
            val file = File(context.getExternalFilesDir(EXTERNAL_STORANGE), fileName)
            file.delete()
        }
    }

    fun getFile(fileName: String?): File {
        return File(context.getExternalFilesDir(EXTERNAL_STORANGE), fileName)
    }

    companion object {
        fun createDownloader(context: Context): Downloader {
            return Downloader(
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager,
                context,
            )
        }
    }
}
