package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoreRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AccountDetailScreen(
    accountId: String,
    repository: StoreRepository,
    onBack: () -> Unit,
    onProceedToCheckout: (PubgAccount) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val accounts by repository.accounts.collectAsState()
    val wishlistIds by repository.wishlistIds.collectAsState()
    val language by repository.language.collectAsState()
    val settings by repository.storeSettings.collectAsState()

    val account = accounts.find { it.id == accountId }

    if (account == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CharcoalBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Account not found", color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    val isWishlisted = wishlistIds.contains(account.id)
    var activeTab by remember { mutableIntStateOf(0) } // 0: Specs & Gallery, 1: Video Showcase, 2: Reviews

    Scaffold(
        containerColor = CharcoalBlack,
        topBar = {
            Surface(
                color = CharcoalBlack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, PubgOrange.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ID: ${account.id}",
                                color = PubgOrangeBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { repository.toggleWishlist(account.id) }) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) AceRed else TextPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this PUBG Mobile account on Adnan PUBG Store: ${account.title} - Price: Rs. ${account.pricePkr} PKR (WhatsApp: +92 302 1570918)")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Floating Action Bar with WhatsApp and Buy Now buttons
            Surface(
                color = DarkSurface,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WhatsApp Chat Button
                    OutlinedButton(
                        onClick = {
                            openWhatsAppChat(context, settings.whatsappNumber, account.title, account.id)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF25D366).copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(1.2.dp, Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.URDU) "واٹس ایپ" else "WhatsApp",
                            color = Color(0xFF25D366),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Buy Now Button
                    Button(
                        onClick = {
                            if (account.status == AccountStatus.AVAILABLE) {
                                onProceedToCheckout(account)
                            } else {
                                Toast.makeText(context, "This account is currently ${account.status.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = account.status != AccountStatus.SOLD,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PubgOrange,
                            disabledContainerColor = DarkCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Buy",
                            tint = CharcoalBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (account.status == AccountStatus.SOLD) {
                                if (language == AppLanguage.URDU) "فروخت ہو چکا ہے" else "Already Sold"
                            } else {
                                if (language == AppLanguage.URDU) "ابھی خریدیں" else "Buy Now"
                            },
                            color = CharcoalBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // 1. Image Banner with Status Overlay
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    val imgRes = if (account.imageDrawableRes.contains("mummy")) {
                        com.example.R.drawable.pubg_mummy_banner
                    } else {
                        com.example.R.drawable.pubg_hero_banner
                    }

                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = account.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        CharcoalBlack
                                    )
                                )
                            )
                    )

                    // Badges row
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccountStatusBadge(status = account.status, language = language)

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CharcoalBlack.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, PubgGold)
                        ) {
                            Text(
                                text = "🏆 ${account.rankTier.displayName}",
                                color = PubgGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CharcoalBlack.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, GlacierCyan)
                        ) {
                            Text(
                                text = "🌍 ${account.server.displayName}",
                                color = GlacierCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 2. Title & Pricing Header
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = if (language == AppLanguage.URDU && account.titleUrdu.isNotBlank()) account.titleUrdu else account.title,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "Rs. ${account.pricePkr}",
                                    color = PubgOrange,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " PKR",
                                    color = PubgOrangeBright,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (account.originalPricePkr > account.pricePkr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Rs. ${account.originalPricePkr} PKR",
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = AceRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        val discountPercent = ((account.originalPricePkr - account.pricePkr) * 100 / account.originalPricePkr)
                                        Text(
                                            text = "-$discountPercent% OFF",
                                            color = AceRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Rating & Reviews Pill
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = PubgGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${account.rating} (${account.reviewCount})",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trust Notice
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceVariant,
                        border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = "Security",
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Protected by Adnan PUBG Store Escrow. Full login change assistance on WhatsApp.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // 3. Tab Switcher (Specs & Inventory, Video Showcase, Reviews)
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = CharcoalBlack,
                    contentColor = PubgOrange,
                    divider = { Divider(color = DarkCardBorder) }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Text(
                                text = if (language == AppLanguage.URDU) "تفصیلات" else "Specs & Inventory",
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Text(
                                text = if (language == AppLanguage.URDU) "ویڈیو" else "Video Player",
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = {
                            Text(
                                text = if (language == AppLanguage.URDU) "ریویوز" else "Reviews (${account.reviewCount})",
                                fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // TAB 0: Specs & Inventory Cards
            if (activeTab == 0) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // Key Specs Grid
                        Text(
                            text = "Account Statistics",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(title = "Level", value = "${account.level}", subtitle = "Prestige", modifier = Modifier.weight(1f))
                            StatBox(title = "K/D Ratio", value = "${account.kdRatio}", subtitle = "High Tier", modifier = Modifier.weight(1f))
                            StatBox(title = "Mythics", value = "${account.mythicItemsCount}", subtitle = "Fashion", modifier = Modifier.weight(1f))
                            StatBox(title = "Guns", value = "${account.gunSkinsCount}", subtitle = "Lab Skins", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Additional Specs Table
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                SpecRow(label = "Server Region", value = account.server.displayName)
                                Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                SpecRow(label = "Current Season", value = account.season)
                                Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                SpecRow(label = "Vehicle Skins", value = "${account.vehicleSkinsCount} Special Cars")
                                Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                SpecRow(label = "Outfits Count", value = "${account.outfitsCount} Legendary / Mythic")
                                Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                SpecRow(label = "Estimated Value", value = "Rs. ${account.inventoryValuePkr} PKR")
                                Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                SpecRow(label = "Linked Platform", value = account.linkedPlatform.displayName)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Highlight Skins Cards
                        Text(
                            text = "Featured Inventory Highlights",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        account.highlightSkins.forEach { skin ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceVariant,
                                border = BorderStroke(1.dp, GlacierCyan.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = "Skin",
                                        tint = GlacierCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = skin,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Full Description
                        Text(
                            text = "Description",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = account.description,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // TAB 1: Video Showcase
            if (activeTab == 1) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Interactive Gameplay & Lobby Showcase",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        VideoShowcasePlayer(
                            videoTitle = account.videoPreviewTitle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Video Verification Checklist",
                                    color = PubgOrangeBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("✅ Glacier M416 On-Hit Spark & Freeze Kill Effect Verified", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✅ In-game UID matched & recorded in store database", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✅ Zero third-party cheat history or ban notices", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // TAB 2: Customer Reviews
            if (activeTab == 2) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified Buyer Feedback",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "⭐ 4.9 / 5.0",
                                color = PubgGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                items(listOf(
                    Review("r1", account.id, "Zubair Khan (Lahore)", 5, "M416 Glacier real Lv7! Got account delivered within 10 minutes on WhatsApp after JazzCash payment. Adnan bhai is super trusted!", "2 days ago"),
                    Review("r2", account.id, "Daniyal Ahmed (Karachi)", 5, "Single link Twitter transfer was super smooth. Changed email and phone in 5 minutes. Best PUBG store in Pakistan.", "5 days ago"),
                    Review("r3", account.id, "Shoaib Malik (Rawalpindi)", 5, "Account matched the video 100%. Highly recommended for anyone looking for safe PUBG accounts.", "1 week ago")
                )) { review ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = review.buyerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "⭐⭐⭐⭐⭐", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = review.comment, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Verified Buyer · ${review.dateText}", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = PubgOrangeBright, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
