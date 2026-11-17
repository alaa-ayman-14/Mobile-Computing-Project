package com.example.bookstore.ui.listings

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.R
import com.example.bookstore.data.entity.Book
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MyListingsAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val onEditClick: (Book) -> Unit,
    private val onToggleSoldClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<MyListingsAdapter.ListingViewHolder>() {

    class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBookImage: ImageView = itemView.findViewById(R.id.ivListingBookImage)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tvListingStatusBadge)
        val tvTitle: TextView = itemView.findViewById(R.id.tvListingBookTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvListingBookAuthor)
        val tvMeta: TextView = itemView.findViewById(R.id.tvListingMeta)
        val tvDescription: TextView = itemView.findViewById(R.id.tvListingDescription)
        val tvPrice: TextView = itemView.findViewById(R.id.tvListingBookPrice)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEditBook)
        val btnToggleSold: MaterialButton = itemView.findViewById(R.id.btnToggleSold)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDeleteBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_listing_book, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val book = books[position]
        val context = holder.itemView.context

        holder.tvTitle.text = book.title
        holder.tvAuthor.text = "by ${book.author}"
        holder.tvMeta.text = "${book.category} • ${book.condition}"
        holder.tvDescription.text = book.description

        val priceInEgp = book.priceInPiastres / 100.0
        holder.tvPrice.text = String.format(Locale.US, "%.2f EGP", priceInEgp)

        if (!book.imageUri.isNullOrEmpty()) {
            try {
                holder.ivBookImage.setImageURI(Uri.parse(book.imageUri))
                if (holder.ivBookImage.drawable == null) {
                    holder.ivBookImage.setImageResource(R.drawable.ic_book_placeholder)
                }
            } catch (_: Exception) {
                holder.ivBookImage.setImageResource(R.drawable.ic_book_placeholder)
            }
        } else {
            holder.ivBookImage.setImageResource(R.drawable.ic_book_placeholder)
        }

        if (book.isSold) {
            holder.tvStatusBadge.text = "SOLD"
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.error))
            holder.btnToggleSold.text = "Mark Available"
            holder.btnToggleSold.setBackgroundColor(
                ContextCompat.getColor(context, R.color.secondary)
            )
        } else {
            holder.tvStatusBadge.text = "AVAILABLE"
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_dark))
            holder.btnToggleSold.text = "Mark as Sold"
            holder.btnToggleSold.setBackgroundColor(
                ContextCompat.getColor(context, R.color.primary)
            )
        }

        holder.itemView.setOnClickListener { onBookClick(book) }
        holder.btnEdit.setOnClickListener { onEditClick(book) }
        holder.btnToggleSold.setOnClickListener { onToggleSoldClick(book) }
        holder.btnDelete.setOnClickListener { onDeleteClick(book) }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}