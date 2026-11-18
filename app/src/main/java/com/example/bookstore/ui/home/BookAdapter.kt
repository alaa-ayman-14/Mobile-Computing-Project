package com.example.bookstore.ui.home

import android.graphics.Typeface
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.R
import com.example.bookstore.data.entity.Book
import java.util.Locale

sealed class HomeListItem {
    data class CategoryHeader(val categoryName: String, val bookCount: Int) : HomeListItem()
    data class BookItem(val book: Book) : HomeListItem()
}
class BookAdapter(
    private var items: List<HomeListItem>,
    private val onCategoryHeaderClick: (String) -> Unit = {},
    private val onBookClick: (Long) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_BOOK = 1
    }
    class HeaderViewHolder(
        val container: LinearLayout,
        val titleText: TextView,
        val actionText: TextView
    ) : RecyclerView.ViewHolder(container)
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        val bookCategoryBadge: TextView = itemView.findViewById(R.id.bookCategoryBadge)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        val bookCondition: TextView = itemView.findViewById(R.id.bookCondition)
        val bookPrice: TextView = itemView.findViewById(R.id.bookPrice)
    }
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeListItem.CategoryHeader -> VIEW_TYPE_HEADER
            is HomeListItem.BookItem -> VIEW_TYPE_BOOK
        }
    }
    fun isHeader(position: Int): Boolean {
        return position in items.indices && items[position] is HomeListItem.CategoryHeader
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        if (viewType == VIEW_TYPE_HEADER) {
            val density = context.resources.displayMetrics.density
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * density).toInt()
                    bottomMargin = (6 * density).toInt()
                    marginStart = (6 * density).toInt()
                    marginEnd = (6 * density).toInt()
                }
            }
            val titleText = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTypeface(Typeface.SERIF, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
            val actionText = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
            container.addView(titleText)
            container.addView(actionText)
            return HeaderViewHolder(container, titleText, actionText)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.book_card, parent, false)
            return BookViewHolder(view)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeListItem.CategoryHeader -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.titleText.text = item.categoryName
                headerHolder.actionText.text = "${item.bookCount} available  ›"
                headerHolder.container.setOnClickListener {
                    onCategoryHeaderClick(item.categoryName)
                }
            }
            is HomeListItem.BookItem -> {
                val bookHolder = holder as BookViewHolder
                val book = item.book
                bookHolder.bookCategoryBadge.text = book.category
                bookHolder.bookTitle.text = book.title
                bookHolder.bookAuthor.text = "by ${book.author}"
                bookHolder.bookCondition.text = "Condition: ${book.condition}"
                val priceInEgp = book.priceInPiastres / 100.0
                bookHolder.bookPrice.text = String.format(Locale.US, "EGP %.2f", priceInEgp)

                if (!book.imageUri.isNullOrEmpty()) {
                    try {
                        bookHolder.bookImage.setImageURI(Uri.parse(book.imageUri))
                        if (bookHolder.bookImage.drawable == null) {
                            bookHolder.bookImage.setImageResource(R.drawable.ic_book_placeholder)
                        }
                    } catch (e: Exception) {
                        bookHolder.bookImage.setImageResource(R.drawable.ic_book_placeholder)
                    }
                } else {
                    bookHolder.bookImage.setImageResource(R.drawable.ic_book_placeholder)
                }
                bookHolder.itemView.setOnClickListener {
                    onBookClick(book.id)
                }
            }
        }
    }
    override fun getItemCount(): Int = items.size
    fun updateItems(newItems: List<HomeListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}