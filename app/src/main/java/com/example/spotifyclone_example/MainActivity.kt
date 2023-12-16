@file:Suppress("DEPRECATION")

package com.example.spotifyclone_example

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.spotifyclone_example.databinding.ActivityMainBinding
import com.example.spotifyclone_example.fragments.fragment_favorite
import com.example.spotifyclone_example.fragments.fragment_home
import com.example.spotifyclone_example.fragments.fragment_music

class MainActivity : AppCompatActivity() {
private lateinit var binding: ActivityMainBinding
private val homeFragment = fragment_home()
private val favoriteFragment = fragment_favorite()
private val musicFragment = fragment_music()
private val playlistFragment = fragment_playlist()

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    //request storage permission
    requestRuntimePermissions()

    //load music fragment by default
    loadFragment(musicFragment)


    // bottom nav bar
    binding.bottomNav.setOnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.itemHome -> loadFragment(homeFragment)
            R.id.itemMusic -> loadFragment(musicFragment)
            R.id.itemFavorite -> loadFragment(favoriteFragment)
            R.id.itemPlaylist -> loadFragment(playlistFragment)
        }
        true
    }


}


private fun requestRuntimePermissions() {
    if (ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
    }
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == 100) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
    } else {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
    }
}

private fun loadFragment(fragment: Fragment) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(R.id.mainContainer, fragment)
    transaction.commit()
}
}