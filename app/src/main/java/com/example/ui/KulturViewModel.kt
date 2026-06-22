package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Category
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.local.CartItemWithDetails
import com.example.data.local.KulturDatabase
import com.example.data.local.KulturRepository
import com.example.data.local.UserSessionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Screen {
    object Home : Screen
    object Wishlist : Screen
    object Cart : Screen
    object Profile : Screen
    object PurchaseHistory : Screen
    object ShippingAddress : Screen
    object PaymentMethods : Screen
    data class ProductDetail(val product: Product) : Screen
}

class KulturViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KulturRepository
    
    // UI Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // History of back stack for custom back-handling
    private val navigationHistory = mutableListOf<Screen>()

    // Product Search & Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    // Expose all sample products
    val allProducts: List<Product> = ProductRepository.sampleProducts

    // Reactive products list based on filters and searches
    val filteredProducts: StateFlow<List<Product>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedSubcategory
    ) { query, category, subcategory ->
        var list = allProducts
        if (category != null) {
            list = list.filter { it.category == category }
        }
        if (subcategory != null) {
            list = list.filter { it.subcategory.lowercase() == subcategory.lowercase() }
        }
        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.subcategory.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allProducts)

    // Room reactive flows
    val cartItems: StateFlow<List<CartItemWithDetails>>
    val wishlistItems: StateFlow<List<Product>>
    val userSession: StateFlow<UserSessionEntity?>

    // Wishlist quick-look set
    val wishlistProductIds: StateFlow<Set<String>>

    // Purchase checkout visual loading states
    private val _isCheckingOut = MutableStateFlow(false)
    val isCheckingOut: StateFlow<Boolean> = _isCheckingOut.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess: StateFlow<Boolean> = _checkoutSuccess.asStateFlow()

    init {
        val database = KulturDatabase.getDatabase(application)
        repository = KulturRepository(database.kulturDao())

        cartItems = repository.cartWithProducts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        wishlistItems = repository.wishlistProducts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userSession = repository.userSession
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        wishlistProductIds = wishlistItems
            .map { list -> list.map { it.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    }

    // Navigation triggers
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            navigationHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (navigationHistory.isNotEmpty()) {
            _currentScreen.value = navigationHistory.removeAt(navigationHistory.size - 1)
        } else if (_currentScreen.value != Screen.Home) {
            _currentScreen.value = Screen.Home
        }
    }

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        _selectedSubcategory.value = null // reset subcategory on category change
    }

    fun selectSubcategory(subcategory: String?) {
        _selectedSubcategory.value = subcategory
    }

    // Cart transactions
    fun addToCart(product: Product, size: String, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product.id, size, quantity)
        }
    }

    fun decreaseCartItem(productId: String) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, -1)
        }
    }

    fun increaseCartItem(productId: String) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, 1)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    // Shipping Address info state
    private val _shippingAddress = MutableStateFlow(
        ShippingAddressInfo(
            recipientName = "Streetwear Fan",
            street = "Jl. Kemang Raya No. 12B, Mampang Prapatan",
            city = "Jakarta Selatan",
            postalCode = "12730",
            phone = "081234567890"
        )
    )
    val shippingAddress: StateFlow<ShippingAddressInfo> = _shippingAddress.asStateFlow()

    // Saved payment methods state
    private val _paymentMethods = MutableStateFlow(
        listOf(
            SavedPaymentMethod("1", "Streetwear Fan", "**** **** **** 8824", "12/28", "VISA"),
            SavedPaymentMethod("2", "Streetwear Fan", "GOPAY - 081234567890", "", "E-WALLET")
        )
    )
    val paymentMethods: StateFlow<List<SavedPaymentMethod>> = _paymentMethods.asStateFlow()

    // Order history list state
    private val _orderHistory = MutableStateFlow<List<OrderHistoryItem>>(
        listOf(
            OrderHistoryItem(
                orderId = "MTD-98124",
                date = "20 Juni 2026",
                items = emptyList(),
                totalAmount = 249000,
                status = "Selesai"
            )
        )
    )
    val orderHistory: StateFlow<List<OrderHistoryItem>> = _orderHistory.asStateFlow()

    fun saveShippingAddress(address: ShippingAddressInfo) {
        _shippingAddress.value = address
    }

    fun addPaymentMethod(method: SavedPaymentMethod) {
        _paymentMethods.value = _paymentMethods.value + method
    }

    fun removePaymentMethod(id: String) {
        _paymentMethods.value = _paymentMethods.value.filter { it.id != id }
    }

    fun performCheckout() {
        viewModelScope.launch {
            val itemsInCart = cartItems.value
            if (itemsInCart.isNotEmpty()) {
                val subtotal = itemsInCart.sumOf { it.product.price * it.entity.quantity }.toInt()
                val newOrder = OrderHistoryItem(
                    orderId = "MTD-${(10000..99999).random()}",
                    date = "Hari Ini",
                    items = itemsInCart,
                    totalAmount = subtotal,
                    status = "Sedang Diproses"
                )
                _orderHistory.value = listOf(newOrder) + _orderHistory.value
            }
            _isCheckingOut.value = true
            kotlinx.coroutines.delay(1800) // cool loading simulation
            _isCheckingOut.value = false
            _checkoutSuccess.value = true
            repository.clearCart()
            kotlinx.coroutines.delay(2500) // show success banner then clear
            _checkoutSuccess.value = false
        }
    }

    // Wishlist triggers
    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val isCurrentlyLiked = wishlistProductIds.value.contains(product.id)
            repository.toggleWishlist(product.id, !isCurrentlyLiked)
        }
    }

    // Authentication simulation
    fun submitLogin(email: String, name: String) {
        viewModelScope.launch {
            repository.loginUser(email, name)
            navigateTo(Screen.Profile)
        }
    }

    fun submitGoogleLogin() {
        viewModelScope.launch {
            repository.loginUser("google.user@kultur.studio", "Streetwear Enthusiast")
            navigateTo(Screen.Profile)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}

data class OrderHistoryItem(
    val orderId: String,
    val date: String,
    val items: List<CartItemWithDetails>,
    val totalAmount: Int,
    val status: String
)

data class ShippingAddressInfo(
    val recipientName: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val phone: String
)

data class SavedPaymentMethod(
    val id: String,
    val cardHolder: String,
    val cardNumber: String,
    val expiry: String,
    val type: String
)
