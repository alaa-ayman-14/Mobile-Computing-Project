
package com.example.bookstore.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val phoneNumber: String,
    val profileImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
