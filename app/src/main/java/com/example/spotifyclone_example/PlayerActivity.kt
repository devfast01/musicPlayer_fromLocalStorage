package com.example.spotifyclone_example

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.spotifyclone_example.fragments.fragment_now_playing.Companion.bindingNow
import android.media.audiofx.AudioEffect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.spotifyclone_example.databinding.ActivityPlayerBinding
import com.example.spotifyclone_example.fragments.fragment_favorite
import com.example.spotifyclone_example.fragments.fragment_music
import com.example.spotifyclone_example.models.Song
import com.example.spotifyclone_example.models.checkFavorite
import com.example.spotifyclone_example.models.formatDuration
import com.example.spotifyclone_example.models.setBlurImgArt
import com.example.spotifyclone_example.models.setImgArt
import com.example.spotifyclone_example.models.setSongPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton


@Suppress("DEPRECATION")
class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        var musicService: MusicService? = null
        lateinit var runnable: Runnable

        //song data
        lateinit var songListPA: ArrayList<Song>
        var songPosition: Int = 0
        var nowPlayingID: String = ""

        //player behaviour
        var isPlaying: Boolean = false
        var isRepeat: Boolean = false
        var isFavorite: Boolean = false
        var favIndex: Int = -1

        //layout elements
        lateinit var fabPlayPausePA: FloatingActionButton

        @SuppressLint("StaticFieldLeak")
        lateinit var tvTitlePA: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var tvArtistPA: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var imgSongPA: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var imgFavoritePA: ImageView
    }
    private lateinit var binding:ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init layout elements,songListPA array and set layout
        initialize()

        //---------------------------All Buttons-------------------------
        //equalizer button
        binding.imgEqualizer.setOnClickListener {
            openEqualizer()
        }

        //play pause button
        binding.fabPlayPause.setOnClickListener {
            if (isPlaying)
                pauseSong()
            else
                playSong()
        }

        //prev button
        binding.imgPrevious.setOnClickListener {
            changeSong(nextSong = false)
        }

        //next button
        binding.imgNext.setOnClickListener {
            changeSong(nextSong = true)
        }

        //repeat button
        binding.imgRepeat.setOnClickListener {
            if (!isRepeat) {
                isRepeat = true
                binding.imgRepeat.setColorFilter(ContextCompat.getColor(this, R.color.colorAccentDark))
            } else {
                isRepeat = false
                binding.imgRepeat.setColorFilter(ContextCompat.getColor(this, R.color.text))
            }
        }

        //shuffle button
        binding.imgShuffle.setOnClickListener {
            Toast.makeText(this, "Implementing soon", Toast.LENGTH_SHORT).show()
        }

        //back button
        binding.imgBack.setOnClickListener {
            finish()
        }

        //fav button
        binding.imgFavPA.setOnClickListener {
            //set fav icon
            if (isFavorite) {
                isFavorite = false
                binding.imgFavPA.setImageResource(R.drawable.ic_favorite)
                fragment_favorite.songListFF.removeAt(favIndex)
            } else {
                isFavorite = true
                favIndex = songPosition
                binding.imgFavPA.setImageResource(R.drawable.ic_baseline_favorite)
                fragment_favorite.songListFF.add(songListPA[songPosition])
            }

        }

        //-------------seekbar change handler------------
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

    }

    //---------------------------All functions----------------------------

    //initialize
    private fun initialize() {


        //init layout elements
        fabPlayPausePA = binding.fabPlayPause
        tvTitlePA = binding.tvSongTitlePA
        tvArtistPA = binding.tvSongArtistPA
        imgSongPA = binding.imgCurrentSongPA
        imgFavoritePA = binding.imgFavPA

        // init song related var
        songPosition = intent.getIntExtra("INDEX", 0)

        // init songList according to class
        when (intent.getStringExtra("CLASS")) {
            "MusicAdapter" -> {
                initMusicService()

                songListPA = ArrayList()
                songListPA.addAll(fragment_music.songListMF)

                setLayout()
            }

            "FavoriteAdapter" -> {
                initMusicService()

                songListPA = ArrayList()
                songListPA.addAll(fragment_favorite.songListFF)

                setLayout()
            }

            "MusicFragment" -> {
                initMusicService()

                songListPA = ArrayList()
                songListPA.addAll(fragment_music.songListMF)
                songListPA.shuffle()

                setLayout()
            }

            "NowPlaying" -> {
                setLayout()

                //set play pause icon
                if (isPlaying)
                    fabPlayPausePA.setImageResource(R.drawable.ic_pause)
                else
                    fabPlayPausePA.setImageResource(R.drawable.ic_play)


                //seek bar
                binding.tvSeekBarEnd.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())

                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

                setSeekBar()
            }
        }
    }

    //---------------Start Music Services---------------------
    private fun initMusicService() {
        val i = Intent(this, MusicService::class.java)
        bindService(i, this, BIND_AUTO_CREATE)
        startService(i)
    }

    //-------------- Song related functions------------------
    //change song
    private fun changeSong(nextSong: Boolean) {
        setSongPosition(nextSong)

        //set layout of player activity
        setLayout()

        //set layout of now playing
        setLayoutNP()

        // init music player again
        createMediaPlayer()
    }

    //play song
    private fun playSong() {
        binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()

        //moving title
        binding.tvSongTitlePA.isSelected = true
    }

    //pause song
    private fun pauseSong() {
        binding.fabPlayPause.setImageResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()

        //moving title
        binding.tvSongTitlePA.isSelected = false
    }

    // open equalizer
    private fun openEqualizer() {
        try {
            val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            equalizerIntent.putExtra(
                AudioEffect.EXTRA_AUDIO_SESSION,
                musicService!!.mediaPlayer!!.audioSessionId
            )
            equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
            equalizerIntent.putExtra(
                AudioEffect.EXTRA_CONTENT_TYPE,
                AudioEffect.CONTENT_TYPE_MUSIC
            )

            startActivityForResult(equalizerIntent, 10)

        } catch (e: Exception) {
            Toast.makeText(this, "Equalizer feature not supported", Toast.LENGTH_SHORT).show()
        }
    }

    //------------player setup related functions-------------

    //create music player
    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()

            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(songListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()

            isPlaying = true
            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)

            //set song id
            nowPlayingID = songListPA[songPosition].id

            //seek bar
            binding.tvSeekBarEnd.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())

            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

            //show notification
            musicService!!.showNotification(R.drawable.ic_pause)

            //call 'on song complete' function
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to create Media Player", Toast.LENGTH_SHORT).show()
        }
    }

    //set layout of music player
    private fun setLayout() {
        //set fav index
        favIndex = checkFavorite(songListPA[songPosition].id)

        //set song info
        binding.tvSongTitlePA.text = songListPA[songPosition].title
        binding.tvSongArtistPA.text = songListPA[songPosition].artist

        //moving title
        binding.tvSongTitlePA.isSelected = true

        //set image
        setImgArt(baseContext, songListPA[songPosition].path, imgSongPA)

        //set blurry background
        setBlurImgArt(this, songListPA[songPosition].path,binding.backgroundPA)

        //set button colors
        if (isRepeat)
            binding.imgRepeat.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.colorAccentDark
                )
            )

        //set fav icon
        if (isFavorite)
            binding.imgFavPA.setImageResource(R.drawable.ic_baseline_favorite)
        else
            binding.imgFavPA.setImageResource(R.drawable.ic_favorite)


    }

    // set layout of Now Playing
    private fun setLayoutNP() {

        //song image
        setImgArt(
            baseContext, songListPA[songPosition].path,
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

    }

    //set seekbar position
    private fun setSeekBar() {

        runnable = Runnable {
            binding.tvSeekBarStart.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition

            //increment seekbar with song position
            Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
        }

        // start runnable after 0 millisecond
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    //----------------on song completion----------------
    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        //run next song after song complete
        changeSong(nextSong = true)
    }

    //---------------- Music Service functions---------------
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MusicBinder
        musicService = binder.currentService()
        createMediaPlayer()
        setSeekBar()

        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    //------------------- Activity Result-------------------
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //checking for equalizer
        if (resultCode == 10 && resultCode == RESULT_OK)
            return

    }
}