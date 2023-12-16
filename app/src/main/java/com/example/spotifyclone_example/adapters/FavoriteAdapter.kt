package com.vishal.cocaine.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyclone_example.PlayerActivity
import com.example.spotifyclone_example.R
import com.example.spotifyclone_example.models.Song
import com.example.spotifyclone_example.models.formatDuration

class FavoriteAdapter(var context: Context, private var songList: ArrayList<Song>) :
    RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.songTitle.text = songList[position].title
        holder.songArtist.text = songList[position].artist
        holder.songDuration.text = formatDuration(songList[position].duration)

        //set image
//         setImgArt(context,songList[position].path,holder.songImg)

        holder.itemView.setOnClickListener {
            when (songList[position].id) {
                PlayerActivity.nowPlayingID -> {
                    sendIntent("NowPlaying", PlayerActivity.songPosition)
                }
                else -> sendIntent("FavoriteAdapter", position)
            }

        }
    }

    private fun sendIntent(ref: String, pos: Int) {
        val i = Intent(context, PlayerActivity::class.java)
        i.putExtra("INDEX", pos)
        i.putExtra("CLASS", ref)
        context.startActivity(i)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var songTitle: TextView
        var songArtist: TextView
        var songDuration: TextView

        init {

            songTitle = itemView.findViewById<View>(R.id.tvSongTitle) as TextView
            songArtist = itemView.findViewById<View>(R.id.tvSongArtist) as TextView
            songDuration = itemView.findViewById<View>(R.id.tvSongDuration) as TextView
        }
//
    }

}
