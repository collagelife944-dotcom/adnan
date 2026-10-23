package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.net.URLEncoder

fun openWhatsAppChat(context: Context, number: String, accountTitle: String, accountId: String) {
    try {
        val cleanNumber = number.replace("+", "").replace("-", "").replace(" ", "")
        val rawMessage = "Hi, I am interested in this PUBG account: $accountTitle (ID: $accountId)"
        val encodedMessage = URLEncoder.encode(rawMessage, "UTF-8")
        val url = "https://wa.me/$cleanNumber?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp support: +92 302 1570918", Toast.LENGTH_LONG).show()
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreTopBar(
    currentLanguage: AppLanguage,
    onLanguageToggle: () -> Unit,
    wishlistCount: Int,
    onWishlistClick: () -> Unit,
    isAdminMode: Boolean,
    onAdminToggle: () -> Unit
) {
    Surface(
        color = CharcoalBlack,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(PubgOrangeBright, CharcoalBlack)
                                )
                            )
                            .border(1.5.dp, PubgOrange, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.pubg_logo),
                            contentDescription = "Adnan PUBG Store Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ADNAN PUBG",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STORE",
                                color = PubgOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                        Text(
                            text = if (currentLanguage == AppLanguage.URDU) "محفوظ اور معتبر ڈلیوری" else "Trusted Accounts, Safe Delivery",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Actions: Language Switch & Admin Mode Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language Switch Pill
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = DarkSurfaceVariant,
                        border = BorderStroke(1.dp, PubgOrange.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onLanguageToggle() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ENGLISH) "🇺🇸 EN" else "🇵🇰 اردو",
                                color = PubgOrangeBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Wishlist Icon with Badge
                    BadgedBox(
                        badge = {
                            if (wishlistCount > 0) {
                                Badge(
                                    containerColor = PubgOrange,
                                    contentColor = CharcoalBlack
                                ) {
                                    Text(text = wishlistCount.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onWishlistClick,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (wishlistCount > 0) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (wishlistCount > 0) AceRed else TextPrimary
                            )
                        }
                    }

                    // Admin Switch Button
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isAdminMode) PubgOrange else DarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (isAdminMode) PubgGold else DarkCardBorder),
                        modifier = Modifier.clickable { onAdminToggle() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Filled.AdminPanelSettings else Icons.Outlined.Security,
                                contentDescription = "Admin Toggle",
                                tint = if (isAdminMode) CharcoalBlack else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAdminMode) "Admin" else "Store",
                                color = if (isAdminMode) CharcoalBlack else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Glowing divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, PubgOrange.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
        }
    }
}

