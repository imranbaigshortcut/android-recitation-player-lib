package com.ibmst.recitation.player

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.ConnectivityManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.ibmst.recitation.model.Page
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap

class AudioStateMachine(val context: Context, val listener: PlayerListener) :
    OnCompletionListener,
    OnPreparedListener,
    MediaPlayer.OnErrorListener,
    OnBufferingUpdateListener {
    private var mMediaPlayer: MediaPlayer = MediaPlayer()
    private val jobsMap = HashMap<Long, Int>()
    private var jobsDownloaded = 0
    private var currentAyat = 0
    private var receiver: BroadcastReceiver
    var currentState: State = Idle()
    private lateinit var page: Page
    private var audioFiles = arrayListOf<String>()
    private val downloader = Downloader.createDownloader(context)

    init {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setOnCompletionListener(this)
        mMediaPlayer.setOnErrorListener(this)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    val downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID,
                        0,
                    )
                    if (downloader.isDownloaded(downloadId)) {
                        downloaded(downloadId)
                    }
                }
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE,
            ),
        )
    }

    fun play(page: Page) {
        this.page = page
        currentState.play(page)
    }

    fun pause() {
        currentState.pause()
    }

    fun stop(audioPageCompleted: Boolean) {
        currentState.stop(audioPageCompleted)
    }

    fun downloaded(id: Long) {
        jobsDownloaded++
        val bismillah = page.isBismillahPage
        if (jobsDownloaded == page.ayatList.size + (if (bismillah) 1 else 0)) {
            allDownloaded()
        }
        currentState.downloaded(id)
    }

    fun allDownloaded() {
        currentState.allDownloaded()
    }

    fun playbackCompleted() {
        currentState.playbackCompleted()
    }

    fun pageChange(page: Page) {
        this.page = page
        currentState.pageChange(page)
    }

    fun resume(thisPage: Page) {
        if (page != null && thisPage.pageNo == page!!.pageNo) {
            if (currentState is Paused) {
                currentState.play(page!!)

                // AppEventBus.getBus().post(new PlayingEvent(page.getPageNo(), currentAyat));
            }
        }
    }

    inner class Idle : State() {
        override fun play(page: Page) {
            val isBismillah = page.isBismillahPage
            jobsMap.clear()
            jobsDownloaded = 0
            val ayats = page.ayatList
            audioFiles = ArrayList()
            if (isBismillah) {
                audioFiles.add("001001.mp3")
            }
            for (i in ayats.indices) {
                var file = String.format("%03d%03d.mp3", page.suraNo, ayats[i].number)
                if (page.pageNo == 1) {
                    file = String.format("%03d%03d.mp3", page.suraNo, ayats[i].number + 1)
                }
                audioFiles.add(file)

                // audioFiles.add(ayats.get(i).getAudioTalawat());
            }
            for (i in audioFiles.indices) {
                val audio = audioFiles.get(i)
                if (downloader.doesFileExist(audio)) {
                    this@AudioStateMachine.downloaded(i.toLong())
                } else {
                    if (!isNetworkAvailable) {
                        val toast = Toast.makeText(context, "No internet", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        return
                    }
                    val jobId = downloader.downloadFile(audio)
                    if (jobId != 0L) {
                        if (jobsMap.containsKey(jobId)) {
                            jobsMap.remove(jobId)
                        }
                        jobsMap[jobId] = i
                    }
                }
            }
        }

        override fun allDownloaded() {
            moveToState(Playing())
        }
    }

    inner class Playing : State {
        val autoStart: Boolean
        val startAyat: Int

        constructor(startAyat: Int) {
            this.startAyat = startAyat
            autoStart = true
        }

        constructor(autoStart: Boolean) {
            startAyat = 0
            this.autoStart = autoStart
        }

        constructor() {
            startAyat = 0
            autoStart = true
        }

        var needToReset = false
        override fun start() {
            if (autoStart) {
                try {
                    currentAyat = startAyat
                    prepare()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    onError()
                }
            }
        }

        @Throws(IOException::class)
        private fun prepare() {
            if (audioFiles!!.size > currentAyat) {
                if (needToReset) {
                    mMediaPlayer.reset()
                }
                val audio = audioFiles!![currentAyat]
                mMediaPlayer.setDataSource(downloader.getFile(audio).path)
                listener.showProgress(true)
                mMediaPlayer.prepareAsync()
                needToReset = true
                if (page!!.isBismillahPage) {
                    listener.playing(page, currentAyat)
                } else {
                    listener.playing(page, currentAyat)
                }
            } else {
                this@AudioStateMachine.stop(true)
            }
        }

        override fun onPrepared(mp: MediaPlayer?) {
            mMediaPlayer.start()
            moveToState(Playing(false))
        }

        override fun onCompletion(mp: MediaPlayer?) {
            super.onCompletion(mp)
            mMediaPlayer.reset()
            currentAyat++
            try {
                prepare()
            } catch (ex: Exception) {
                ex.printStackTrace()
                onError()
            }
        }

        override fun stop(audioPageCompleted: Boolean) {
            try {
                mMediaPlayer.stop()
                mMediaPlayer.reset()
                moveToState(Idle())
                if (audioPageCompleted) {
                    // AppEventBus.getBus().post(new RecitationPageCompletedEvent(page.getPageNo()));
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun pause() {
            try {
                mMediaPlayer.pause()
                moveToState(Paused())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun pageChange(page: Page?) {
            try {
                mMediaPlayer.stop()
                mMediaPlayer.reset()
                moveToState(Idle())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    inner class Paused : State() {
        override fun play(page: Page) {
            mMediaPlayer.start()
            moveToState(Playing(currentAyat))
        }

        override fun stop(audioPageCompleted: Boolean) {
            try {
                mMediaPlayer.stop()
                mMediaPlayer.reset()
                moveToState(Idle())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    open inner class State {
        open fun start() {}
        open fun play(page: Page) {}
        open fun pause() {}
        open fun stop(audioCompletedStop: Boolean) {}
        fun downloaded(id: Long) {}
        fun playbackCompleted() {}
        open fun pageChange(page: Page?) {}
        open fun allDownloaded() {}
        fun onError() {}
        open fun onPrepared(mp: MediaPlayer?) {}
        open fun onCompletion(mp: MediaPlayer?) {}
    }

    interface PlayerListener {
        fun onStateChange(old: State?, newState: State?)
        fun onBufferingUpdate(percent: Int)
        fun showProgress(showBusy: Boolean)
        fun playing(page: Page?, ayat: Int)
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        listener.onBufferingUpdate(percent)
    }

    override fun onCompletion(mp: MediaPlayer) {
        currentState.onCompletion(mp)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        listener.showProgress(false)
        currentState.onError()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        listener.showProgress(false)
        currentState.onPrepared(mp)
    }

    fun release() {
        mMediaPlayer.release()
        context.unregisterReceiver(receiver)
    }

    fun shutdown() {
        context.unregisterReceiver(receiver)
    }

    fun moveToState(state: State) {
        val old = currentState
        currentState = state
        Log.d("AudioPlayer", "State :" + currentState.javaClass.canonicalName)
        listener.onStateChange(old, currentState)
        currentState.start()
    }

    private val isNetworkAvailable: Boolean
        private get() {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
}
