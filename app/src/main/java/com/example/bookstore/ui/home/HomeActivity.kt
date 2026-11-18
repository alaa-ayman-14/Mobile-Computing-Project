package com.example.bookstore.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.BookStoreApp
import com.example.bookstore.R
import com.example.bookstore.data.database.DatabaseProvider
import com.example.bookstore.data.entity.Book
import com.example.bookstore.ui.SystemNavigationBarStyler
import com.example.bookstore.ui.bookdetails.BookDetailsActivity
import com.example.bookstore.ui.navigation.BottomNavigationHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private enum class SortOption(val label: String) {
        CATEGORY_SECTIONS("Category View"),
        PRICE_LOW_TO_HIGH("Price: Low to High"),
        PRICE_HIGH_TO_LOW("Price: High to Low"),
        TITLE_A_TO_Z("Title: A to Z"),
        TITLE_Z_TO_A("Title: Z to A"),
        AUTHOR_A_TO_Z("Author: A to Z"),
        AUTHOR_Z_TO_A("Author: Z to A"),
        NEWEST_FIRST("Newest First"),
        OLDEST_FIRST("Oldest First")
    }

    private enum class PriceFilter(val label: String, val minPiastres: Long, val maxPiastres: Long) {
        ALL("All Prices", 0L, Long.MAX_VALUE),
        UNDER_200("Under EGP 200", 0L, 19999L),
        BETWEEN_200_AND_400("EGP 200 – 400", 20000L, 40000L),
        OVER_400("Over EGP 400", 40001L, Long.MAX_VALUE)
    }

    private lateinit var bookAdapter: BookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var btnClearSearch: TextView
    private lateinit var tvWelcomeUser: TextView
    private lateinit var categoryScroll: HorizontalScrollView
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var filterSortScroll: HorizontalScrollView
    private lateinit var chipSortBy: Chip
    private lateinit var chipConditionFilter: Chip
    private lateinit var chipPriceFilter: Chip
    private lateinit var chipResetFilters: Chip
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var emptyStateTitle: TextView
    private lateinit var emptyStateSubtitle: TextView

    private var dataJob: Job? = null
    private var submittedSearchQuery = ""
    private var selectedCategory = "All"
    private var selectedCondition = "All"
    private var selectedPriceFilter = PriceFilter.ALL
    private var selectedSort = SortOption.CATEGORY_SECTIONS

    private var availableCategories = listOf<String>()
    private var availableConditions = listOf<String>()
    private var hasAnyBooksInDb = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        SystemNavigationBarStyler.apply(this)
        BottomNavigationHelper.setupBottomNavigation(
            activity = this,
            selectedItemId = R.id.navigation_home
        )

        bindViews()
        setupRecyclerView()
        setupSearchListeners()
        setupFilterAndSortListeners()
        loadWelcomeGreeting()

        observeBooks()
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.booksRecyclerView)
        searchBar = findViewById(R.id.searchBar)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        categoryScroll = findViewById(R.id.categoryScroll)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        filterSortScroll = findViewById(R.id.filterSortScroll)
        chipSortBy = findViewById(R.id.chipSortBy)
        chipConditionFilter = findViewById(R.id.chipConditionFilter)
        chipPriceFilter = findViewById(R.id.chipPriceFilter)
        chipResetFilters = findViewById(R.id.chipResetFilters)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        emptyStateTitle = findViewById(R.id.emptyStateTitle)
        emptyStateSubtitle = findViewById(R.id.emptyStateSubtitle)
    }

    private fun loadWelcomeGreeting() {
        val app = application as BookStoreApp
        lifecycleScope.launch {
            val userId = app.sessionManager.userId.first()
            if (userId != null) {
                val user = app.database.userDao().getUserById(userId)
                if (user != null && !user.fullName.isNullOrBlank()) {
                    tvWelcomeUser.text = "Welcome, ${user.fullName}!"
                }
            }
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            items = emptyList(),
            onCategoryHeaderClick = { clickedCategory ->
                selectedCategory = clickedCategory
                selectCategoryChipByName(clickedCategory)
                observeBooks()
            },
            onBookClick = { bookId ->
                val intent = Intent(this@HomeActivity, BookDetailsActivity::class.java)
                intent.putExtra("bookId", bookId)
                startActivity(intent)
            }
        )

        val gridLayoutManager = GridLayoutManager(this, 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (bookAdapter.isHeader(position)) 2 else 1
            }
        }

        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = bookAdapter
    }

    private fun setupSearchListeners() {
        searchBar.setOnEditorActionListener { _, actionId, event ->
            val isEnterEvent = event != null &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN

            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                isEnterEvent
            ) {
                submittedSearchQuery = searchBar.text.toString().trim()
                hideKeyboard()
                observeBooks()
                true
            } else {
                false
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                btnClearSearch.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                if (text.isEmpty() && submittedSearchQuery.isNotEmpty()) {
                    submittedSearchQuery = ""
                    observeBooks()
                }
            }
        })

        btnClearSearch.setOnClickListener {
            searchBar.setText("")
            submittedSearchQuery = ""
            hideKeyboard()
            observeBooks()
        }
    }

    private fun setupFilterAndSortListeners() {
        chipSortBy.setOnClickListener {
            val options = SortOption.entries.toTypedArray()
            val labels = options.map { it.label }.toTypedArray()
            val currentIndex = options.indexOf(selectedSort).coerceAtLeast(0)

            AlertDialog.Builder(this)
                .setTitle("Sort Books By")
                .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                    selectedSort = options[which]
                    chipSortBy.text = "Sort: ${selectedSort.label} ▾"
                    updateResetChipVisibility()
                    observeBooks()
                    dialog.dismiss()
                }
                .show()
        }

        chipConditionFilter.setOnClickListener {
            val options = listOf("All") + availableConditions
            val labels = options.map { if (it == "All") "All Conditions" else it }.toTypedArray()
            val currentIndex = options.indexOf(selectedCondition).coerceAtLeast(0)

            AlertDialog.Builder(this)
                .setTitle("Filter by Condition")
                .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                    selectedCondition = options[which]
                    chipConditionFilter.text = "Condition: ${selectedCondition} ▾"
                    updateResetChipVisibility()
                    observeBooks()
                    dialog.dismiss()
                }
                .show()
        }

        chipPriceFilter.setOnClickListener {
            val options = PriceFilter.entries.toTypedArray()
            val labels = options.map { it.label }.toTypedArray()
            val currentIndex = options.indexOf(selectedPriceFilter).coerceAtLeast(0)

            AlertDialog.Builder(this)
                .setTitle("Filter by Price Range")
                .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                    selectedPriceFilter = options[which]
                    chipPriceFilter.text = if (selectedPriceFilter == PriceFilter.ALL) {
                        "Price: All ▾"
                    } else {
                        "${selectedPriceFilter.label} ▾"
                    }
                    updateResetChipVisibility()
                    observeBooks()
                    dialog.dismiss()
                }
                .show()
        }

        chipResetFilters.setOnClickListener {
            searchBar.setText("")
            submittedSearchQuery = ""
            selectedCategory = "All"
            selectedCondition = "All"
            selectedPriceFilter = PriceFilter.ALL
            selectedSort = SortOption.CATEGORY_SECTIONS

            chipSortBy.text = "Sort: ${selectedSort.label} ▾"
            chipConditionFilter.text = "Condition: All ▾"
            chipPriceFilter.text = "Price: All ▾"
            selectCategoryChipByName("All")
            updateResetChipVisibility()
            observeBooks()
        }
    }

    private fun observeBooks() {
        dataJob?.cancel()
        dataJob = lifecycleScope.launch {
            val database = DatabaseProvider.getDatabase(applicationContext)
            val bookDao = database.bookDao()

            val allAvailableFlow = bookDao.getAvailableBooks()
            val searchedFlow = if (submittedSearchQuery.isBlank()) {
                allAvailableFlow
            } else {
                bookDao.searchBooks(submittedSearchQuery)
            }

            combine(allAvailableFlow, searchedFlow) { allBooks, searchedBooks ->
                Pair(allBooks, searchedBooks)
            }.collect { (allBooks, searchedBooks) ->
                hasAnyBooksInDb = allBooks.isNotEmpty()

                val dbCategories = allBooks.map { it.category.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()

                val dbConditions = allBooks.map { it.condition.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()

                if (dbCategories != availableCategories) {
                    availableCategories = dbCategories
                    if (selectedCategory != "All" && selectedCategory !in availableCategories) {
                        selectedCategory = "All"
                    }
                    rebuildDynamicCategoryChips(availableCategories)
                }

                availableConditions = dbConditions
                if (selectedCondition != "All" && selectedCondition !in availableConditions) {
                    selectedCondition = "All"
                    chipConditionFilter.text = "Condition: All ▾"
                }

                if (!hasAnyBooksInDb) {
                    categoryScroll.visibility = View.GONE
                    filterSortScroll.visibility = View.GONE
                    recyclerView.visibility = View.GONE

                    emptyStateContainer.visibility = View.VISIBLE
                    emptyStateTitle.text = "No books available at the moment"
                    emptyStateSubtitle.text = "Come back later"
                    bookAdapter.updateItems(emptyList())
                    return@collect
                }

                categoryScroll.visibility = View.VISIBLE
                filterSortScroll.visibility = View.VISIBLE

                val filteredBooks = searchedBooks.filter { book ->
                    val matchesCategory = selectedCategory == "All" ||
                            book.category.equals(selectedCategory, ignoreCase = true)
                    val matchesCondition = selectedCondition == "All" ||
                            book.condition.equals(selectedCondition, ignoreCase = true)
                    val matchesPrice = book.priceInPiastres in
                            selectedPriceFilter.minPiastres..selectedPriceFilter.maxPiastres

                    matchesCategory && matchesCondition && matchesPrice
                }

                if (filteredBooks.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateContainer.visibility = View.VISIBLE
                    emptyStateTitle.text = "No matching books found"
                    emptyStateSubtitle.text = "Try a different search term or reset your filters"
                    bookAdapter.updateItems(emptyList())
                    return@collect
                }

                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val displayItems = buildDisplayList(filteredBooks)
                bookAdapter.updateItems(displayItems)
            }
        }
    }

    private fun buildDisplayList(books: List<Book>): List<HomeListItem> {
        val useSectionedHomeView = submittedSearchQuery.isBlank() &&
                selectedCategory == "All" &&
                selectedSort == SortOption.CATEGORY_SECTIONS

        if (useSectionedHomeView) {
            val grouped = books.groupBy { it.category }
            val sectionedItems = mutableListOf<HomeListItem>()
            for ((categoryName, categoryBooks) in grouped.toSortedMap(String.CASE_INSENSITIVE_ORDER)) {
                sectionedItems.add(HomeListItem.CategoryHeader(categoryName, categoryBooks.size))
                categoryBooks.sortedByDescending { it.createdAt }.forEach { book ->
                    sectionedItems.add(HomeListItem.BookItem(book))
                }
            }
            return sectionedItems
        }

        val sortedBooks = when (selectedSort) {
            SortOption.CATEGORY_SECTIONS,
            SortOption.NEWEST_FIRST -> books.sortedByDescending { it.createdAt }
            SortOption.OLDEST_FIRST -> books.sortedBy { it.createdAt }
            SortOption.PRICE_LOW_TO_HIGH -> books.sortedBy { it.priceInPiastres }
            SortOption.PRICE_HIGH_TO_LOW -> books.sortedByDescending { it.priceInPiastres }
            SortOption.TITLE_A_TO_Z -> books.sortedBy { it.title.lowercase() }
            SortOption.TITLE_Z_TO_A -> books.sortedByDescending { it.title.lowercase() }
            SortOption.AUTHOR_A_TO_Z -> books.sortedBy { it.author.lowercase() }
            SortOption.AUTHOR_Z_TO_A -> books.sortedByDescending { it.author.lowercase() }
        }

        return sortedBooks.map { HomeListItem.BookItem(it) }
    }

    private fun rebuildDynamicCategoryChips(categories: List<String>) {
        categoryChipGroup.setOnCheckedStateChangeListener(null)
        categoryChipGroup.removeAllViews()

        val allOptions = listOf("All") + categories
        for (cat in allOptions) {
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = cat
                isCheckable = true
                isChecked = cat.equals(selectedCategory, ignoreCase = true)
                chipBackgroundColor = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        ContextCompat.getColor(context, R.color.primary_dark),
                        ContextCompat.getColor(context, R.color.surface)
                    )
                )
                setTextColor(
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(
                            ContextCompat.getColor(context, R.color.on_primary),
                            ContextCompat.getColor(context, R.color.text_primary)
                        )
                    )
                )
                chipStrokeWidth = 1f * resources.displayMetrics.density
                chipStrokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.border)
                )
            }
            categoryChipGroup.addView(chip)
        }

        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val checkedChip = group.findViewById<Chip>(checkedId)
            if (checkedChip != null) {
                selectedCategory = checkedChip.text.toString()
                updateResetChipVisibility()
                observeBooks()
            }
        }
    }

    private fun selectCategoryChipByName(categoryName: String) {
        for (i in 0 until categoryChipGroup.childCount) {
            val chip = categoryChipGroup.getChildAt(i) as? Chip
            if (chip != null && chip.text.toString().equals(categoryName, ignoreCase = true)) {
                chip.isChecked = true
                break
            }
        }
        updateResetChipVisibility()
    }

    private fun updateResetChipVisibility() {
        val isAnyFilterActive = submittedSearchQuery.isNotEmpty() ||
                selectedCategory != "All" ||
                selectedCondition != "All" ||
                selectedPriceFilter != PriceFilter.ALL ||
                selectedSort != SortOption.CATEGORY_SECTIONS

        chipResetFilters.visibility = if (isAnyFilterActive) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(searchBar.windowToken, 0)
        searchBar.clearFocus()
    }
}