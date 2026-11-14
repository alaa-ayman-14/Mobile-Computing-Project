package com.example.bookstore.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.bookstore.data.entity.Book
import com.example.bookstore.data.entity.Favorite

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("""
        DELETE FROM favorites
        WHERE userId = :userId AND bookId = :bookId
    """)
    suspend fun removeFavorite(userId: Long, bookId: Long)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favorites
            WHERE userId = :userId AND bookId = :bookId
        )
    """)
    suspend fun isFavorite(userId: Long, bookId: Long): Boolean

    @Query("""
        SELECT books.* FROM books
        INNER JOIN favorites ON books.id = favorites.bookId
        WHERE favorites.userId = :userId
        ORDER BY favorites.createdAt DESC
    """)
    fun getFavoriteBooks(userId: Long): Flow<List<Book>>
}