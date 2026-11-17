package com.example.bookstore.ui.listings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.data.entity.Book
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToLong


class AddEditBookActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK_ID = "bookId"
    }

    private lateinit var tvScreenTitle: TextView
    private lateinit var tvScreenSubtitle: TextView
    private lateinit var ivBookCoverPreview: ImageView
    private lateinit var btnPickImage: MaterialButton

    private lateinit var tilBookTitle: TextInputLayout
    private lateinit var etBookTitle: TextInputEditText

    private lateinit var tilBookAuthor: TextInputLayout
    private lateinit var etBookAuthor: TextInputEditText

    private lateinit var tilBookPrice: TextInputLayout
    private lateinit var etBookPrice: TextInputEditText

    private lateinit var tilBookCategory: TextInputLayout
    private lateinit var actvBookCategory: MaterialAutoCompleteTextView

    private lateinit var tilBookCondition: TextInputLayout
    private lateinit var actvBookCondition: MaterialAutoCompleteTextView

    private lateinit var tilBookDescription: TextInputLayout
    private lateinit var etBookDescription: TextInputEditText

    private lateinit var btnSaveBook: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private var editingBookId: Long = -1L
    private var existingBook: Book? = null
    private var selectedImageUriString: String? = null

    private val categoriesList = listOf(
        "Fiction",
        "Non-Fiction",
        "Computer Science",
        "Engineering",
        "Business & Economics",
        "Science & Math",
        "Literature",
        "History",
        "Self-Help",
        "Other"
    )

    private val conditionsList = listOf(
        "New",
        "Like New",
        "Good",
        "Acceptable"
    )

    // Image Picker Launcher with Persistable URI Permission
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Ignore if the provider does not support persistable permissions
            }

            selectedImageUriString = uri.toString()
            displayBookImage(selectedImageUriString)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_book)
        SystemNavigationBarStyler.apply(this)

        bindViews()
        setupDropdownMenus()
        setupClickListeners()

        editingBookId = intent.getLongExtra(EXTRA_BOOK_ID, -1L)
        if (editingBookId != -1L) {
            loadBookForEditing(editingBookId)
        }
    }

    private fun bindViews() {
        tvScreenTitle = findViewById(R.id.tvAddEditScreenTitle)
        tvScreenSubtitle = findViewById(R.id.tvAddEditScreenSubtitle)
        ivBookCoverPreview = findViewById(R.id.ivBookCoverPreview)
        btnPickImage = findViewById(R.id.btnPickImage)

        tilBookTitle = findViewById(R.id.tilBookTitle)
        etBookTitle = findViewById(R.id.etBookTitle)

        tilBookAuthor = findViewById(R.id.tilBookAuthor)
        etBookAuthor = findViewById(R.id.etBookAuthor)

        tilBookPrice = findViewById(R.id.tilBookPrice)
        etBookPrice = findViewById(R.id.etBookPrice)

        tilBookCategory = findViewById(R.id.tilBookCategory)
        actvBookCategory = findViewById(R.id.actvBookCategory)

        tilBookCondition = findViewById(R.id.tilBookCondition)
        actvBookCondition = findViewById(R.id.actvBookCondition)

        tilBookDescription = findViewById(R.id.tilBookDescription)
        etBookDescription = findViewById(R.id.etBookDescription)

        btnSaveBook = findViewById(R.id.btnSaveBook)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupDropdownMenus() {
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categoriesList
        )
        actvBookCategory.setAdapter(categoryAdapter)

        val conditionAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            conditionsList
        )
        actvBookCondition.setAdapter(conditionAdapter)
    }

    private fun setupClickListeners() {
        btnPickImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        ivBookCoverPreview.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnSaveBook.setOnClickListener {
            validateAndSaveBook()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadBookForEditing(bookId: Long) {
        tvScreenTitle.text = "Edit Book"
        tvScreenSubtitle.text = "Update your book listing details"
        btnSaveBook.text = "Update Book"

        val app = application as BookStoreApp
        lifecycleScope.launch {
            val book = app.database.bookDao().getBookById(bookId)
            if (book == null) {
                Toast.makeText(
                    this@AddEditBookActivity,
                    "Book not found",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            existingBook = book
            selectedImageUriString = book.imageUri

            etBookTitle.setText(book.title)
            etBookAuthor.setText(book.author)

            val priceInEgp = book.priceInPiastres / 100.0
            etBookPrice.setText(String.format(Locale.US, "%.2f", priceInEgp))

            actvBookCategory.setText(book.category, false)
            actvBookCondition.setText(book.condition, false)
            etBookDescription.setText(book.description)

            displayBookImage(book.imageUri)
        }
    }

    private fun displayBookImage(uriString: String?) {
        if (!uriString.isNullOrEmpty()) {
            try {
                ivBookCoverPreview.setPadding(0, 0, 0, 0)
                ivBookCoverPreview.setImageURI(Uri.parse(uriString))
                if (ivBookCoverPreview.drawable == null) {
                    resetToPlaceholderImage()
                }
            } catch (_: Exception) {
                resetToPlaceholderImage()
            }
        } else {
            resetToPlaceholderImage()
        }
    }

    private fun resetToPlaceholderImage() {
        val pad = (8 * resources.displayMetrics.density).toInt()
        ivBookCoverPreview.setPadding(pad, pad, pad, pad)
        ivBookCoverPreview.setImageResource(R.drawable.ic_book_placeholder)
    }

    private fun validateAndSaveBook() {
        // Clear previous errors
        tilBookTitle.error = null
        tilBookAuthor.error = null
        tilBookPrice.error = null
        tilBookCategory.error = null
        tilBookCondition.error = null
        tilBookDescription.error = null

        val title = etBookTitle.text?.toString()?.trim().orEmpty()
        val author = etBookAuthor.text?.toString()?.trim().orEmpty()
        val priceText = etBookPrice.text?.toString()?.trim().orEmpty()
        val category = actvBookCategory.text?.toString()?.trim().orEmpty()
        val condition = actvBookCondition.text?.toString()?.trim().orEmpty()
        val description = etBookDescription.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (title.isEmpty()) {
            tilBookTitle.error = "Please enter the book title"
            hasError = true
        }
        if (author.isEmpty()) {
            tilBookAuthor.error = "Please enter the author name"
            hasError = true
        }

        val priceEgp = priceText.toDoubleOrNull()
        if (priceEgp == null || priceEgp <= 0.0) {
            tilBookPrice.error = "Please enter a valid price greater than 0"
            hasError = true
        }

        if (category.isEmpty()) {
            tilBookCategory.error = "Please select or enter a category"
            hasError = true
        }
        if (condition.isEmpty()) {
            tilBookCondition.error = "Please select the book condition"
            hasError = true
        }
        if (description.isEmpty()) {
            tilBookDescription.error = "Please enter a short description"
            hasError = true
        }

        if (hasError || priceEgp == null) return

        val priceInPiastres = (priceEgp * 100.0).roundToLong()

        btnSaveBook.isEnabled = false
        val app = application as BookStoreApp

        lifecycleScope.launch {
            try {
                val currentUserId = app.sessionManager.userId.first()
                if (currentUserId == null) {
                    Toast.makeText(
                        this@AddEditBookActivity,
                        "Session expired. Please log in again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnSaveBook.isEnabled = true
                    return@launch
                }

                val bookDao = app.database.bookDao()
                val currentBook = existingBook

                if (currentBook == null) {
                    // Add New Book Mode
                    val newBook = Book(
                        title = title,
                        author = author,
                        description = description,
                        priceInPiastres = priceInPiastres,
                        category = category,
                        condition = condition,
                        imageUri = selectedImageUriString,
                        sellerId = currentUserId,
                        isSold = false,
                        createdAt = System.currentTimeMillis()
                    )
                    bookDao.insertBook(newBook)
                    Toast.makeText(
                        this@AddEditBookActivity,
                        "Book added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Edit Existing Book Mode
                    val updatedBook = currentBook.copy(
                        title = title,
                        author = author,
                        description = description,
                        priceInPiastres = priceInPiastres,
                        category = category,
                        condition = condition,
                        imageUri = selectedImageUriString
                    )
                    bookDao.updateBook(updatedBook)
                    Toast.makeText(
                        this@AddEditBookActivity,
                        "Book updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                finish()
            } catch (e: Exception) {
                btnSaveBook.isEnabled = true
                Toast.makeText(
                    this@AddEditBookActivity,
                    "Failed to save book. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
