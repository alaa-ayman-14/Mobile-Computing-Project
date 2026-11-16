package com.example.bookstore.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.R
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.navigation.BottomNavigationHelper

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        SystemNavigationBarStyler.apply(this)

        BottomNavigationHelper.setupBottomNavigation(
            activity = this,
            selectedItemId = R.id.navigation_home
        )
    }
}
