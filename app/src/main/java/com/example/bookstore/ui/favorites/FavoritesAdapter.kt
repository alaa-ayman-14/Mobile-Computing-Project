
package com.example.bookstore.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.data.entity.Book
import com.example.bookstore.databinding.ItemFavoriteBookBinding
import android.net.Uri
import com.example.bookstore.R

class FavoritesAdapter(
    private val onRemoveClick: (Book) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private var books: List<Book> = emptyList()

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }


    inner class FavoriteViewHolder(
        val binding: ItemFavoriteBookBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteViewHolder {
        val binding = ItemFavoriteBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun getItemCount(): Int = books.size


    override fun onBindViewHolder(
        holder: FavoriteViewHolder,
        position: Int
    ) {
        val book = books[position]

        val imageView = holder.binding.ivFavoriteBookImage

        imageView.setImageResource(R.drawable.ic_book_placeholder)

        if (!book.imageUri.isNullOrBlank()) {
            try {
                imageView.setImageURI(Uri.parse(book.imageUri))
                if (imageView.drawable == null) {
                    imageView.setImageResource(R.drawable.ic_book_placeholder)
                }
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.ic_book_placeholder)
            }
        }


        holder.binding.tvFavoriteBookTitle.text = book.title
        holder.binding.tvFavoriteBookAuthor.text = book.author

        val priceInEgp = book.priceInPiastres / 100.0
        holder.binding.tvFavoriteBookPrice.text =
            String.format(java.util.Locale.US, "%.2f EGP", priceInEgp)

        holder.binding.btnRemoveFavorite.setOnClickListener {
            onRemoveClick(book)
        }
    }

}
