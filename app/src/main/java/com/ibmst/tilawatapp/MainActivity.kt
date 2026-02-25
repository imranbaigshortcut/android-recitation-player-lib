package com.ibmst.tilawatapp

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.ibmst.recitation.model.Page
import com.ibmst.recitation.player.AudioStateMachine
import com.ibmst.recitation.player.MediaService
import com.ibmst.tilawatapp.databinding.ActivityMainBinding
import com.ibmst.tilawatapp.utils.Assets

class MainActivity : AppCompatActivity(), AudioStateMachine.PlayerListener {

    lateinit var binding: ActivityMainBinding
    var mBound = false
    var mService: MediaService? = null
    var mMyServiceConnection: MyServiceConnection? = null
    var audioPaused = false

    var currentPage: Page? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.play.setOnClickListener {
            Assets.loadPageAsync(this@MainActivity, 1) {
                currentPage = it
            }
        }
    }

    inner class MyServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MediaService.LocalBinder
            mService = binder.service
            mService?.setPlayerListener(this@MainActivity)
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound = false
        }
    }

    override fun onStateChange(old: AudioStateMachine.State?, newState: AudioStateMachine.State?) {
    }

    override fun onBufferingUpdate(percent: Int) {
    }

    override fun showProgress(showBusy: Boolean) {
    }

    override fun playing(page: Page?, ayat: Int) {
    }
}
