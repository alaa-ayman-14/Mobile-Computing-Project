package com.example.bookstore.ui.favorites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.databinding.ActivityFavoritesBinding
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.navigation.BottomNavigationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemNavigationBarStyler.apply(this)

        BottomNavigationHelper.setupBottomNavigation(
            activity = this,
            selectedItemId = R.id.navigation_favorite
        )

        favoritesAdapter = FavoritesAdapter { book ->
            lifecycleScope.launch {
                val app = application as BookStoreApp
                val userId = app.sessionManager.userId.first()

                if (userId != null) {
                    app.database.favoriteDao().removeFavorite(
                        userId,
                        book.id
                    )
                }
            }
        }

        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        binding.rvFavorites.adapter = favoritesAdapter

        val app = application as BookStoreApp
        val favoriteDao = app.database.favoriteDao()

        lifecycleScope.launch {
            val userId = app.sessionManager.userId.first()

            if (userId != null) {
                favoriteDao.getFavoriteBooks(userId).collect { books ->
                    favoritesAdapter.updateBooks(books)
                }
            }
        }
    }
}
