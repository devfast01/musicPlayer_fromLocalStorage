package com.example.spotifyclone_example.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.spotifyclone_example.PlayerActivity
import com.example.spotifyclone_example.PlayerActivity.Companion.isPlaying
import com.example.spotifyclone_example.PlayerActivity.Companion.musicService
import com.example.spotifyclone_example.PlayerActivity.Companion.nowPlayingID
import com.example.spotifyclone_example.PlayerActivity.Companion.runnable
import com.example.spotifyclone_example.PlayerActivity.Companion.songListPA
import com.example.spotifyclone_example.PlayerActivity.Companion.songPosition
import com.example.spotifyclone_example.R
import com.example.spotifyclone_example.databinding.FragmentNowPlayingBinding
import com.example.spotifyclone_example.models.setImgArt
import com.example.spotifyclone_example.models.setSongPosition

class fragment_now_playing : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var bindingNow: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        bindingNow = FragmentNowPlayingBinding.bind(view)
        bindingNow.root.visibility = View.GONE

        //click on now playing layout
        bindingNow.root.setOnClickListener {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra("INDEX", songPosition)
            i.putExtra("CLASS", "NowPlaying")
            startActivity(i)
        }

        //play pause button
        bindingNow.imgPlayPauseNP.setOnClickListener {
            if (isPlaying)
                pauseSong()
            else
                playSong()
        }

        //next button
        bindingNow.imgNextNP.setOnClickListener {
            changeSong(nextSong = true)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (musicService != null) {
            bindingNow.root.visibility = View.VISIBLE

            setLayoutNP()
        }

    }


    // set layout of Now Playing
    private fun setLayoutNP() {

        //song image
        setImgArt(
            requireContext(), songListPA[songPosition].path,
            bindingNow.imgCurrentSongNP
        )

        //song title
        bindingNow.tvSongTitleNP.text =
            songListPA[songPosition].title

        //moving title
        bindingNow.tvSongTitleNP.isSelected = true

        //set play pause icon
        if (isPlaying)
            bindingNow.imgPlayPauseNP.setImageResource(R.drawable.ic_pause)
        else
            bindingNow.imgPlayPauseNP.setImageResource(R.drawable.ic_play)

        //set seekbar
        bindingNow.seekBarNP.progress = 0
        bindingNow.seekBarNP.max = musicService!!.mediaPlayer!!.duration
        setSeekBarNP()

    }

    //set seekbar position
    private fun setSeekBarNP() {

        runnable = Runnable {

            bindingNow.seekBarNP.progress = musicService!!.mediaPlayer!!.currentPosition

            //increment seekbar with song position
            Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
        }

        // start runnable after 0 millisecond
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    //--------------song related functions-------------------
    //play song
    private fun playSong() {
        bindingNow.imgPlayPauseNP.setImageResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()

        //moving title
        bindingNow.tvSongTitleNP.isSelected = true
    }

    //pause song
    private fun pauseSong() {
        bindingNow.imgPlayPauseNP.setImageResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()

        //moving title
        bindingNow.tvSongTitleNP.isSelected = false
    }

    //change song
    private fun changeSong(nextSong: Boolean) {
        setSongPosition(nextSong)

        //setup music player
        if (musicService!!.mediaPlayer == null)
            musicService!!.mediaPlayer = MediaPlayer()

        musicService!!.mediaPlayer!!.reset()
        musicService!!.mediaPlayer!!.setDataSource(songListPA[songPosition].path)
        musicService!!.mediaPlayer!!.prepare()

        //set song id
        nowPlayingID = songListPA[songPosition].id

        //set layout
        setLayoutNP()

        //play song
        playSong()
    }
}