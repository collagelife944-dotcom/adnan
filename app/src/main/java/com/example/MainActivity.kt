package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.StoreRepository
import com.example.model.AppLanguage
import com.example.model.PubgAccount
import com.example.ui.components.StoreBottomBar
import com.example.ui.components.StoreTopBar
import com.example.ui.screens.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repository = remember { StoreRepository(context.applicationContext) }
            val currentLanguage by repository.language.collectAsState()
            val wishlistIds by repository.wishlistIds.collectAsState()
            val orders by repository.orders.collectAsState()
            val accounts by repository.accounts.collectAsState()

            val pendingOrdersCount = orders.count { it.status == com.example.model.OrderStatus.PENDING }

            // Navigation state
            var currentScreen by remember { mutableStateOf("home") }
            var selectedAccountId by remember { mutableStateOf<String?>(null) }
            var initialBrowseCategory by remember { mutableStateOf<String?>(null) }

            // Layout direction based on language
            val layoutDirection = if (currentLanguage == AppLanguage.URDU) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            // Handle back presses smoothly
            BackHandler(enabled = currentScreen != "home") {
                when {
                    currentScreen == "checkout" -> currentScreen = "account_detail"
                    currentScreen == "account_detail" -> currentScreen = "browse"
                    currentScreen == "admin" -> currentScreen = "home"
                    currentScreen == "browse" || currentScreen == "orders" || currentScreen == "wishlist" || currentScreen == "profile" -> currentScreen = "home"
                    else -> currentScreen = "home"
                }
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    val isSubScreen = currentScreen == "account_detail" || currentScreen == "checkout" || currentScreen == "admin"

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CharcoalBlack),
                        containerColor = CharcoalBlack,
                        topBar = {
                            if (!isSubScreen) {
                                Column(modifier = Modifier.statusBarsPadding()) {
                                    StoreTopBar(
                                        currentLanguage = currentLanguage,
                                        onLanguageToggle = {
                                            val next = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.URDU else AppLanguage.ENGLISH
                                            repository.setLanguage(next)
                                        },
                                        wishlistCount = wishlistIds.size,
                                        onWishlistClick = { currentScreen = "wishlist" },
                                        isAdminMode = false,
                                        onAdminToggle = { currentScreen = "admin" }
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (!isSubScreen) {
                                StoreBottomBar(
                                    currentScreen = currentScreen,
                                    onNavigate = { route ->
                                        currentScreen = route
                                    },
                                    pendingOrdersCount = pendingOrdersCount,
                                    language = currentLanguage
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                                },
                                label = "ScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    "home" -> HomeScreen(
                                        repository = repository,
                                        onNavigateToAccount = { id ->
                                            selectedAccountId = id
                                            currentScreen = "account_detail"
                                        },
                                        onNavigateToBrowse = { cat ->
                                            initialBrowseCategory = cat
                                            currentScreen = "browse"
                                        },
                                        onNavigateToAdmin = { currentScreen = "admin" }
                                    )

                                    "browse" -> BrowseScreen(
                                        repository = repository,
                                        initialCategory = initialBrowseCategory,
                                        onNavigateToAccount = { id ->
                                            selectedAccountId = id
                                            currentScreen = "account_detail"
                                        }
                                    )

                                    "orders" -> OrdersScreen(
                                        repository = repository,
                                        onNavigateToHome = { currentScreen = "home" }
                                    )

                                    "wishlist" -> WishlistScreen(
                                        repository = repository,
                                        onNavigateToAccount = { id ->
                                            selectedAccountId = id
                                            currentScreen = "account_detail"
                                        },
                                        onNavigateToBrowse = { currentScreen = "browse" }
                                    )

                                    "profile" -> ProfileScreen(
                                        repository = repository,
                                        onNavigateToOrders = { currentScreen = "orders" },
                                        onNavigateToWishlist = { currentScreen = "wishlist" },
                                        onNavigateToAdmin = { currentScreen = "admin" }
                                    )

                                    "account_detail" -> {
                                        val targetId = selectedAccountId ?: accounts.firstOrNull()?.id ?: ""
                                        AccountDetailScreen(
                                            accountId = targetId,
                                            repository = repository,
                                            onBack = { currentScreen = "home" },
                                            onProceedToCheckout = {
                                                currentScreen = "checkout"
                                            }
                                        )
                                    }

                                    "checkout" -> {
                                        val targetAccount = accounts.find { it.id == selectedAccountId } ?: accounts.first()
                                        CheckoutScreen(
                                            account = targetAccount,
                                            repository = repository,
                                            onBack = { currentScreen = "account_detail" },
                                            onOrderPlaced = { order ->
                                                currentScreen = "orders"
                                            }
                                        )
                                    }

                                    "admin" -> AdminScreen(
                                        repository = repository,
                                        onExitAdmin = { currentScreen = "home" }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
