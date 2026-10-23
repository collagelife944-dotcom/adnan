package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoreRepository
import com.example.model.*
import com.example.ui.components.AccountCard
import com.example.ui.theme.*

enum class SortOption(val labelEn: String, val labelUr: String) {
    NEWEST("Newest", "جدید ترین"),
    PRICE_LOW_HIGH("Price: Low to High", "قیمت: کم سے زیادہ"),
    PRICE_HIGH_LOW("Price: High to Low", "قیمت: زیادہ سے کم"),
    MOST_MYTHICS("Most Mythics", "زیادہ میتھک اشیاء")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    repository: StoreRepository,
    initialCategory: String? = null,
    onNavigateToAccount: (String) -> Unit
) {
    val accounts by repository.accounts.collectAsState()
    val wishlistIds by repository.wishlistIds.collectAsState()
    val language by repository.language.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "all") }
    var selectedRank by remember { mutableStateOf<RankTier?>(null) }
    var selectedServer by remember { mutableStateOf<ServerRegion?>(null) }
    var maxPriceFilter by remember { mutableFloatStateOf(100000f) }
    var selectedSort by remember { mutableStateOf(SortOption.NEWEST) }
    var onlyAvailable by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Filter logic
    val filteredAccounts = accounts.filter { account ->
        val matchesCategory = selectedCategory == "all" || account.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                account.title.contains(searchQuery, ignoreCase = true) ||
                account.description.contains(searchQuery, ignoreCase = true) ||
                account.highlightSkins.any { it.contains(searchQuery, ignoreCase = true) } ||
                account.rankTier.displayName.contains(searchQuery, ignoreCase = true) ||
                account.server.displayName.contains(searchQuery, ignoreCase = true)
        val matchesRank = selectedRank == null || account.rankTier == selectedRank
        val matchesServer = selectedServer == null || account.server == selectedServer
        val matchesPrice = account.pricePkr <= maxPriceFilter.toLong()
        val matchesStatus = !onlyAvailable || account.status == AccountStatus.AVAILABLE

        matchesCategory && matchesSearch && matchesRank && matchesServer && matchesPrice && matchesStatus
    }.let { list ->
        when (selectedSort) {
            SortOption.NEWEST -> list.sortedByDescending { it.createdAt }
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.pricePkr }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.pricePkr }
            SortOption.MOST_MYTHICS -> list.sortedByDescending { it.mythicItemsCount }
        }
    }

    Scaffold(
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Row
            Surface(
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = PubgOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (language == AppLanguage.URDU) "پب جی اکاؤنٹ، گن سکنز تلاش کریں..." else "Search by guns, mummy, rank, server...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                    IconButton(
                        onClick = { showFilterSheet = true }
                    ) {
                        BadgedBox(
                            badge = {
                                val activeCount = (if (selectedRank != null) 1 else 0) +
                                        (if (selectedServer != null) 1 else 0) +
                                        (if (maxPriceFilter < 100000f) 1 else 0) +
                                        (if (onlyAvailable) 1 else 0)
                                if (activeCount > 0) {
                                    Badge(containerColor = PubgOrange, contentColor = CharcoalBlack) {
                                        Text(activeCount.toString(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Tune,
                                contentDescription = "Filter",
                                tint = if (selectedRank != null || selectedServer != null || maxPriceFilter < 100000f) PubgOrange else TextPrimary
                            )
                        }
                    }
                }
            }

            // Quick Sort & Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Filter (All vs Specific)
                items(listOf("all", "glacier", "xsuit", "conqueror", "mummy", "budget", "cars")) { cat ->
                    val isCatSelected = selectedCategory == cat
                    val catLabel = when (cat) {
                        "all" -> "All"
                        "glacier" -> "Glacier M4"
                        "xsuit" -> "X-Suits"
                        "conqueror" -> "Conqueror"
                        "mummy" -> "Mummy"
                        "budget" -> "Budget"
                        "cars" -> "Cars"
                        else -> cat
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isCatSelected) PubgOrange else DarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (isCatSelected) PubgGold else DarkCardBorder),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = catLabel,
                            color = if (isCatSelected) CharcoalBlack else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Results count and active sort display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredAccounts.size} " + if (language == AppLanguage.URDU) "اکاؤنٹس دستیاب" else "Accounts Found",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // Sort Dropdown Trigger
                var sortMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        border = BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.clickable { sortMenuExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⇅ " + if (language == AppLanguage.URDU) selectedSort.labelUr else selectedSort.labelEn,
                                color = PubgOrangeBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (language == AppLanguage.URDU) option.labelUr else option.labelEn,
                                        color = if (selectedSort == option) PubgOrange else TextPrimary,
                                        fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedSort = option
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Accounts List or Empty State
            if (filteredAccounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FilterAltOff,
                            contentDescription = "No results",
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.URDU) "کوئی اکاؤنٹ نہیں ملا" else "No matching accounts found",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.URDU) "فلٹرز صاف کر کے دوبارہ کوشش کریں" else "Try clearing some filters or searching with different keywords.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedCategory = "all"
                                selectedRank = null
                                selectedServer = null
                                maxPriceFilter = 100000f
                                onlyAvailable = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PubgOrange)
                        ) {
                            Text("Reset All Filters", color = CharcoalBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredAccounts, key = { it.id }) { account ->
                        AccountCard(
                            account = account,
                            isWishlisted = wishlistIds.contains(account.id),
                            onWishlistToggle = { repository.toggleWishlist(account.id) },
                            onClick = { onNavigateToAccount(account.id) },
                            language = language
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = DarkSurface,
            scrimColor = Color.Black.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters & Preferences",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    TextButton(
                        onClick = {
                            selectedRank = null
                            selectedServer = null
                            maxPriceFilter = 100000f
                            onlyAvailable = false
                        }
                    ) {
                        Text("Reset", color = AceRed)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Max Price Slider
                Text(
                    text = "Max Price: Rs. ${maxPriceFilter.toInt()} PKR",
                    color = PubgOrangeBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = maxPriceFilter,
                    onValueChange = { maxPriceFilter = it },
                    valueRange = 10000f..100000f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = PubgOrange,
                        activeTrackColor = PubgOrange,
                        inactiveTrackColor = DarkCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Server Region Filter
                Text(
                    text = "Server Region",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedServer == null,
                            onClick = { selectedServer = null },
                            label = { Text("All Servers") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PubgOrange,
                                selectedLabelColor = CharcoalBlack
                            )
                        )
                    }
                    items(ServerRegion.values()) { server ->
                        FilterChip(
                            selected = selectedServer == server,
                            onClick = { selectedServer = if (selectedServer == server) null else server },
                            label = { Text(server.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PubgOrange,
                                selectedLabelColor = CharcoalBlack
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rank Tier Filter
                Text(
                    text = "Rank Tier",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedRank == null,
                            onClick = { selectedRank = null },
                            label = { Text("Any Rank") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PubgOrange,
                                selectedLabelColor = CharcoalBlack
                            )
                        )
                    }
                    items(RankTier.values()) { rank ->
                        FilterChip(
                            selected = selectedRank == rank,
                            onClick = { selectedRank = if (selectedRank == rank) null else rank },
                            label = { Text(rank.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PubgOrange,
                                selectedLabelColor = CharcoalBlack
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Only Available Accounts",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = onlyAvailable,
                        onCheckedChange = { onlyAvailable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PubgOrange,
                            checkedTrackColor = PubgOrangeGlow
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Apply Filters (${filteredAccounts.size} Results)",
                        color = CharcoalBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
