package BugBug.androidApp.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import BugBug.androidApp.R

class SoundPlayer(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var screamId: Int = 0
    private var isLoaded: Boolean = false
    private var pendingPlay: Boolean = false

    init {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
                if (pendingPlay) {
                    playScream()
                    pendingPlay = false
                }
            }
        }
        screamId = soundPool.load(context, R.raw.scream2, 1)
    }

    fun playScream() {
        if (!isLoaded) {
            pendingPlay = true
            return
        }
        soundPool.play(screamId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}