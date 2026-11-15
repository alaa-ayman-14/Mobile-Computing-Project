package com.example.bookstore.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.R
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.profile.ProfileActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        SystemNavigationBarStyler.apply(this)

        val btnGoToProfile = findViewById<MaterialButton>(R.id.btnGoToProfile)

        btnGoToProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
