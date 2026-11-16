package com.example.bookstore.ui.favorites

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookstore.R
import kotlinx.coroutines.flow.first
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookstore.databinding.ActivityFavoritesBinding

import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


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