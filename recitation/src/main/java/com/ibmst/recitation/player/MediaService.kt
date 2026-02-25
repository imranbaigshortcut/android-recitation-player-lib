package com.ibmst.recitation.player

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.ibmst.recitation.model.Page
import com.ibmst.recitation.player.AudioStateMachine.* // ktlint-disable no-wildcard-imports

class MediaService : Service(), PlayerListener {
    private var mAudioMachine: AudioStateMachine = AudioStateMachine(this, this)

    fun setPlayerListener(playerListener: PlayerListener?) {
        mPlayerListener = playerListener
    }

    var mPlayerListener: PlayerListener? = null
    private val mBinder: IBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    val isPlaying: Boolean
        get() = mAudioMachine.currentState is Playing

    val isIdle: Boolean
        get() = mAudioMachine.currentState is Idle

    inner class LocalBinder : Binder() {
        val service: MediaService
            get() = this@MediaService
    }

    fun pageChange(page: Page) {
        mAudioMachine.pageChange(page)
    }

    override fun onStateChange(old: AudioStateMachine.State?, newState: AudioStateMachine.State?) {
        mPlayerListener?.onStateChange(old, newState)
    }

    override fun onBufferingUpdate(percent: Int) {
        mPlayerListener?.onBufferingUpdate(percent)
    }

    override fun showProgress(showBusy: Boolean) {
        mPlayerListener?.showProgress(showBusy)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioMachine.release()
    }

    fun play(page: Page?) {
        page ?: return
        mAudioMachine.play(page)
    }

    fun resume(page: Page?) {
        page ?: return
        mAudioMachine.resume(page)
    }

    fun pause() {
        mAudioMachine.pause()
    }

    fun stop() {
        mAudioMachine.stop(false)
    }

    override fun playing(page: Page?, ayat: Int) {
        mPlayerListener?.playing(page, ayat)
    }
}
