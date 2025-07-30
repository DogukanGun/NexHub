package com.dag.nexwallet.base.components

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class AudioManager(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    
    suspend fun playManifestMusic() {
        withContext(Dispatchers.IO) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    // Load the manifest.mp3 file from raw resources
                    setDataSource(context,
                        "android.resource://${context.packageName}/raw/manifest".toUri()
                    )
                    prepare()
                    start()
                    this@AudioManager.isPlaying = true
                }
            } catch (e: Exception) {
                // Handle any errors
                isPlaying = false
            }
        }
    }
    
    fun stopMusic() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
        isPlaying = false
    }
    
    fun isMusicPlaying(): Boolean = isPlaying
    
    fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
    }
    
    fun resumeMusic() {
        mediaPlayer?.start()
        isPlaying = true
    }
} 