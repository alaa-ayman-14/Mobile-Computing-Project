
package com.example.bookstore.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        SystemNavigationBarStyler.apply(this)

        val authRepository = (application as BookStoreApp).authRepository

        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)

        val tilFullName = findViewById<TextInputLayout>(R.id.tilFullName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPhone = findViewById<TextInputLayout>(R.id.tilPhone)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirmPassword =
            findViewById<TextInputLayout>(R.id.tilConfirmPassword)

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword =
            findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        tvGoToLogin.setOnClickListener {
            finish()
        }

        findViewById<android.widget.Button>(R.id.btnRegister)
            .setOnClickListener {

                // Get input values
                val fullName = etFullName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                // Clear previous errors
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

                // Validate Password
                if (password.length < 8) {
                    tilPassword.error = "Password must be at least 8 characters"
                    isValid = false
                }

                // Validate Confirm Password
                if (confirmPassword.isBlank()) {
                    tilConfirmPassword.error = "Please confirm your password"
                    isValid = false
                } else if (password != confirmPassword) {
                    tilConfirmPassword.error = "Passwords do not match"
                    isValid = false
                }

                // Check validation result
                if (isValid) {

                    btnRegister.isEnabled = false

                    lifecycleScope.launch {

                        try {
                            val registeredUser = authRepository.register(
                                fullName = fullName,
                                email = email,
                                password = password,
                                phoneNumber = phone
                            )

                            if (registeredUser != null) {

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Account created successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                finish()

                            } else {

                                tilEmail.error = "This email is already registered"

                            }

                        } catch (e: Exception) {

                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()

                        } finally {

                            btnRegister.isEnabled = true

                        }
                    }
                }
            }
    }
}
