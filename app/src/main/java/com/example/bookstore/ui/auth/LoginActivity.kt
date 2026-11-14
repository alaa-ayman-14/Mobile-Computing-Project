package com.example.bookstore.ui.auth

import android.content.Intent
import com.example.bookstore.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import com.example.bookstore.ui.home.HomeActivity
import com.example.bookstore.ui.SystemNavigationBarStyler

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        SystemNavigationBarStyler.apply(this)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val authRepository = (application as BookStoreApp).authRepository

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            tilEmail.error = null
            tilPassword.error = null

            var isValid = true

            if (email.isBlank()) {
                tilEmail.error = "Email is required"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email address"
                isValid = false
            }

            if (password.isBlank()) {
                tilPassword.error = "Password is required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            btnLogin.isEnabled = false

            lifecycleScope.launch {
                try {

                    val loggedInUser = authRepository.login(
                        email = email,
                        password = password
                    )

                    if (loggedInUser != null) {

                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(this@LoginActivity, HomeActivity::class.java)
                        )

                        finish()

                    } else {

                        Toast.makeText(
                            this@LoginActivity,
                            "Email or password is incorrect",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this@LoginActivity,
                        "Something went wrong. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {
                    btnLogin.isEnabled = true
                }
            }
        }
    }
}