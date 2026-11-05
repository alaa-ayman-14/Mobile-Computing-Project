package com.example.bookstore.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bookstore.data.entity.User

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["sellerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sellerId"]),
        Index(value = ["category"]),
        Index(value = ["isSold"])
    ]
)
data class Book(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val description: String,
    val price: Double,
    val category: String,
    val condition: String,
    val imageUri: String? = null,
    val sellerId: Long,
    val isSold: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)