package org.deskconn.exoplayervideostreaming

import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log.v
import android.view.OrientationEventListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity(), Player.Listener {

    private val videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var playerView: PlayerView
    var mOrientationListener: OrientationEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get PlayerView by its id
        playerView = findViewById(R.id.playerView)

        mOrientationListener = object : OrientationEventListener(
            this,
            SensorManager.SENSOR_DELAY_NORMAL
        ) {
            override fun onOrientationChanged(orientation: Int) {
                Log.e(
                    "DEBUG_TAG",
                    "Orientation changed to $orientation"
                )

                when (orientation) {
                    in 1..89 -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                    in 180 .. 360 -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                    in 90 .. 180 -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    }
                }
            }
        }

        if ((mOrientationListener as OrientationEventListener).canDetectOrientation()) {
            Log.e("DEBUG_TAG", "Can detect orientation")
            (mOrientationListener as OrientationEventListener).enable()
        } else {
            Log.e("DEBUG_TAG", "Cannot detect orientation")
            (mOrientationListener as OrientationEventListener).disable()
        }

    }

    private fun initPlayer(){
        // Create a player instance.
        exoPlayer = SimpleExoPlayer.Builder(this).build()

        // Bind the player to the view.
        playerView.player = exoPlayer

        //setting exoplayer when it is ready.
        exoPlayer!!.playWhenReady = true

        // Set the media source to be played.
        exoPlayer!!.setMediaSource(buildMediaSource())

        // Prepare the player.
        exoPlayer!!.prepare()
    }

    private fun releasePlayer(){
        if (exoPlayer == null) {
            return
        }
        //release player when done
        exoPlayer!!.release()
        exoPlayer = null
    }

    //creating mediaSource
    private fun buildMediaSource(): MediaSource {
        // Create a data source factory.
        val dataSourceFactory: DefaultHttpDataSource.Factory = DefaultHttpDataSource.Factory()

        // Create a progressive media source pointing to a stream uri.
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl))

        return mediaSource
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || exoPlayer == null){
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mOrientationListener?.disable();
    }

}