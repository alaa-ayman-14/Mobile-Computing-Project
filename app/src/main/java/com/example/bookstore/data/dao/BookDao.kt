package com.example.bookstore.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bookstore.data.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBookById(bookId: Long): Book?

    @Query("SELECT * FROM books WHERE isSold = 0 ORDER BY createdAt DESC")
    fun getAvailableBooks(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books
        WHERE isSold = 0
        AND (title LIKE '%' || :query || '%'
        OR author LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchBooks(query: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getBooksBySeller(sellerId: Long): Flow<List<Book>>

    @Query("UPDATE books SET isSold = :isSold WHERE id = :bookId")
    suspend fun updateSoldStatus(bookId: Long, isSold: Boolean)
}