@Composable
fun StoreBottomBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    pendingOrdersCount: Int = 0,
    language: AppLanguage
) {
    NavigationBar(
        containerColor = CharcoalBlack,
        tonalElevation = 8.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = DarkCardBorder.copy(alpha = 0.4f)
        )
    ) {
        val items = listOf(
            Triple("home", Icons.Filled.Home, AppStrings.get("home", language)),
            Triple("browse", Icons.Filled.Search, AppStrings.get("browse", language)),
            Triple("orders", Icons.Filled.ReceiptLong, AppStrings.get("orders", language)),
            Triple("wishlist", Icons.Filled.Favorite, AppStrings.get("wishlist", language)),
            Triple("admin", Icons.Filled.Shield, AppStrings.get("admin_panel", language))
        )

        items.forEach { (route, icon, label) ->
            val selected = currentScreen == route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                icon = {
                    if (route == "orders" && pendingOrdersCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = PubgOrange, contentColor = CharcoalBlack) {
                                    Text(pendingOrdersCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PubgOrange,
                    selectedTextColor = PubgOrange,
                    indicatorColor = PubgOrangeGlow,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

@Composable
fun AccountStatusBadge(status: AccountStatus, language: AppLanguage) {
    val (bgColor, textColor, label) = when (status) {
        AccountStatus.AVAILABLE -> Triple(
            SuccessGreen.copy(alpha = 0.18f),
            SuccessGreen,
            if (language == AppLanguage.URDU) "دستیاب" else "AVAILABLE"
        )
        AccountStatus.RESERVED -> Triple(
            PendingYellow.copy(alpha = 0.18f),
            PendingYellow,
            if (language == AppLanguage.URDU) "محفوظ" else "RESERVED"
        )
        AccountStatus.SOLD -> Triple(
            AceRed.copy(alpha = 0.18f),
            AceRed,
            if (language == AppLanguage.URDU) "فروخت شدہ" else "SOLD"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AccountCard(
    account: PubgAccount,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(
            1.dp,
            if (account.isFeatured) PubgOrange.copy(alpha = 0.6f) else DarkCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Header with overlay badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Local drawable image or placeholder
                val imageRes = if (account.imageDrawableRes.contains("mummy")) {
                    com.example.R.drawable.pubg_mummy_banner
                } else {
                    com.example.R.drawable.pubg_hero_banner
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = account.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    DarkCard.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Top row inside image: Status & Featured / Wishlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountStatusBadge(status = account.status, language = language)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (account.isFeatured) {
                            Surface(
                                color = PubgGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, PubgGold),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Featured",
                                        tint = PubgGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "HOT",
                                        color = PubgGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Wishlist circular button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CharcoalBlack.copy(alpha = 0.7f))
                                .border(1.dp, DarkCardBorder, CircleShape)
                                .clickable { onWishlistToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isWishlisted) AceRed else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom row inside image: Rank Tier & Level
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CharcoalBlack.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, PubgOrange.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "👑 ${account.rankTier.displayName}",
                            color = PubgOrangeBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CharcoalBlack.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = "Lv. ${account.level} · KD ${account.kdRatio}",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Body Details
            Column(modifier = Modifier.padding(14.dp)) {
                // Title
                Text(
                    text = if (language == AppLanguage.URDU && account.titleUrdu.isNotBlank()) account.titleUrdu else account.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Highlight tags (Glacier, Mummy, X-Suit, etc.)
                if (account.highlightSkins.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        account.highlightSkins.take(2).forEach { skin ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant,
                                border = BorderStroke(0.8.dp, GlacierCyan.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "✦ $skin",
                                    color = GlacierCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "✨ ${account.mythicItemsCount} Mythics",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "🔫 ${account.gunSkinsCount} Guns",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "🌍 ${account.server.displayName}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = DarkCardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Quick Action Row
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
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " PKR",
                                color = PubgOrangeBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (account.originalPricePkr > account.pricePkr) {
                            Text(
                                text = "Rs. ${account.originalPricePkr}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    // Direct WhatsApp Enquiry Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                        modifier = Modifier.clickable {
                            openWhatsAppChat(context, "923021570918", account.title, account.id)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.URDU) "رابطہ" else "Chat",
                                color = Color(0xFF25D366),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrustBadgesRow() {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustItem(icon = Icons.Filled.VerifiedUser, title = "100% Safe Escrow", subtitle = "No Scam Guarantee")
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkCardBorder))
            TrustItem(icon = Icons.Filled.FlashOn, title = "Fast Delivery", subtitle = "Under 15 Mins")
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkCardBorder))
            TrustItem(icon = Icons.Filled.SupportAgent, title = "24/7 WhatsApp", subtitle = "+92 302 1570918")
        }
    }
}

@Composable
private fun TrustItem(icon: ImageVector, title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PubgOrange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 9.sp
        )
    }
}

@Composable
fun VideoShowcasePlayer(
    videoTitle: String,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0.35f) }
    var showHud by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                kotlinx.coroutines.delay(100)
                currentProgress = (currentProgress + 0.01f).let { if (it > 1f) 0f else it }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, GlacierCyan.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color.Black)
            ) {
                // Background showcase image (PUBG hero artwork)
                Image(
                    painter = painterResource(id = com.example.R.drawable.pubg_hero_banner),
                    contentDescription = "Showcase Video",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark video overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isPlaying) 0.25f else 0.5f))
                        .clickable {
                            showHud = !showHud
                            if (!isPlaying) isPlaying = true
                        }
                )

                // Watermark & Live Video Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, GlacierCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) AceRed else GlacierCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "PLAYING VIDEO (1080p 60FPS)" else "VIDEO PREVIEW",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = PubgOrange.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ADNAN PUBG STORE",
                            color = CharcoalBlack,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Center Play / Pause big button
                if (showHud || !isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(PubgOrange.copy(alpha = 0.9f))
                            .shadow(12.dp, CircleShape)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = CharcoalBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Video Controls Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Progress Slider
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = PubgOrange,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = videoTitle,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "${(currentProgress * 42).toInt()}s / 0:42",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
