package com.example.spotifyclone_example.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifyclone_example.R
import com.example.spotifyclone_example.databinding.FragmentFavoriteBinding
import com.example.spotifyclone_example.models.Song
import com.vishal.cocaine.adapters.FavoriteAdapter

class fragment_favorite : Fragment() {
    companion object {
        var songListFF: ArrayList<Song> = ArrayList()
    }
    private lateinit var binding:FragmentFavoriteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        binding = FragmentFavoriteBinding.bind(view)
        binding.root.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //recycler setup
        setSongRecyclerFF()

    }

    private fun setSongRecyclerFF() {

        binding.rvSongsListFF.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSongsListFF.adapter = FavoriteAdapter(requireContext(), songListFF)

        val songListAnimFF = LayoutAnimationController(
            AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_up_anim
            )
        )
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        binding.rvSongsListFF.layoutAnimation = songListAnimFF

    }
}