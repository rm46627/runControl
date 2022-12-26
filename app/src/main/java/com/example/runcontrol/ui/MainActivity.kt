package com.example.runcontrol.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runcontrol.R
import com.example.runcontrol.databinding.ActivityMainBinding
import com.example.runcontrol.extensions.View.gone
import com.example.runcontrol.extensions.View.show
import com.example.runcontrol.ui.permission.PermissionFragment
import com.example.runcontrol.utils.Permissions.hasLocationPermission
import dagger.hilt.android.AndroidEntryPoint

//      TODO: animate bottomNav sliding up and down

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        if(hasLocationPermission(this) && navHostFragment.childFragmentManager.fragments[0] is PermissionFragment){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
            binding.bottomNavigationView.show()
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapsFragment,
                R.id.controlFragment
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mapsFragment -> binding.bottomNavigationView.show()
                R.id.controlFragment -> binding.bottomNavigationView.show()
                R.id.historyFragment -> binding.bottomNavigationView.show()
                R.id.resultFragment -> binding.bottomNavigationView.show()
                else -> binding.bottomNavigationView.gone()
            }
        }

        binding.bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        mainViewModel.canChangeFragment.observe(this){
            when(it){
                true -> binding.bottomNavigationView.menu.getItem(1).isEnabled = true
                false -> binding.bottomNavigationView.menu.getItem(1).isEnabled = false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}