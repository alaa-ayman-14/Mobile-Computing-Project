package com.example.bookstore.ui.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.bookstore.R
import com.example.bookstore.ui.favorites.FavoritesActivity
import com.example.bookstore.ui.home.HomeActivity
import com.example.bookstore.ui.listings.MyListingsActivity
import com.example.bookstore.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {

    fun setupBottomNavigation(activity: AppCompatActivity, selectedItemId: Int) {
        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            ?: return

        fun syncSelection() {
            bottomNavigationView.setOnItemSelectedListener(null)
            bottomNavigationView.selectedItemId = selectedItemId
            bottomNavigationView.menu.findItem(selectedItemId)?.isChecked = true

            bottomNavigationView.setOnItemSelectedListener { item ->
                if (item.itemId == selectedItemId) {
                    return@setOnItemSelectedListener true
                }

                val targetClass = when (item.itemId) {
                    R.id.navigation_home -> HomeActivity::class.java
                    R.id.navigation_favorite -> FavoritesActivity::class.java
                    R.id.navigation_listings -> MyListingsActivity::class.java
                    R.id.navigation_profile -> ProfileActivity::class.java
                    else -> null
                }

                if ((targetClass != null) && (activity::class.java != targetClass)) {
                    val intent = Intent(activity, targetClass)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    activity.startActivity(intent)
                    activity.overridePendingTransition(0, 0)
                    true
                } else {
                    false
                }
            }
        }

        syncSelection()

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                syncSelection()
            }
        })
    }
}
