package com.example.playback

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.Futures

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    companion object {
        fun startMediaService(context: Context) {
            val intent = Intent(context, PlaybackService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore service starts that fail due to active apps restrictions
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = PlaybackManager.getPlayer(this)
        
        // Define Custom Commands for Prev/Next if required or use standard media actions
        val sessionCallback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                // Accept all connections and enable typical media keys
                return MediaSession.ConnectionResult.accept(
                    SessionCommand.COMMAND_CODE_PLAY_PAUSE.let { 
                        MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS 
                    },
                    MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                )
            }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.let {
            it.player.release()
            it.release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
