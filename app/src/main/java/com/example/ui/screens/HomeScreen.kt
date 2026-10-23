package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoreRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: StoreRepository,
    onNavigateToAccount: (String) -> Unit,
    onNavigateToBrowse: (String?) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val accounts by repository.accounts.collectAsState()
    val wishlistIds by repository.wishlistIds.collectAsState()
    val language by repository.language.collectAsState()
    val categories by repository.categories.collectAsState()
    val settings by repository.storeSettings.collectAsState()

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val featuredAccounts = accounts.filter { it.isFeatured }
    val newArrivals = accounts.sortedByDescending { it.createdAt }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Notice Announcement Bar
        item {
            Surface(
                color = PubgOrange.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, PubgOrange.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Campaign,
                        contentDescription = "Notice",
                        tint = PubgOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = settings.noticeBanner,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 2. Hero Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.2.dp, PubgOrange.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.pubg_hero_banner),
                    contentDescription = "Hero Showcase",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    CharcoalBlack.copy(alpha = 0.88f),
                                    CharcoalBlack.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = PubgOrange,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.URDU) "خصوصی ڈیلز دستیاب ہیں" else "VERIFIED PUBG STORE",
                            color = CharcoalBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (language == AppLanguage.URDU) "گلیشیر M416 اور ممی سیٹس" else "GLACIER M416 & X-SUITS",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = if (language == AppLanguage.URDU) "جاز کیش، ایزی پیسہ اور بینک ٹرانسفر کے ساتھ" else "Instant WhatsApp Delivery · JazzCash & Easypaisa",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigateToBrowse(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.URDU) "ابھی خریدیں" else "Browse Accounts",
                                color = CharcoalBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                openWhatsAppChat(context, settings.whatsappNumber, "General Enquiry", "Store")
                            },
                            border = BorderStroke(1.dp, Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WhatsApp",
                                color = Color(0xFF25D366),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Search Bar Trigger
        item {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToBrowse(null) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = PubgOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "اکاؤنٹ، گن سکنز، رینک تلاش کریں..." else "Search accounts, Glacier, X-Suits, Conqueror...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 4. Trust Badges
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                TrustBadgesRow()
            }
        }

        // 5. Category Pills Row
        item {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Text(
                    text = AppStrings.get("categories", language),
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PubgOrange else DarkSurfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PubgGold else DarkCardBorder
                            ),
                            modifier = Modifier.clickable {
                                selectedCategory = category.id
                                onNavigateToBrowse(if (category.id == "all") null else category.id)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = category.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.URDU) category.nameUr else category.nameEn,
                                    color = if (isSelected) CharcoalBlack else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                                if (category.badge.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = if (isSelected) CharcoalBlack else PubgOrange.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = category.badge,
                                            color = if (isSelected) PubgGold else PubgOrangeBright,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Featured Accounts Section
        if (featuredAccounts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔥 " + AppStrings.get("featured_accounts", language),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = AppStrings.get("view_all", language) + " →",
                        color = PubgOrangeBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToBrowse(null) }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(featuredAccounts) { account ->
                        AccountCard(
                            account = account,
                            isWishlisted = wishlistIds.contains(account.id),
                            onWishlistToggle = { repository.toggleWishlist(account.id) },
                            onClick = { onNavigateToAccount(account.id) },
                            language = language,
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        // 7. Video Showcase Preview Card
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = "🎬 " + AppStrings.get("video_showcase", language),
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                VideoShowcasePlayer(
                    videoTitle = "M416 Glacier Max Lv7 Hit Effect + White Mummy Emote (Asia Server)"
                )
            }
        }

        // 8. New Arrivals Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ " + AppStrings.get("new_arrivals", language),
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = AppStrings.get("view_all", language) + " →",
                    color = PubgOrangeBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToBrowse(null) }
                )
            }
        }

        items(newArrivals) { account ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                AccountCard(
                    account = account,
                    isWishlisted = wishlistIds.contains(account.id),
                    onWishlistToggle = { repository.toggleWishlist(account.id) },
                    onClick = { onNavigateToAccount(account.id) },
                    language = language
                )
            }
        }

        // 9. Admin Quick Access Banner (at footer)
        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PubgGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onNavigateToAdmin() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PubgGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Security,
                                contentDescription = "Admin",
                                tint = PubgGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Portal (/admin)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add accounts, verify orders, deliver credentials",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        Icons.Filled.ArrowForwardIos,
                        contentDescription = "Go",
                        tint = PubgGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
