package com.example.projektworldwisdom

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Setup Navigation with BottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavView.setupWithNavController(navController)

        // Hide BottomNavigation on auth screens (Login/Register)
        val destinationsWithoutBottomNav = setOf(
            R.id.loginFragment,
            R.id.registerFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavView.visibility =
                if (destination.id in destinationsWithoutBottomNav) View.GONE else View.VISIBLE
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}