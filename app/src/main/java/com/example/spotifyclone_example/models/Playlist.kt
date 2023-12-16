package com.example.spotifyclone_example.models

data class Playlist(
    val title:String,
    val list: ArrayList<Song>,
    val totalSongs: Int
)
