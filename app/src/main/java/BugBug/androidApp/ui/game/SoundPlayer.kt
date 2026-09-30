package BugBug.androidApp.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import BugBug.androidApp.R

class SoundPlayer(context: Context) {

    private val appContext = context.applicationContext

    // --- SFX ---
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

    private var musicPlayer: MediaPlayer? = null
    private var musicVolume: Float = 0.5f

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
        screamId = soundPool.load(appContext, R.raw.scream2, 1)
    }

    fun playScream() {
        if (!isLoaded) {
            pendingPlay = true
            return
        }
        soundPool.play(screamId, 1f, 1f, 1, 0, 1f)
    }

    fun startBackgroundMusic(volume: Float = musicVolume) {
        musicVolume = volume
        if (musicPlayer?.isPlaying == true) return
        musicPlayer?.release()
        musicPlayer = try {
            MediaPlayer.create(appContext, R.raw.fight)?.apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun pauseBackgroundMusic() {
        musicPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun resumeBackgroundMusic() {
        musicPlayer?.let { if (!it.isPlaying) it.start() }
    }

    fun stopBackgroundMusic() {
        musicPlayer?.let {
            try { if (it.isPlaying) it.stop() } catch (_: IllegalStateException) {}
            it.release()
        }
        musicPlayer = null
    }

    fun release() {
        stopBackgroundMusic()
        soundPool.release()
    }
}