package com.basit.aitattoomaker.presentation

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.basit.aitattoomaker.BuildConfig
import com.basit.aitattoomaker.R
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
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        registerUser()
    }

    @SuppressLint("HardwareIds")
    private fun registerUser() {
        try {
            lifecycleScope.launch {
                        try {
                            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                            val manufacturer = Build.MANUFACTURER
                            val model = Build.MODEL
                            val deviceName = "${manufacturer} ${model}"
                            viewModel.registerUser(androidId, "tato", "android", BuildConfig.VERSION_NAME, ModelName(deviceName))
                            viewModel.registerResponse.observe(this@MainActivity) { result ->
                                result.onSuccess { registerResponse ->
                                    Log.d("VM","Success Registration")
//                                    Toast.makeText(this@MainActivity, "Registration Successful: ${registerResponse.response.device_id}", Toast.LENGTH_SHORT).show()
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
//                    Toast.makeText(this, "Registration Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
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