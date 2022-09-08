package com.example.runcontrol.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.runcontrol.R
import com.example.runcontrol.util.Permissions.hasLocationPermission
import com.example.runcontrol.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController :NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        if(hasLocationPermission(this)){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
        }
    }
}