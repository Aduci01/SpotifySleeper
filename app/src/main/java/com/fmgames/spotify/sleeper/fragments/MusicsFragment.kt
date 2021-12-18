package com.fmgames.spotify.sleeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fmgames.spotify.sleeper.R
import com.fmgames.spotify.sleeper.databinding.FragmentMusicsBinding
import com.fmgames.spotify.sleeper.databinding.FragmentTimeSetterBinding
import com.fmgames.spotify.sleeper.spotify.SpotifyManager

class MusicsFragment : Fragment() {
    private lateinit var binding: FragmentMusicsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMusicsBinding.inflate(LayoutInflater.from(context))

        initUi()

        return binding.root
    }

    private fun initUi(){
        binding.musicSleep.setOnClickListener {
            onClickMusicCard("spotify:playlist:21wbvqMl5HNxhfi2cNqsdZ")
        }

        binding.musicAccoustic.setOnClickListener {
            onClickMusicCard("spotify:playlist:4uf0gphPVNtk4Uu0bxtdGA")
        }

        binding.musicClassical.setOnClickListener {
            onClickMusicCard("spotify:playlist:1h0CEZCm6IbFTbxThn6Xcs")
        }
    }

    private fun onClickMusicCard(uri: String){
        SpotifyManager.tryToStartPlaylist(uri, binding.root.context)
    }

}