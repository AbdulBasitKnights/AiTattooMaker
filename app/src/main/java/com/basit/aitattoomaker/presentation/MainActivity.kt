package com.basit.aitattoomaker.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.ActivityMainBinding
import com.basit.aitattoomaker.extension.hideSystemBars
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        this.hideSystemBars()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding?.navView?.itemBackground = null
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
        try {
            viewModel.registerResponse.observe(this){
                if(it!=null){
                    Log.e("VM","Response: ${it.response}")
                    Log.e("VM","Meta: ${it.meta}")
                }
            }
        } catch (e: Exception) {
           e.printStackTrace()
        }
        navView.setupWithNavController(navController)
    }
    fun hidebottombar() {
        binding?.navView?.visibility = View.GONE
    }
    fun showbottombar() {
        binding?.navView?.visibility = View.VISIBLE
    }
}