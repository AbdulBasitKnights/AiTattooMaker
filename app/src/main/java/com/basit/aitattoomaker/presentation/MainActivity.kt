package com.basit.aitattoomaker.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.basit.aitattoomaker.BuildConfig
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.ads.AdsManager.loadInterstitialAdAfterSplash
import com.basit.aitattoomaker.data.repo.ModelName
import com.basit.aitattoomaker.databinding.ActivityMainBinding
import com.basit.aitattoomaker.extension.hideSystemBars
import com.basit.aitattoomaker.presentation.utils.access_Token
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        this.hideSystemBars()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding?.navView?.itemBackground = null
        loadInterstitialAdAfterSplash(this,resources.getString(R.string.inter_af_home_hf),resources.getString(R.string.inter_af_home),{},{},{})
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        if (binding.navView != null) {
            NavigationUI.setupWithNavController(binding.navView, navController)
            hidebottombar()
            navController.navigate(R.id.navigation_aicamera)
          /*  binding.navView.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_aicreate -> {
                        showbottombar()
                        navController.navigate(R.id.navigation_aicreate)
                        true
                    }

                    R.id.navigation_settings -> {
                        showbottombar()
                        navController.navigate(R.id.navigation_settings)
                        true
                    }

                    R.id.navigation_history -> {
                        showbottombar()
                        navController.navigate(R.id.navigation_history)
                        true
                    }

                    R.id.navigation_aicamera -> {
                        hidebottombar()
                        navController.navigate(R.id.navigation_aicamera)
                        true
                    }

                    else -> {
                        showbottombar()
                    navController.navigate(R.id.navigation_aicreate)
                        true
                    }
                }

            }*/
        }

        registerUser()
    }

    @SuppressLint("HardwareIds")
    private fun registerUser() {
        try {
            lifecycleScope.launch {
                        try {
                            val manufacturer = Build.MANUFACTURER
                            val model = Build.MODEL
                            val deviceName = "${manufacturer} ${model}"
                            viewModel.registerUser(ModelName(deviceName))
                            viewModel.registerResponse.observe(this@MainActivity) { result ->
                                result.onSuccess { registerResponse ->
                                    Log.d("VM","Success Registration")
//                                    Toast.makeText(this@MainActivity, "Registration Successful: ${registerResponse.response.device_id}", Toast.LENGTH_SHORT).show()
                                }.onFailure { exception ->
                                    // Handle failure (e.g., show error message)
                                    Log.e("VM","Exception: ${exception.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("VM","Exception: ${e.message}")
                            e.printStackTrace()
                        }
                        viewModel.getTokenResponse.observe(this@MainActivity) { result ->
                            result.onSuccess { registerResponse ->
                                // Handle success (e.g., show success message or navigate to another screen)
                                lifecycleScope.launch {
                                    access_Token=registerResponse.response.access_token
                                    viewModel.storeAccessToken(this@MainActivity, registerResponse.response.access_token)
                                }
                                Toast.makeText(this@MainActivity, "Access Token from Api: ${registerResponse.response.access_token}", Toast.LENGTH_SHORT).show()
                            }.onFailure { exception ->
                                // Handle failure (e.g., show error message)
                                Log.e("VM","Exception: ${exception.message}")
                            }
                        }
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun hidebottombar() {
        binding?.navView?.visibility = View.GONE
    }
    fun showbottombar() {
        binding?.navView?.visibility = View.VISIBLE
    }



}