package com.vishal.cocaine.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyclone_example.R
import com.example.spotifyclone_example.fragment_playlist
import com.example.spotifyclone_example.models.Playlist

class PlaylistAdapter(var context: Context,var playlistData: ArrayList<Playlist>) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.playlistTitle.text = playlistData[position].title
        holder.playlistTitle.isSelected = true
    }

    override fun getItemCount(): Int {
        return playlistData.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        playlistData = ArrayList()
        playlistData.addAll(fragment_playlist.playlistPF.ref)
        notifyDataSetChanged()
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var playlistTitle: TextView


        init {

            playlistTitle = itemView.findViewById<View>(R.id.tvPlaylistTitlePI) as TextView

        }
    }
}
