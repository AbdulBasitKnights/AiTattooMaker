package com.basit.aitattoomaker

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.basit.aitattoomaker.databinding.ActivityMainBinding
import com.basit.aitattoomaker.presentation.utils.CameraPermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
   /*     val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_aicreate, R.id.navigation_aitools, R.id.navigation_search,R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)*/
        navView.setupWithNavController(navController)
    }


}