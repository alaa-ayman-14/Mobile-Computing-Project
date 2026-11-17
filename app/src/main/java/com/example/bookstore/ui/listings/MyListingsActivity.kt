package com.example.bookstore.ui.listings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.data.entity.Book
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.auth.LoginActivity
import com.example.bookstore.ui.bookdetails.BookDetailsActivity
import com.example.bookstore.ui.navigation.BottomNavigationHelper
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyListingsActivity : AppCompatActivity() {

    private lateinit var rvMyListings: RecyclerView
    private lateinit var emptyListingsLayout: LinearLayout
    private lateinit var btnAddBook: MaterialButton
    private lateinit var listingsAdapter: MyListingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_listings)
        SystemNavigationBarStyler.apply(this)

        BottomNavigationHelper.setupBottomNavigation(
            activity = this,
            selectedItemId = R.id.navigation_listings
        )

        rvMyListings = findViewById(R.id.rvMyListings)
        emptyListingsLayout = findViewById(R.id.emptyListingsLayout)
        btnAddBook = findViewById(R.id.btnAddBook)

        setupRecyclerView()
        setupListeners()
        observeSellerBooks()
    }

    private fun setupRecyclerView() {
        listingsAdapter = MyListingsAdapter(
            books = emptyList(),
            onBookClick = { book ->
                val intent = Intent(this, BookDetailsActivity::class.java).apply {
                    putExtra("bookId", book.id)
                }
                startActivity(intent)
            },
            onEditClick = { book ->
                val intent = Intent(this, AddEditBookActivity::class.java).apply {
                    putExtra(AddEditBookActivity.EXTRA_BOOK_ID, book.id)
                }
                startActivity(intent)
            },
            onToggleSoldClick = { book ->
                toggleSoldStatus(book)
            },
            onDeleteClick = { book ->
                confirmAndDeleteBook(book)
            }
        )

        rvMyListings.layoutManager = LinearLayoutManager(this)
        rvMyListings.adapter = listingsAdapter
    }

    private fun setupListeners() {
        btnAddBook.setOnClickListener {
            val intent = Intent(this, AddEditBookActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeSellerBooks() {
        val app = application as BookStoreApp

        lifecycleScope.launch {
            val currentUserId = app.sessionManager.userId.first()
            if (currentUserId == null) {
                Toast.makeText(
                    this@MyListingsActivity,
                    "Please log in to view your listings",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@MyListingsActivity, LoginActivity::class.java))
                finish()
                return@launch
            }

            app.database.bookDao().getBooksBySeller(currentUserId).collect { sellerBooks ->
                if (sellerBooks.isEmpty()) {
                    rvMyListings.visibility = View.GONE
                    emptyListingsLayout.visibility = View.VISIBLE
                } else {
                    emptyListingsLayout.visibility = View.GONE
                    rvMyListings.visibility = View.VISIBLE
                    listingsAdapter.updateBooks(sellerBooks)
                }
            }
        }
    }

    private fun toggleSoldStatus(book: Book) {
        val app = application as BookStoreApp
        val newSoldStatus = !book.isSold

        lifecycleScope.launch {
            try {
                app.database.bookDao().updateSoldStatus(book.id, newSoldStatus)
                val message = if (newSoldStatus) {
                    "Book marked as Sold"
                } else {
                    "Book marked as Available"
                }
                Toast.makeText(this@MyListingsActivity, message, Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@MyListingsActivity,
                    "Failed to update book status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun confirmAndDeleteBook(book: Book) {
        AlertDialog.Builder(this)
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete \"${book.title}\"?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteBook(book)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteBook(book: Book) {
        val app = application as BookStoreApp

        lifecycleScope.launch {
            try {
                app.database.bookDao().deleteBook(book)
                Toast.makeText(
                    this@MyListingsActivity,
                    "Book deleted",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@MyListingsActivity,
                    "Failed to delete book",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}