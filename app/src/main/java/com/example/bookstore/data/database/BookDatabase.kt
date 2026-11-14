package com.example.bookstore.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookstore.data.dao.BookDao
import com.example.bookstore.data.dao.FavoriteDao
import com.example.bookstore.data.dao.UserDao
import com.example.bookstore.data.entity.Book
import com.example.bookstore.data.entity.Favorite
import com.example.bookstore.data.entity.User

@Database(
    entities = [
        User::class,
        Book::class,
        Favorite::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun bookDao(): BookDao

    abstract fun favoriteDao(): FavoriteDao
}