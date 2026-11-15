package com.example.bookstore.ui.bookdetails

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookstore.R
import com.example.bookstore.BookStoreApp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import android.net.Uri
import com.google.android.material.button.MaterialButton
import android.content.Intent

class BookDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_details)

        val bookId = intent.getLongExtra("bookId", -1L)

        if (bookId == -1L) {
            finish()
            return
        }

        val app = application as BookStoreApp

        lifecycleScope.launch {
            val book = app.database.bookDao().getBookById(bookId)

            if (book == null) {
                Toast.makeText(
                    this@BookDetailsActivity,
                    "Book not found",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            val seller = app.database.userDao().getUserById(book.sellerId)

            val tvSellerName = findViewById<TextView>(R.id.tvSellerName)

            if (seller != null) {
                tvSellerName.text = seller.fullName
            }
            val btnContactSeller = findViewById<MaterialButton>(R.id.btnContactSeller)

            btnContactSeller.setOnClickListener {
                if (seller != null && seller.phoneNumber.isNotBlank()) {
                    val phone = seller.phoneNumber.filter { it.isDigit() }
                    val whatsappNumber = if (phone.startsWith("0") && phone.length == 11) {
                        "20${phone.substring(1)}"
                    } else {
                        phone
                    }
                    val whatsappIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/$whatsappNumber")                    )

                    startActivity(whatsappIntent)
                }
            }

            val tvBookTitle = findViewById<TextView>(R.id.tvBookTitle)
            tvBookTitle.text = book.title

            val tvBookAuthor = findViewById<TextView>(R.id.tvBookAuthor)
            tvBookAuthor.text = getString(R.string.book_author, book.author)


            val tvBookPrice = findViewById<TextView>(R.id.tvBookPrice)
            val price = book.priceInPiastres / 100.0
            tvBookPrice.text = String.format(java.util.Locale.US, "%.2f EGP", price)

            val tvBookCondition = findViewById<TextView>(R.id.tvBookCondition)
            tvBookCondition.text = book.condition

            val tvBookDescription = findViewById<TextView>(R.id.tvBookDescription)
            tvBookDescription.text = book.description

            val ivBookImage = findViewById<ImageView>(R.id.ivBookImage)

            if (!book.imageUri.isNullOrEmpty()) {
                ivBookImage.setImageURI(Uri.parse(book.imageUri))
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}