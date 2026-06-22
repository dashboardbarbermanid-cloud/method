package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val selectedSize: String
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey val productId: String
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val email: String,
    val name: String,
    val isLoggedIn: Boolean = false
)
