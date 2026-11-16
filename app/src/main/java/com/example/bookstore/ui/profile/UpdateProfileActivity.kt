package com.example.bookstore.ui.profile

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UpdateProfileActivity : AppCompatActivity() {

    private var currentUserId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        SystemNavigationBarStyler.apply(this)

        val app = application as BookStoreApp
        val authRepository = app.authRepository

        val tilFullName = findViewById<TextInputLayout>(R.id.tilFullName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPhone = findViewById<TextInputLayout>(R.id.tilPhone)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirmPassword = findViewById<TextInputLayout>(R.id.tilConfirmPassword)

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val btnUpdateProfile = findViewById<MaterialButton>(R.id.btnUpdateProfile)

        // Load existing user details to pre-fill fields
        lifecycleScope.launch {
            try {
                val userId = app.sessionManager.userId.first()
                if (userId != null) {
                    currentUserId = userId
                    val user = app.database.userDao().getUserById(userId)
                    if (user != null) {
                        etFullName.setText(user.fullName)
                        etEmail.setText(user.email)
                        etPhone.setText(user.phoneNumber)
                    }
                }
            } catch (_: Exception) {
                Toast.makeText(
                    this@UpdateProfileActivity,
                    "Failed to load user details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnUpdateProfile.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            tilFullName.error = null
            tilEmail.error = null
            tilPhone.error = null
            tilPassword.error = null
            tilConfirmPassword.error = null

            var isValid = true

            // Validate Full Name
            if (fullName.isBlank()) {
                tilFullName.error = "Full name is required"
                isValid = false
            }

            // Validate Email
            if (email.isBlank()) {
                tilEmail.error = "Email is required"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email address"
                isValid = false
            }

            // Validate Phone Number
            if (!phone.matches(Regex("^[0-9]{11}$"))) {
                tilPhone.error = "Phone number must be exactly 11 digits"
                isValid = false
            }

            // Validate Password if user typed something in password field
            if (password.isNotEmpty()) {
                if (password.length < 8) {
                    tilPassword.error = "Password must be at least 8 characters"
                    isValid = false
                }

                if (confirmPassword.isBlank()) {
                    tilConfirmPassword.error = "Please confirm your password"
                    isValid = false
                } else if (password != confirmPassword) {
                    tilConfirmPassword.error = "Passwords do not match"
                    isValid = false
                }
            }

            if (!isValid) return@setOnClickListener

            btnUpdateProfile.isEnabled = false

            lifecycleScope.launch {
                try {
                    val updatedUser = authRepository.updateProfile(
                        userId = currentUserId,
                        fullName = fullName,
                        email = email,
                        phoneNumber = phone,
                        newPassword = if (password.isNotEmpty()) password else null
                    )

                    if (updatedUser != null) {
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        tilEmail.error = "This email is already in use by another account"
                    }
                } catch (_: Exception) {
                    Toast.makeText(
                        this@UpdateProfileActivity,
                        "Update failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    btnUpdateProfile.isEnabled = true
                }
            }
        }
    }
}
