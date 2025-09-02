package com.basit.aitattoomaker.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
            viewModel.registerResponse.observe(this) { result ->
                result.onSuccess { registerResponse ->
                    // Handle success (e.g., show success message or navigate to another screen)
                    Toast.makeText(this, "Registration Successful: ${registerResponse.response.device_id}", Toast.LENGTH_SHORT).show()
                }.onFailure { exception ->
                    // Handle failure (e.g., show error message)
//                    Toast.makeText(this, "Registration Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VM","Exception: ${exception.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("VM","Exception: ${e.message}")
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