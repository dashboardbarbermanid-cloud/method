package com.example.data.local

import com.example.data.Product
import com.example.data.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class KulturRepository(private val dao: KulturDao) {

    // Retrieve full product listing
    val allProducts: List<Product> = ProductRepository.sampleProducts

    // Shopping Cart logic: Merges Database items with memory product catalog
    val cartWithProducts: Flow<List<CartItemWithDetails>> = dao.getAllCart().map { cartEntities ->
        cartEntities.mapNotNull { entity ->
            val product = allProducts.find { it.id == entity.productId }
            if (product != null) {
                CartItemWithDetails(product, entity)
            } else {
                null
            }
        }
    }

    // Wishlist logic: Merges Database item IDs with product model info
    val wishlistProducts: Flow<List<Product>> = dao.getAllWishlist().map { wishlistEntities ->
        val likedIds = wishlistEntities.map { it.productId }.toSet()
        allProducts.filter { it.id in likedIds }
    }

    // User authentication status session
    val userSession: Flow<UserSessionEntity?> = dao.getUserSession()

    // Add / Update item in Cart
    suspend fun addToCart(productId: String, size: String, quantity: Int) {
        dao.insertOrUpdateCart(CartEntity(productId, quantity, size))
    }

    // Increase / Decrease quantities directly
    suspend fun updateCartQuantity(productId: String, delta: Int) {
        val currentItems = dao.getAllCart().firstOrNull() ?: return
        val item = currentItems.find { it.productId == productId }
        if (item != null) {
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                dao.deleteCartItem(productId)
            } else {
                dao.insertOrUpdateCart(item.copy(quantity = newQty))
            }
        }
    }

    suspend fun removeFromCart(productId: String) {
        dao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    // Set item Favorite status
    suspend fun toggleWishlist(productId: String, isLiked: Boolean) {
        if (isLiked) {
            dao.insertWishlist(WishlistEntity(productId))
        } else {
            dao.deleteWishlist(productId)
        }
    }

    // Save user profile state
    suspend fun loginUser(email: String, name: String) {
        dao.saveUserSession(UserSessionEntity(email = email, name = name, isLoggedIn = true))
    }

    // Clear session status
    suspend fun logout() {
        dao.clearUserSession()
    }
}

data class CartItemWithDetails(
    val product: Product,
    val entity: CartEntity
)
