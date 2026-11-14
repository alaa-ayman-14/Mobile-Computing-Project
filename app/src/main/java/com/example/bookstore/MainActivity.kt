
package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.ui.auth.LoginActivity
import com.example.bookstore.ui.home.HomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var isRoutingComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {

        // Install Splash Screen before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Keep Splash Screen visible while checking the session
        splashScreen.setKeepOnScreenCondition {
            !isRoutingComplete
        }

        val app = application as BookStoreApp

        lifecycleScope.launch {

            // Get saved user ID
            val savedUserId = app.sessionManager.userId.first()

            val destinationIntent: Intent

            if (savedUserId == null) {

                // No saved session
                destinationIntent = Intent(
                    this@MainActivity,
                    LoginActivity::class.java
                )

            } else {

                // Check if user still exists
                val user = app.database.userDao()
                    .getUserById(savedUserId)

                if (user != null) {

                    destinationIntent = Intent(
                        this@MainActivity,
                        HomeActivity::class.java
                    )

                } else {

                    // Clear invalid session
                    app.sessionManager.clearSession()

                    destinationIntent = Intent(
                        this@MainActivity,
                        LoginActivity::class.java
                    )
                }
            }

            // Open the correct screen
            startActivity(destinationIntent)

            // Disable the default Activity transition
            overridePendingTransition(0, 0)

            // Allow Splash Screen to disappear
            isRoutingComplete = true

            // Close MainActivity
            finish()
        }
    }
}
