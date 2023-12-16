package com.example.spotifyclone_example

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.spotifyclone_example.databinding.FragmentFavoriteBinding
import com.example.spotifyclone_example.databinding.FragmentPlaylistBinding
import com.example.spotifyclone_example.databinding.PlaylistDialogBinding
import com.example.spotifyclone_example.models.Playlist
import com.example.spotifyclone_example.models.SongPlaylist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vishal.cocaine.adapters.PlaylistAdapter


class fragment_playlist : Fragment() {
    companion object {
        var playlistPF: SongPlaylist = SongPlaylist()
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentPlaylistBinding
    }

    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        binding = FragmentPlaylistBinding.bind(view)


        adapter = PlaylistAdapter(requireContext(), playlistPF.ref)

        //set recycler view
        setRecyclerPF()

        //add playlist button
        binding.fabAddPlaylistPF.setOnClickListener {
            addPlaylistDialog()
        }
        return view
    }

    private fun addPlaylistDialog() {
        val addDialogView =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.playlist_dialog, binding.root, false)
        val binder = PlaylistDialogBinding.bind(addDialogView)
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(addDialogView)
            .setTitle("New Playlist")
            .setPositiveButton("ADD") { dialog, _ ->
                val playlistName = binder.etPlaylistNamePD.text
                if(playlistName != null){
                    if(playlistName.isNotEmpty()){
                        addPlaylist(playlistName.toString())
                    }
                }
                dialog.dismiss()
            }.show()

    }

    private fun addPlaylist(playlistName: String) {
        var isPlaylistExists = false
        for(i in playlistPF.ref){
            if (playlistName.equals(i.title)){
                isPlaylistExists = true
                break
            }
        }

        if (isPlaylistExists){
            Toast.makeText(requireContext(),"Playlist Exists!!", Toast.LENGTH_SHORT).show()
        }        else{
            val tempPlaylist = Playlist(title = playlistName, ArrayList(), totalSongs = 0)
            playlistPF.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
    }

    private fun setRecyclerPF() {
        binding.rvPlaylistPF.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlaylistPF.adapter = adapter
    }
}