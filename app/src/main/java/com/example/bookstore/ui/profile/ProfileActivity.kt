package com.example.bookstore.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.auth.LoginActivity
import com.example.bookstore.ui.listings.MyListingsActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        SystemNavigationBarStyler.apply(this)

        val app = application as BookStoreApp

        val tvFullName = findViewById<TextView>(R.id.tvFullName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvPhoneNumber = findViewById<TextView>(R.id.tvPhoneNumber)
        val btnUpdate = findViewById<MaterialButton>(R.id.btnUpdate)
        val btnMyListings = findViewById<MaterialButton>(R.id.btnMyListings)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        lifecycleScope.launch {
            try {
                val userId = app.sessionManager.userId.first()
                if (userId != null) {
                    val user = app.database.userDao().getUserById(userId)
                    if (user != null) {
                        tvFullName.text = user.fullName
                        tvEmail.text = user.email
                        tvPhoneNumber.text = user.phoneNumber
                    }
                }
            } catch (_: Exception) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load profile details",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        // Update button - currently does nothing
        btnUpdate.setOnClickListener {
            // Intentionally empty for now
        }

        // Navigate to My Listings screen
        btnMyListings.setOnClickListener {
            val intent = Intent(this, MyListingsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            btnLogout.isEnabled = false

            lifecycleScope.launch {
                try {
                    app.authRepository.logout()

                    val intent = Intent(
                        this@ProfileActivity,
                        LoginActivity::class.java
                    )
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()

                } catch (_: Exception) {
                    btnLogout.isEnabled = true
                    Toast.makeText(
                        this@ProfileActivity,
                        "Logout failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
