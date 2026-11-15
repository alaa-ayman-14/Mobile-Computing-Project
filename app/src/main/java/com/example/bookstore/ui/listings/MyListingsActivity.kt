package com.example.bookstore.ui.listings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.R
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.google.android.material.button.MaterialButton

class MyListingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_listings)
        SystemNavigationBarStyler.apply(this)

        val btnBackToProfile = findViewById<MaterialButton>(R.id.btnBackToProfile)

        btnBackToProfile.setOnClickListener {
            finish()
        }
    }
}
