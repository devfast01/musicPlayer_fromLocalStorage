package com.example.spotifyclone_example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.spotifyclone_example.ApplicationClass
import com.example.spotifyclone_example.PlayerActivity
import com.example.spotifyclone_example.PlayerActivity.Companion.favIndex
import com.example.spotifyclone_example.PlayerActivity.Companion.imgFavoritePA
import com.example.spotifyclone_example.PlayerActivity.Companion.imgSongPA
import com.example.spotifyclone_example.PlayerActivity.Companion.isFavorite
import com.example.spotifyclone_example.PlayerActivity.Companion.musicService
import com.example.spotifyclone_example.PlayerActivity.Companion.songListPA
import com.example.spotifyclone_example.PlayerActivity.Companion.songPosition
import com.example.spotifyclone_example.PlayerActivity.Companion.tvArtistPA
import com.example.spotifyclone_example.PlayerActivity.Companion.tvTitlePA
import com.example.spotifyclone_example.R
import com.example.spotifyclone_example.fragments.fragment_now_playing
import com.example.spotifyclone_example.models.checkFavorite
import com.example.spotifyclone_example.models.setImgArt
import com.example.spotifyclone_example.models.setSongPosition
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> {
                changeSong(nextSong = false, context = context!!)
            }

            ApplicationClass.PLAY -> {
                if (PlayerActivity.isPlaying)
                    pauseMusic()
                else
                    playMusic()
            }
            ApplicationClass.NEXT -> {
                changeSong(nextSong = true, context = context!!)
            }

            ApplicationClass.EXIT -> {
                musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
                musicService!!.stopForeground(true)
                musicService!!.mediaPlayer!!.release()
                musicService = null
                exitProcess(1)
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.ic_pause)
        PlayerActivity.fabPlayPausePA.setImageResource(R.drawable.ic_pause)
        fragment_now_playing.bindingNow.imgPlayPauseNP.setImageResource(R.drawable.ic_pause)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_play)
        PlayerActivity.fabPlayPausePA.setImageResource(R.drawable.ic_play)
        fragment_now_playing.bindingNow.imgPlayPauseNP.setImageResource(R.drawable.ic_play)
    }

    private fun changeSong(nextSong: Boolean, context: Context) {
        setSongPosition(nextSong)

        musicService!!.mediaPlayer!!.reset()
        musicService!!.mediaPlayer!!.setDataSource(songListPA[songPosition].path)
        musicService!!.mediaPlayer!!.prepare()

        tvTitlePA.text = songListPA[songPosition].title
        tvArtistPA.text = songListPA[songPosition].artist

        //set image
        setImgArt(context, songListPA[songPosition].path, imgSongPA)

        //set fav index
        favIndex = checkFavorite(songListPA[songPosition].id)

        //set fav icon
        if (isFavorite)
            imgFavoritePA.setImageResource(R.drawable.ic_baseline_favorite)
        else
            imgFavoritePA.setImageResource(R.drawable.ic_favorite)


        playMusic()
    }


}