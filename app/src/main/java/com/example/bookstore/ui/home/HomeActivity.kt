
package com.example.bookstore.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        SystemNavigationBarStyler.apply(this)

        val app = application as BookStoreApp

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {

            btnLogout.isEnabled = false

            lifecycleScope.launch {
                try {

                    // Clear the saved user session
                    app.authRepository.logout()

                    // Navigate to Login
                    val intent = Intent(
                        this@HomeActivity,
                        LoginActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                } catch (e: Exception) {

                    btnLogout.isEnabled = true

                    Toast.makeText(
                        this@HomeActivity,
                        "Logout failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
