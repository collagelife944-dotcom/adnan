package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoreRepository
import com.example.model.*
import com.example.ui.components.AccountCard
import com.example.ui.components.openWhatsAppChat
import com.example.ui.theme.*

@Composable
fun WishlistScreen(
    repository: StoreRepository,
    onNavigateToAccount: (String) -> Unit,
    onNavigateToBrowse: () -> Unit
) {
    val accounts by repository.accounts.collectAsState()
    val wishlistIds by repository.wishlistIds.collectAsState()
    val language by repository.language.collectAsState()

    val wishlistedAccounts = accounts.filter { wishlistIds.contains(it.id) }

    Scaffold(
        containerColor = CharcoalBlack,
        topBar = {
            Surface(
                color = CharcoalBlack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = AppStrings.get("wishlist", language),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${wishlistedAccounts.size} saved PUBG accounts",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        if (wishlistedAccounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "Empty Wishlist",
                        tint = TextMuted,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "کوئی پسندیدہ اکاؤنٹ نہیں ہے" else "Your wishlist is empty",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the heart icon on any account to save it here for quick access.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToBrowse,
                        colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.URDU) "اکاؤنٹس براؤز کریں" else "Browse Accounts",
                            color = CharcoalBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(wishlistedAccounts, key = { it.id }) { account ->
                    AccountCard(
                        account = account,
                        isWishlisted = true,
                        onWishlistToggle = { repository.toggleWishlist(account.id) },
                        onClick = { onNavigateToAccount(account.id) },
                        language = language
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    repository: StoreRepository,
    onNavigateToOrders: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val language by repository.language.collectAsState()
    val settings by repository.storeSettings.collectAsState()
    val orders by repository.orders.collectAsState()
    val wishlistIds by repository.wishlistIds.collectAsState()

    var showFaqDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CharcoalBlack,
        topBar = {
            Surface(
                color = CharcoalBlack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = AppStrings.get("profile", language),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Header Card
            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PubgOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(PubgOrangeGlow)
                                .border(1.5.dp, PubgOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "PUBG Player / Buyer",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Direct WhatsApp Delivery Support",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToOrders() }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${orders.size}", color = PubgOrange, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text(text = "Total Orders", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToWishlist() }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${wishlistIds.size}", color = AceRed, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text(text = "Wishlist", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Language & Store Preferences
            item {
                Text(
                    text = "Preferences & Support",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileMenuRow(
                            icon = Icons.Filled.Language,
                            title = "Language / زبان",
                            subtitle = if (language == AppLanguage.ENGLISH) "English (Active)" else "اردو (فعال)",
                            onClick = {
                                val next = if (language == AppLanguage.ENGLISH) AppLanguage.URDU else AppLanguage.ENGLISH
                                repository.setLanguage(next)
                            }
                        )

                        Divider(color = DarkCardBorder.copy(alpha = 0.5f))

                        ProfileMenuRow(
                            icon = Icons.Filled.Chat,
                            title = "WhatsApp Official Support",
                            subtitle = "+92 302 1570918",
                            tint = Color(0xFF25D366),
                            onClick = {
                                openWhatsAppChat(context, settings.whatsappNumber, "Store Support", "Profile")
                            }
                        )

                        Divider(color = DarkCardBorder.copy(alpha = 0.5f))

                        ProfileMenuRow(
                            icon = Icons.Filled.HelpOutline,
                            title = "Frequently Asked Questions (FAQ)",
                            subtitle = "Delivery time, safety guarantee & payment",
                            onClick = { showFaqDialog = true }
                        )

                        Divider(color = DarkCardBorder.copy(alpha = 0.5f))

                        ProfileMenuRow(
                            icon = Icons.Filled.Gavel,
                            title = "Terms of Service",
                            subtitle = "PUBG account transfer rules",
                            onClick = { showTermsDialog = true }
                        )

                        Divider(color = DarkCardBorder.copy(alpha = 0.5f))

                        ProfileMenuRow(
                            icon = Icons.Filled.PrivacyTip,
                            title = "Privacy Policy",
                            subtitle = "Data encryption & security",
                            onClick = { showPrivacyDialog = true }
                        )
                    }
                }
            }

            // Admin Portal Card
            item {
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PubgGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdmin() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = PubgGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Portal (/admin)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage accounts, deliver credentials, update payment numbers",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Open",
                            tint = PubgGold
                        )
                    }
                }
            }
        }
    }

    // FAQ Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Frequently Asked Questions",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. How fast is account delivery?",
                        color = PubgOrangeBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Usually within 5 to 15 minutes after payment verification on JazzCash, Easypaisa, or Bank Transfer.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2. How are credentials delivered?",
                        color = PubgOrangeBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Account credentials are encrypted and only unlock in your Orders screen once the admin marks the order as Delivered.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "3. Can I speak to the owner on WhatsApp?",
                        color = PubgOrangeBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Yes! You can contact Adnan directly anytime on +92 302 1570918 for account verification and transfer assistance.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFaqDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PubgOrange)
                ) {
                    Text("Close", color = CharcoalBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(text = "Terms of Service", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "All PUBG accounts sold on Adnan PUBG Store undergo 7-point safety screening. Buyers must change passwords and unlink prior devices within 24 hours of delivery. 100% money-back guarantee applies if credentials fail upon initial delivery.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PubgOrange)
                ) {
                    Text("Understood", color = CharcoalBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(text = "Privacy Policy", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Buyer phone numbers, transaction IDs, and credentials are kept strictly private and never shared with third parties. End-to-end escrow protection is maintained for all transactions.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PubgOrange)
                ) {
                    Text("Close", color = CharcoalBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color = PubgOrange,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
