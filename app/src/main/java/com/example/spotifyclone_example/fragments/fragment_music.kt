package com.example.spotifyclone_example.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifyclone_example.PlayerActivity
import com.example.spotifyclone_example.R
import com.example.spotifyclone_example.databinding.FragmentFavoriteBinding
import com.example.spotifyclone_example.databinding.FragmentMusicBinding
import com.example.spotifyclone_example.models.Song
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.vishal.cocaine.adapters.MusicAdapter
import java.io.File
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
class fragment_music : Fragment() {
    private lateinit var binding: FragmentMusicBinding
    companion object {
        lateinit var songListMF: ArrayList<Song>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)
        binding = FragmentMusicBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initialize
        fragment_favorite.songListFF = ArrayList()

        //load all songs
        songListMF = loadAllSongs()

        //load fav songs
        loadFavSongs()
        //shuffle button
        binding.fabShuffle.setOnClickListener {
            val i = Intent(requireContext(), PlayerActivity::class.java)
            i.putExtra("INDEX", 0)
            i.putExtra("CLASS", "MusicFragment")
            requireContext().startActivity(i)
        }
        //recycler setup
        setSongRecyclerMF()
    }

    private fun setSongRecyclerMF() {

        binding.rvSongsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSongsList.adapter = MusicAdapter(requireContext(), songListMF)

        val songListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_up_anim))
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        binding.rvSongsList.layoutAnimation = songListAnimFF

    }

    private fun loadFavSongs() {
        //load fav list data using data preferences
        val editor = requireContext().getSharedPreferences("FAVORITE", Context.MODE_PRIVATE)
        val jsonString = editor.getString("FavoriteSongs", null)
        val tokenType = object : TypeToken<ArrayList<Song>>() {}.type
        if (jsonString != null) {
            val favData: ArrayList<Song> = GsonBuilder().create().fromJson(jsonString, tokenType)
            fragment_favorite.songListFF.addAll(favData)
        }
    }

    @SuppressLint("Recycle", "Range")
    private fun loadAllSongs(): ArrayList<Song> {
        val tempSongList = ArrayList<Song>()

        // selecting audio that are not null
        val selection = MediaStore.Audio.Media.TITLE + " != 0"

        // song info we need
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // init cursor
        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )

        // extracting data from song using cursor
        if (cursor != null)
            if (cursor.moveToFirst()) {
                do {
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                    // to get song image path
                    val albumID =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumID).toString()

                    val song = Song(
                        id = idC,
                        title = titleC,
                        artist = artistC,
                        duration = durationC,
                        path = pathC,
                        artUri = artUriC
                    )

                    // add data if file exits
                    val file = File(song.path)
                    if (file.exists())
                        tempSongList.add(song)

                } while (cursor.moveToNext())
                cursor.close()
            }

        return tempSongList
    }

    override fun onResume() {
        super.onResume()

        //store fav list data using data preferences
        val jsonString = GsonBuilder().create().toJson(fragment_favorite.songListFF)
        val editor = requireContext().getSharedPreferences("FAVORITE", Context.MODE_PRIVATE).edit()
        editor.putString("FavoriteSongs", jsonString)
        editor.apply()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onDestroy() {
        super.onDestroy()

        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            PlayerActivity.musicService!!.stopForeground(true)
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)
        }
    }
}