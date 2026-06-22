package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Category
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.local.CartItemWithDetails
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

// Navigation Tab Configuration
enum class NavItem(val title: String, val icon: ImageVector, val screen: Screen) {
    HOME("Explore", Icons.Default.GridView, Screen.Home),
    WISHLIST("Loved", Icons.Default.FavoriteBorder, Screen.Wishlist),
    CART("Cart", Icons.Default.LocalMall, Screen.Cart),
    PROFILE("Account", Icons.Default.PersonOutline, Screen.Profile)
}

@Composable
fun KulturAppContainer(viewModel: KulturViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistIds by viewModel.wishlistProductIds.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    val totalCartCount = cartItems.sumOf { it.entity.quantity }

    // Screen Dimensions Configuration for Adaptive Mode
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Tablet Sidebar Navigation (Floating on desktop side)
            if (isTablet) {
                FloatingSidebar(
                    currentScreen = currentScreen,
                    totalCartCount = totalCartCount,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }

            // Main Content Area with EdgeToEdge transitions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetY = { 300 }
                        ) + fadeIn(animationSpec = tween(250)) togetherWith
                        slideOutVertically(
                            animationSpec = tween(200),
                            targetOffsetY = { -200 }
                        ) + fadeOut(animationSpec = tween(150))
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "AppScreenTransition"
                ) { targetScreen ->
                    when (targetScreen) {
                        is Screen.Home -> ExploreScreen(viewModel = viewModel)
                        is Screen.Wishlist -> WishlistScreen(viewModel = viewModel)
                        is Screen.Cart -> CartScreen(viewModel = viewModel)
                        is Screen.Profile -> {
                            if (userSession?.isLoggedIn == true) {
                                ProfileScreen(viewModel = viewModel)
                            } else {
                                LoginScreen(viewModel = viewModel)
                            }
                        }
                        is Screen.ProductDetail -> ProductDetailScreen(
                            product = targetScreen.product,
                            viewModel = viewModel
                        )
                        is Screen.PurchaseHistory -> PurchaseHistoryScreen(viewModel = viewModel)
                        is Screen.ShippingAddress -> ShippingAddressScreen(viewModel = viewModel)
                        is Screen.PaymentMethods -> PaymentMethodsScreen(viewModel = viewModel)
                    }
                }

                // Mobile Bottom Floating Navigation overlay
                val hideBottomBar = currentScreen is Screen.ProductDetail || 
                                   currentScreen is Screen.PurchaseHistory || 
                                   currentScreen is Screen.ShippingAddress || 
                                   currentScreen is Screen.PaymentMethods
                if (!isTablet && !hideBottomBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        FloatingBottomNavigation(
                            currentScreen = currentScreen,
                            totalCartCount = totalCartCount,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// DESKTOP / TABLET SIDEBAR NAVIGATION
// ==========================================
@Composable
fun FloatingSidebar(
    currentScreen: Screen,
    totalCartCount: Int,
    onNavigate: (Screen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Brand Header with mini logo illustration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AllInclusive,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "KULTUR",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "STUDIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Menu navigation items list
                NavItem.entries.forEach { navItem ->
                    val isSelected = when (navItem.screen) {
                        is Screen.Home -> currentScreen is Screen.Home
                        is Screen.Wishlist -> currentScreen is Screen.Wishlist
                        is Screen.Cart -> currentScreen is Screen.Cart
                        is Screen.Profile -> currentScreen is Screen.Profile
                        else -> false
                    }

                    // Active highlight animations
                    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "TabScale")
                    val pillWidth by animateDpAsState(if (isSelected) 8.dp else 0.dp, label = "TabPill")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable { onNavigate(navItem.screen) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("sidebar_${navItem.title.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(pillWidth)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        if (isSelected) Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = navItem.icon,
                            contentDescription = navItem.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = navItem.title,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.7f),
                            modifier = Modifier.weight(1f)
                        )

                        // Cart count custom badge
                        if (navItem == NavItem.CART && totalCartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = totalCartCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Simple footer banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Vibe Out.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Styling & apparel made effortless.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// MOBILE FLOATING NAVIGATION DOCK
// ==========================================
@Composable
fun FloatingBottomNavigation(
    currentScreen: Screen,
    totalCartCount: Int,
    onNavigate: (Screen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(20.dp, RoundedCornerShape(30.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem.entries.forEach { navItem ->
                val isSelected = when (navItem.screen) {
                    is Screen.Home -> currentScreen is Screen.Home
                    is Screen.Wishlist -> currentScreen is Screen.Wishlist
                    is Screen.Cart -> currentScreen is Screen.Cart
                    is Screen.Profile -> currentScreen is Screen.Profile
                    else -> false
                }

                // Bouncing active state indicator
                val bounceScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "NavBounce"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(bounceScale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(navItem.screen) }
                        .testTag("nav_${navItem.title.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                modifier = Modifier.size(24.dp)
                            )

                            if (navItem == NavItem.CART && totalCartCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = totalCartCount.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = navItem.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Micro glowing indicator dot
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// EXPLORE SCREEN (HOME)
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(viewModel: KulturViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState()
    val wishlistIds by viewModel.wishlistProductIds.collectAsState()

    // Slide and cascade reveal animations triggers
    val state = remember { MutableTransitionState(false) }.apply { targetState = true }

    // Subcategories lists depending on active core category
    val subcategories = remember(selectedCategory) {
        when (selectedCategory) {
            Category.HAIR -> listOf("Pomade Water Based", "Clay", "Powder", "Hair Spray", "Hairtonik", "Shampo")
            Category.FASHION -> listOf("T-shirt", "Hoodie", "Crewneck", "Sweater", "Cardigan", "Celana", "Sepatu", "Accesories", "Topi", "Kacamata")
            else -> listOf("Pomade", "Clay", "T-shirt", "Hoodie", "Shampo", "Sepatu", "Kacamata")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp), // clear bottom nav padding
        contentPadding = PaddingValues(16.dp)
    ) {
        // App header / logo branding
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KULTUR",
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ELITE GROOMING & FASHION",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp
                    )
                }

                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search bar custom card with interactive autocomplete suggestions
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    placeholder = { Text("Cari pomade, hoodie, celana, clay...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(0.4f),
                    )
                )

                // Autocomplete Suggestions overlay
                if (searchQuery.isNotEmpty()) {
                    val allSuggBase = listOf("Pomade", "Clay", "Powder", "Hair Spray", "Hairtonik", "Shampo", "T-shirt", "Hoodie", "Sweater", "Celana", "Sepatu", "Topi", "Accesories")
                    val matchingSuggs = allSuggBase.filter { 
                        it.contains(searchQuery, ignoreCase = true) && !it.equals(searchQuery, ignoreCase = true)
                    }
                    if (matchingSuggs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                                Text(
                                    text = "Saran Pencarian:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                                matchingSuggs.take(4).forEach { sug ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setSearchQuery(sug) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = sug,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Premium Hero Banner with generated artwork
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                // Determine layout image background depending on selected core category
                val bannerRes = if (selectedCategory == Category.HAIR) {
                    R.drawable.img_grooming_banner
                } else {
                    R.drawable.img_fashion_banner
                }

                Image(
                    painter = painterResource(id = bannerRes),
                    contentDescription = "Hero banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark subtle layer tint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.85f)),
                                startY = 100f
                            )
                        )
                )

                // Promo tag + labels
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "NEW COLECTION",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedCategory == Category.HAIR) "KULTUR HAIR COSMETICS" else "STREETWEAR SEASON DROP",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Upgrade penataan gaya & outfit-mu dengan sentuhan modern terbaik.",
                        color = Color.White.copy(0.7f),
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Category Selection Row pills
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Kategori Utama",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                // All Products tab
                val isAllSelected = selectedCategory == null
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.selectCategory(null) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Semua",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Grooming tab
                val isHairSelected = selectedCategory == Category.HAIR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isHairSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.selectCategory(Category.HAIR) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Spa,
                            contentDescription = null,
                            tint = if (isHairSelected) Color.Black else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Perawatan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHairSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Fashion tab
                val isFashionSelected = selectedCategory == Category.FASHION
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isFashionSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.selectCategory(Category.FASHION) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = if (isFashionSelected) Color.Black else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fashion",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFashionSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Subcategory slider
        item {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear filter item
                item {
                    val isSubcatAll = selectedSubcategory == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSubcatAll) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSubcatAll) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectSubcategory(null) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Semua Sub",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSubcatAll) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        )
                    }
                }

                items(subcategories) { subcat ->
                    val isSubSelected = selectedSubcategory?.lowercase() == subcat.lowercase()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSubSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSubSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectSubcategory(subcat) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = subcat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        )
                    }
                }
            }
        }

        // Section label for product grid header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) {
                        "Hasil Pencarian untuk \"$searchQuery\" (${filteredProducts.size})"
                    } else {
                        "Lini Koleksi (${filteredProducts.size})"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )

                if (searchQuery.isNotEmpty() || selectedCategory != null || selectedSubcategory != null) {
                    Text(
                        text = "Reset Semua",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.setSearchQuery("")
                            viewModel.selectCategory(null)
                            viewModel.selectSubcategory(null)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Empty state checker
        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Barang tidak ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Cari kata kunci lain atau ubah filter pencarian Anda.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // High-fidelity staggered dynamic item lists
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .heightIn(max = 2000.dp), // let lazy column scroll it smoothly
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = false // disable internal scroll to yield outer lazy column
                ) {
                    itemsIndexed(filteredProducts) { index, product ->
                        // Entry delay staggered animations
                        ProductCard(
                            product = product,
                            index = index,
                            isLiked = wishlistIds.contains(product.id),
                            onCardClick = { viewModel.navigateTo(Screen.ProductDetail(product)) },
                            onLikeClick = { viewModel.toggleWishlist(product) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// DETAILED PRODUCT CARD
// ==========================================
@Composable
fun ProductCard(
    product: Product,
    index: Int,
    isLiked: Boolean,
    onCardClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    // Elegant fade in slide up entry card
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L) // staggered offset
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { 80 }) + fadeIn(animationSpec = tween(300)),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() }
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .testTag("product_card_${product.id}"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background visual asset generator placeholder/colors
                val productBg = when (product.category) {
                    Category.HAIR -> Brush.verticalGradient(listOf(Color(0xFF1E2818), Color(0xFF141513)))
                    Category.FASHION -> Brush.verticalGradient(listOf(Color(0xFF1E1F28), Color(0xFF141513)))
                }

                // Main card picture slot
                Box(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                        .background(productBg),
                    contentAlignment = Alignment.Center
                ) {
                    // Standard premium vector icon styling since images are not real photos
                    val iconVector = when (product.subcategory.lowercase()) {
                        "pomade water based", "clay" -> Icons.Default.Spa
                        "powder", "hair spray" -> Icons.Default.AutoAwesome
                        "hairtonik" -> Icons.Default.InvertColors
                        "shampo" -> Icons.Default.WaterDrop
                        "t-shirt" -> Icons.Default.Checkroom
                        "hoodie", "crewneck", "sweater" -> Icons.Default.Layers
                        "cardigan" -> Icons.Default.Layers
                        "celana" -> Icons.Default.Checkroom
                        "sepatu" -> Icons.Default.DirectionsRun
                        "accesories" -> Icons.Default.AutoAwesome
                        "topi" -> Icons.Default.Face
                        else -> Icons.Default.Tag
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.06f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = if (product.category == Category.HAIR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Top Left tag badge
                    product.tag?.let {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = it,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Favorite heartbeat icon trigger
                IconButton(
                    onClick = { onLikeClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .testTag("like_${product.id}")
                ) {
                    val scale by animateFloatAsState(if (isLiked) 1.2f else 1.0f, label = "LikeScale")
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like item",
                        tint = if (isLiked) Color.Red else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(20.dp)
                            .scale(scale)
                    )
                }
            }

            // Info details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.subcategory.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rp ${String.format("%,d", product.price).replace(',', '.')}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = product.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// WISHLIST SCREEN
// ==========================================
@Composable
fun WishlistScreen(viewModel: KulturViewModel) {
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "Koleksi Favorit Kamu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Daftar barang impian rancangan style yang kamu minati.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (wishlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Wishlist masih kosong",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Jelajahi list explorer dan tap ikon hati di produk yang kamu incar.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Temukan Produk", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(wishlistItems) { index, product ->
                    ProductCard(
                        product = product,
                        index = index,
                        isLiked = true,
                        onCardClick = { viewModel.navigateTo(Screen.ProductDetail(product)) },
                        onLikeClick = { viewModel.toggleWishlist(product) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SHOPPING CART SCREEN
// ==========================================
@Composable
fun CartScreen(viewModel: KulturViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val isCheckingOut by viewModel.isCheckingOut.collectAsState()
    val checkoutSuccess by viewModel.checkoutSuccess.collectAsState()

    val totalSubtotal = cartItems.sumOf { it.product.price * it.entity.quantity }
    val shippingFee = if (totalSubtotal > 300000) 0L else 18000L
    val finalTotal = totalSubtotal + shippingFee

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "Tas Belanja KULTUR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Selesaikan pembayaran untuk produk kurasi idamanmu.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            if (cartItems.isEmpty() && !checkoutSuccess) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalMall,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Keranjang belanjamu kosong",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ayo isi dengan water-based pomade, clay mewah, atau t-shirt oversized kurasi kami.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 30.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Home) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Belanja Sekarang", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (!checkoutSuccess) {
                // Cart listing
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(item = item, viewModel = viewModel)
                    }
                }

                // Billing receipt breakdown details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Produk", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${String.format("%,d", totalSubtotal).replace(',', '.')}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pengiriman", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (shippingFee == 0L) "GRATIS" else "Rp ${String.format("%,d", shippingFee).replace(',', '.')}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (shippingFee == 0L) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Bayar", fontSize = 15.sp, fontWeight = FontWeight.Black)
                            Text(
                                text = "Rp ${String.format("%,d", finalTotal).replace(',', '.')}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.performCheckout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("BAYAR SEKARANG", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }

        // Processing Loading HUD Card overlay
        if (isCheckingOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Memproses Pembayaran",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Menghubungkan ke gateway aman KULTUR...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Checkout Success holographic Ticket Confetti Screen
        if (checkoutSuccess) {
            CheckoutSuccessScreen()
        }
    }
}

// ==========================================
// CART ITEM LIST COMPONENT
// ==========================================
@Composable
fun CartItemRow(item: CartItemWithDetails, viewModel: KulturViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini static thumbnail styling
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(0.04f)),
                contentAlignment = Alignment.Center
            ) {
                val subcatIcon = when (item.product.subcategory.lowercase()) {
                    "pomade water based", "clay" -> Icons.Default.Spa
                    "t-shirt" -> Icons.Default.Checkroom
                    "hoodie", "crewneck" -> Icons.Default.Layers
                    "sepatu" -> Icons.Default.DirectionsRun
                    else -> Icons.Default.Tag
                }
                Icon(
                    imageVector = subcatIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info block
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.subcategory.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Size: ${item.entity.selectedSize}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Rp ${String.format("%,d", item.product.price * item.entity.quantity).replace(',', '.')}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Up-down increment counts
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    IconButton(
                        onClick = { viewModel.decreaseCartItem(item.product.id) },
                        modifier = Modifier.size(28.dp).testTag("decrease_${item.product.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = item.entity.quantity.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { viewModel.increaseCartItem(item.product.id) },
                        modifier = Modifier.size(28.dp).testTag("increase_${item.product.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hapus",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.removeFromCart(item.product.id) }
                        .padding(2.dp)
                )
            }
        }
    }
}

// ==========================================
// CHECKOUT SUCCESS CONFETTI TICKET VISUAL
// ==========================================
@Composable
fun CheckoutSuccessScreen() {
    // Canvas particles coordinates for visual celebration!
    val infiniteTransition = rememberInfiniteTransition(label = "ConfettiTransition")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ConfettiTicker"
    )

    val randomParticles = remember {
        List(40) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 1.5f - 0.5f
            ) to Color(
                red = Random.nextFloat(),
                green = Random.nextFloat() * 0.5f + 0.5f, // highlight greens/yellows
                blue = Random.nextFloat()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas renderer
        Canvas(modifier = Modifier.fillMaxSize()) {
            randomParticles.forEach { (pos, color) ->
                val currentY = ((pos.y + (ticker / 600f)) % 1f) * size.height
                val currentX = pos.x * size.width
                drawCircle(
                    color = color,
                    radius = 8f,
                    center = Offset(currentX, currentY)
                )
            }
        }

        // Holographic receipt card representation
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .width(320.dp)
                .padding(20.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success checkmark",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TRANSAKSI BERHASIL!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "No. Resi: KTR-${Random.nextInt(100000, 999999)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = "TERIMA KASIH TELAH BERBELANJA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Pesanan kurasi premium style Anda telah diteruskan ke kurir ekspedisi. Kami akan segera mengirimkan nomor pelacak ke email terdaftar Anda.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ESTIMASI TIBA: 2 - 3 HARI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ==========================================
// INTERACTIVE DETAILED SCREEN
// ==========================================
@Composable
fun ProductDetailScreen(product: Product, viewModel: KulturViewModel) {
    var selectedSize by remember { mutableStateOf(if (product.sizes.isNotEmpty()) product.sizes[0] else "Default") }
    var detailQuantity by remember { mutableStateOf(1) }
    var showAddedToast by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableStateOf(1.0f) }

    val wishlistIds by viewModel.wishlistProductIds.collectAsState()
    val isLiked = wishlistIds.contains(product.id)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            // Custom high backbar navigation header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF222C1B), Color(0xFF141513))
                        )
                    )
            ) {
                // Header icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.4f))
                            .clickable { viewModel.navigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.4f))
                            .clickable { viewModel.toggleWishlist(product) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Decorative category visual symbols (Zoomable)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val vectorItem = if (product.category == Category.HAIR) Icons.Default.Spa else Icons.Default.Checkroom

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.04f))
                            .graphicsLayer(
                                scaleX = zoomScale,
                                scaleY = zoomScale
                            )
                            .clickable {
                                zoomScale = when (zoomScale) {
                                    1.0f -> 1.5f
                                    1.5f -> 2.0f
                                    2.0f -> 2.5f
                                    else -> 1.0f
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorItem,
                            contentDescription = null,
                            tint = if (product.category == Category.HAIR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Zoom Pill Control
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable {
                                zoomScale = when (zoomScale) {
                                    1.0f -> 1.5f
                                    1.5f -> 2.0f
                                    2.0f -> 2.5f
                                    else -> 1.0f
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ZOOM: ${zoomScale}x",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Bottom Overlay visual gradients
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // Description body parts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Category + Star ratings label row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.subcategory.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating} / 5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Product title
                Text(
                    text = product.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rupiah Pricing tag highlight
                Text(
                    text = "Rp ${String.format("%,d", product.price).replace(',', '.')}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Interactive Sizing list selection
                if (product.sizes.isNotEmpty()) {
                    Text(
                        text = "PILIH UKURAN / UKURAN KEMASAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        product.sizes.forEach { size ->
                            val isSelectedSize = size == selectedSize
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelectedSize) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedSize = size }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelectedSize) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Interactive Details product description
                Text(
                    text = "DESKRIPSI PRODUK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Specifications parameters key-value map tables
                if (product.details.isNotEmpty()) {
                    Text(
                        text = "SPESIFIKASI DETAIL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 120.dp) // clear bottom drawer
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            product.details.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = value,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom purchase sliding control dock overlay drawer
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Up Down Modifier controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (detailQuantity > 1) detailQuantity-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = detailQuantity.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = { detailQuantity++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Primary Add To Cart Button
                Button(
                    onClick = {
                        viewModel.addToCart(product, selectedSize, detailQuantity)
                        showAddedToast = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_to_cart_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalMall, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MASUKKAN TAS",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Added Success Micro Toast notification overlay popup
        AnimatedVisibility(
            visible = showAddedToast,
            enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -100 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Berhasil masuk tas!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        LaunchedEffect(showAddedToast) {
                            if (showAddedToast) {
                                delay(2000)
                                showAddedToast = false
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ACCOUNT / PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(viewModel: KulturViewModel) {
    val userSession by viewModel.userSession.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Profil KULTUR Anda",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
        }

        // Hero Brand Graphic for Premium Polish
        item {
            Image(
                painter = painterResource(id = R.drawable.img_brand_logo),
                contentDescription = "KULTUR Brand Icon Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp)),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userSession?.name ?: "Streetwear Fan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = userSession?.email ?: "fan@kultur.studio",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                fontFamily = FontFamily.Monospace
            )
        }

        // Membership Gold Badge
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "GOLD MEMBERSHIP TIER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }

        // Dashboard info
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sistem Membership & Benefit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BenefitRow(benefit = "Bebas Biaya Pengiriman Seluruh Jawa di atas Rp 300.000")
                    BenefitRow(benefit = "Akses Prioritas ke Drop T-Shirt & Hoodie Terbatas")
                    BenefitRow(benefit = "Grooming Konsultasi Rambut Online via AI Chat")
                }
            }
        }

        // Settings items
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column {
                    ProfileMenuRow(icon = Icons.Default.Inventory, title = "Riwayat Pembelian", onClick = { viewModel.navigateTo(Screen.PurchaseHistory) })
                    ProfileMenuRow(icon = Icons.Default.LocationOn, title = "Data Alamat Pengiriman", onClick = { viewModel.navigateTo(Screen.ShippingAddress) })
                    ProfileMenuRow(icon = Icons.Default.Payment, title = "Metode Pembayaran Tersimpan", onClick = { viewModel.navigateTo(Screen.PaymentMethods) })
                    ProfileMenuRow(
                        icon = Icons.Default.Logout,
                        title = "Keluar dari Akun",
                        color = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.logout() }
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitRow(benefit: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Stars,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = benefit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ==========================================
// HIGHLY ANIMATED INTERACTIVE LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(viewModel: KulturViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            // Moving brand badge logo
            Image(
                painter = painterResource(id = R.drawable.img_brand_logo),
                contentDescription = "KULTUR Brand Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MASUK KE KULTUR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Nikmati penataan gaya rambut & fashion urban terlengkap.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // SignUp Name Field
        if (isSignUp) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Email address field
        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Anda") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("login_email"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Password secure field
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("login_password"),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Error message popup
        loginError?.let {
            item {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Submit action Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty() || (isSignUp && name.isEmpty())) {
                        loginError = "Mohon lengkapi seluruh kolom input."
                    } else if (!email.contains("@")) {
                        loginError = "Masukkan alamat email yang valid."
                    } else if (password.length < 6) {
                        loginError = "Kata sandi minimal 6 karakter."
                    } else {
                        loginError = null
                        val finalName = if (isSignUp) name else email.substringBefore("@")
                        viewModel.submitLogin(email, finalName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSignUp) "DAFTAR SEKARANG" else "MASUK AKUN BESPOKE",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Google Authentication Divider Line
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Text(
                    text = "ATAU MASUK DENGAN",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }

        // Google authenticate button click trigger
        item {
            OutlinedButton(
                onClick = { viewModel.submitGoogleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_login"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Google Icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LANJUTKAN DENGAN GOOGLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Sign Up Toggle helper Text
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUp) "Sudah memiliki akun? " else "Belum bergabung? ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isSignUp) "Masuk di sini" else "Daftar Akun KULTUR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        isSignUp = !isSignUp
                        loginError = null
                    }
                )
            }
        }
    }
}

// ==========================================
// RIWAYAT PEMBELIAN (PURCHASE HISTORY) SCREEN
// ==========================================
@Composable
fun PurchaseHistoryScreen(viewModel: KulturViewModel) {
    val orderHistory by viewModel.orderHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // App bar top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Riwayat Pembelian",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (orderHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Riwayat Pesanan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gunakan Keranjang Belanja untuk memesan produk perawatan atau fashion.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orderHistory) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Order ID & Status Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = order.orderId,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = order.date,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                // Status Indicator Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (order.status == "Selesai") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color(0xFFFF9800).copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.status.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (order.status == "Selesai") MaterialTheme.colorScheme.primary else Color(0xFFFF9800)
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            // Items description list
                            if (order.items.isEmpty()) {
                                Text(
                                    text = "Buku Layanan/Sesi Pembelian Khusus",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = item.product.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Ukuran: ${item.entity.selectedSize}  •  Kuantitas: ${item.entity.quantity}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Text(
                                            text = "Rp ${String.format("%,d", item.product.price * item.entity.quantity).replace(',', '.')}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            // Total summary Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Pembayaran",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Rp ${String.format("%,d", order.totalAmount).replace(',', '.')}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DATA ALAMAT PENGIRIMAN SCREEN
// ==========================================
@Composable
fun ShippingAddressScreen(viewModel: KulturViewModel) {
    val address by viewModel.shippingAddress.collectAsState()

    var nameState by remember { mutableStateOf(address.recipientName) }
    var streetState by remember { mutableStateOf(address.street) }
    var cityState by remember { mutableStateOf(address.city) }
    var zipState by remember { mutableStateOf(address.postalCode) }
    var phoneState by remember { mutableStateOf(address.phone) }

    var saveCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Alamat Pengiriman",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Address Form Details
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipient name textfield
            OutlinedTextField(
                value = nameState,
                onValueChange = { nameState = it },
                label = { Text("Nama Penerima", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Street address
            OutlinedTextField(
                value = streetState,
                onValueChange = { streetState = it },
                label = { Text("Alamat Lengkap", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // City
            OutlinedTextField(
                value = cityState,
                onValueChange = { cityState = it },
                label = { Text("Kota / Kabupaten", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ZIP / Postal
            OutlinedTextField(
                value = zipState,
                onValueChange = { zipState = it },
                label = { Text("Kode Pos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Phone
            OutlinedTextField(
                value = phoneState,
                onValueChange = { phoneState = it },
                label = { Text("Nomor HP Aktif", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (saveCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Alamat Pengiriman Berhasil Disimpan!",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Button
        Button(
            onClick = {
                viewModel.saveShippingAddress(
                    ShippingAddressInfo(
                        recipientName = nameState,
                        street = streetState,
                        city = cityState,
                        postalCode = zipState,
                        phone = phoneState
                    )
                )
                saveCompleted = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "SIMPAN ALAMAT",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// METODE PEMBAYARAN (PAYMENT METHODS) SCREEN
// ==========================================
@Composable
fun PaymentMethodsScreen(viewModel: KulturViewModel) {
    val methods by viewModel.paymentMethods.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    var cardHolderState by remember { mutableStateOf("") }
    var cardNumberState by remember { mutableStateOf("") }
    var expiryState by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("VISA") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // AppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Metode Pembayaran",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "METODE PEMBAYARAN TERDAFTAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Current stored list of payment systems
            methods.forEach { met ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (met.type == "E-WALLET") Icons.Default.AccountBalanceWallet else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = met.cardNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${met.type}  •  ${met.cardHolder}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.removePaymentMethod(met.id) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action triggers to add payment methods
            if (!showForm) {
                OutlinedButton(
                    onClick = { showForm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TAMBAH METODE BARU",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "FORM PEMBAYARAN BARU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Selector Type
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("VISA", "MASTERCARD", "E-WALLET").forEach { type ->
                                val isSelected = selectedType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedType = type }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Number card
                        OutlinedTextField(
                            value = cardNumberState,
                            onValueChange = { cardNumberState = it },
                            label = { Text("Nomor Kartu / HP E-Wallet", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Cardholder
                        OutlinedTextField(
                            value = cardHolderState,
                            onValueChange = { cardHolderState = it },
                            label = { Text("Nama Pemilik Akun", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Expiry CVV Row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = expiryState,
                                onValueChange = { expiryState = it },
                                label = { Text("Berlaku S/D (MM/YY)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { showForm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("BATAL", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            }

                            Button(
                                onClick = {
                                    if (cardNumberState.isNotEmpty() && cardHolderState.isNotEmpty()) {
                                        viewModel.addPaymentMethod(
                                            SavedPaymentMethod(
                                                id = System.currentTimeMillis().toString(),
                                                cardHolder = cardHolderState,
                                                cardNumber = if (selectedType == "E-WALLET") "GOPAY - $cardNumberState" else "**** **** **** " + cardNumberState.takeLast(4),
                                                expiry = expiryState,
                                                type = selectedType
                                            )
                                        )
                                        showForm = false
                                        cardHolderState = ""
                                        cardNumberState = ""
                                        expiryState = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("SIMPAN", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
