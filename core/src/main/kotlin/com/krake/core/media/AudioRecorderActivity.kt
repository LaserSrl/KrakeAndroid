package com.krake.core.media

import android.Manifest
import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.krake.core.R
import com.krake.core.app.ThemableNavigationActivity
import com.krake.core.permission.PermissionListener
import com.krake.core.permission.PermissionManager
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Created by antoniolig on 03/05/2017.
 */
open class AudioRecorderActivity : ThemableNavigationActivity(),
        View.OnClickListener,
        Handler.Callback,
        MediaPlayer.OnCompletionListener,
        PermissionListener {

    companion object {
        private val TAG = AudioRecorderActivity::class.java.simpleName
        private const val REPEAT_INTERVAL = 40
    }

    private lateinit var handler: Handler
    private lateinit var recordBtn: ImageButton
    private lateinit var playBtn: ImageButton
    private lateinit var deleteBtn: ImageButton
    private lateinit var sendBackBtn: ImageButton
    private lateinit var visualizerView: RecordVisualizerView

    private lateinit var audioUri: Uri

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var descriptor: AssetFileDescriptor? = null

    private lateinit var permissionManager: PermissionManager

    private fun getDescriptor(forced: Boolean): AssetFileDescriptor? {
        if (descriptor == null || forced) {
            try {
                descriptor = contentResolver.openAssetFileDescriptor(audioUri, "rwt")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        return descriptor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(this)
        inflateMainView(R.layout.activity_audio_record, true)

        permissionManager = PermissionManager(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .addListener(this)

        permissionManager.create()

        audioUri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT)

        recordBtn = findViewById(R.id.recordButton)
        playBtn = findViewById(R.id.playButton)
        deleteBtn = findViewById(R.id.deleteRecordButton)
        sendBackBtn = findViewById(R.id.sendBackFileButton)
        visualizerView = findViewById(R.id.visualizerView)

        recordBtn.setOnClickListener(this)
        playBtn.setOnClickListener(this)
        deleteBtn.setOnClickListener(this)
        sendBackBtn.setOnClickListener(this)

        updateUI(false, false)
    }

    override fun onPermissionsHandled(acceptedPermissions: Array<out String>)
    {
        if (acceptedPermissions.contains(Manifest.permission.RECORD_AUDIO))
        {
            // Start the recording
            recorder = MediaRecorder()
            recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            recorder!!.setOutputFile(getDescriptor(false)!!.fileDescriptor)
            recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            visualizerView.clear()
            handler.sendEmptyMessage(0)

            try {
                recorder!!.prepare()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }

            recorder!!.start()
            updateUI(true, false)
        }
    }

    private fun onRecord(start: Boolean) {
        if (start) {
            permissionManager.request()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        player = MediaPlayer()
        try {
            val descriptor = getDescriptor(false)
            player!!.setDataSource(descriptor!!.fileDescriptor, descriptor.startOffset, descriptor.length)
            player!!.prepare()
            player!!.start()
            player!!.setOnCompletionListener(this)
        } catch (e: IOException) {
            Log.e(TAG, "prepare() failed")
        }

        updateUI(false, true)
    }

    private fun stopPlaying() {
        player?.release()
        player = null

        updateUI(false, false)
    }

    private fun stopRecording() {
        handler.removeMessages(0)

        recorder?.let {
            it.stop()
            it.release()
        }

        recorder = null
        updateUI(false, false)
    }

    override fun onPause() {
        super.onPause()
        if (recorder != null) {
            stopRecording()
        }

        if (player != null) {
            stopPlaying()
        }
        updateUI(false, false)
    }

    override fun onClick(v: View) {
        if (v === recordBtn) {
            onRecord(recorder == null)
        } else if (v === playBtn) {
            onPlay(player == null)
        } else if (v === deleteBtn) {
            if (contentResolver.delete(audioUri, null, null) > 0) {
                getDescriptor(true)
                updateUI(false, false)
            }
        } else if (v === sendBackBtn) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun updateUI(recording: Boolean, playing: Boolean) {
        val descriptor = getDescriptor(false)
        val isValid = descriptor != null && descriptor.length > 0

        playBtn.isEnabled = !recording && isValid

        if (playBtn.isEnabled)
            playBtn.imageAlpha = 255
        else
            playBtn.imageAlpha = 150

        playBtn.setImageResource(if (playing) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp)

        deleteBtn.isEnabled = !recording && !playing && isValid
        if (deleteBtn.isEnabled)
            deleteBtn.imageAlpha = 255
        else
            deleteBtn.imageAlpha = 150

        if (recording) {
            recordBtn.setImageResource(R.drawable.ic_stop_24dp)
        } else {
            recordBtn.setImageResource(R.drawable.ic_mic_24dp)
            if (!isValid) {
                sendBackBtn.visibility = View.GONE
                recordBtn.visibility = View.VISIBLE
            } else {
                recordBtn.visibility = View.GONE
                sendBackBtn.visibility = View.VISIBLE
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        if (recorder != null) {
            // get the current amplitude
            val x = recorder!!.maxAmplitude
            visualizerView.addAmplitude(x.toFloat()) // update the VisualizeView
            visualizerView.visibility = View.INVISIBLE
            visualizerView.visibility = View.VISIBLE
            visualizerView.invalidate() // refresh the VisualizerView

            // update in 40 milliseconds
            handler.sendEmptyMessageDelayed(0, REPEAT_INTERVAL.toLong())
        }
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        stopPlaying()
    }
}