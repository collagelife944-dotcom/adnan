package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import com.example.ui.components.AccountStatusBadge
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    repository: StoreRepository,
    onExitAdmin: () -> Unit
) {
    BackHandler { onExitAdmin() }

    val context = LocalContext.current
    val isAuthenticated by repository.isAdminAuthenticated.collectAsState()
    val adminRole by repository.currentAdminRole.collectAsState()
    val accounts by repository.accounts.collectAsState()
    val orders by repository.orders.collectAsState()
    val settings by repository.storeSettings.collectAsState()
    val language by repository.language.collectAsState()

    var adminPinInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Accounts, 2: Orders, 3: Settings
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<PubgAccount?>(null) }

    // IF NOT AUTHENTICATED -> SHOW ADMIN LOGIN / ROLE SELECTION
    if (!isAuthenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CharcoalBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.5.dp, PubgGold.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PubgGold.copy(alpha = 0.15f))
                            .border(1.dp, PubgGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = PubgGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Adnan PUBG Store",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Admin Panel Authentication (/admin)",
                        color = PubgGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = { adminPinInput = it },
                        label = { Text("Admin PIN or Password") },
                        placeholder = { Text("default: admin or 1234") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgGold,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select Admin Role",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    var chosenRole by remember { mutableStateOf(AdminRole.SUPER_ADMIN) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AdminRole.values().forEach { role ->
                            val isSelected = chosenRole == role
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PubgGold else DarkSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { chosenRole = role }
                            ) {
                                Text(
                                    text = role.title,
                                    color = if (isSelected) CharcoalBlack else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (adminPinInput.isBlank() || adminPinInput == "admin" || adminPinInput == "1234" || adminPinInput == "admin123") {
                                repository.setAdminAuthenticated(true, chosenRole)
                                Toast.makeText(context, "Logged in as ${chosenRole.title}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Tip: Use 'admin' or '1234' to login", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PubgGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Login to Admin Dashboard",
                            color = CharcoalBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = onExitAdmin) {
                        Text("← Return to Store", color = TextSecondary)
                    }
                }
            }
        }
        return
    }

    // AUTHENTICATED ADMIN PANEL
    Scaffold(
        containerColor = CharcoalBlack,
        topBar = {
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onExitAdmin) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Exit Admin", tint = TextPrimary)
                            }
                            Column {
                                Text(
                                    text = "Admin Panel",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Role: ${adminRole.title}",
                                    color = PubgGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Quick Add Account Button
                            Button(
                                onClick = {
                                    editingAccount = null
                                    showAddAccountDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Account", color = CharcoalBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Logout Button
                            IconButton(
                                onClick = { repository.setAdminAuthenticated(false) }
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = AceRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Navigation
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurface,
                        contentColor = PubgOrange,
                        divider = { Divider(color = DarkCardBorder) }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Dashboard", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Accounts (${accounts.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                val pendingCount = orders.count { it.status == OrderStatus.PENDING }
                                Text(
                                    text = if (pendingCount > 0) "Orders ($pendingCount)" else "Orders (${orders.size})",
                                    fontSize = 12.sp,
                                    color = if (pendingCount > 0) PubgOrange else TextPrimary,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("Settings", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> AdminDashboardTab(
                    accounts = accounts,
                    orders = orders,
                    onNavigateToAccounts = { selectedTab = 1 },
                    onNavigateToOrders = { selectedTab = 2 }
                )
                1 -> AdminAccountsTab(
                    accounts = accounts,
                    repository = repository,
                    onAddAccountClick = {
                        editingAccount = null
                        showAddAccountDialog = true
                    },
                    onEditAccountClick = { acc ->
                        editingAccount = acc
                        showAddAccountDialog = true
                    }
                )
                2 -> AdminOrdersTab(
                    orders = orders,
                    repository = repository
                )
                3 -> AdminSettingsTab(
                    settings = settings,
                    repository = repository
                )
            }
        }
    }

    // ADD / EDIT ACCOUNT MODAL DIALOG
    if (showAddAccountDialog) {
        AccountEditDialog(
            accountToEdit = editingAccount,
            onDismiss = { showAddAccountDialog = false },
            onSave = { newOrUpdatedAccount ->
                if (editingAccount != null) {
                    repository.updateAccount(newOrUpdatedAccount)
                    Toast.makeText(context, "Account updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    repository.addAccount(newOrUpdatedAccount)
                    Toast.makeText(context, "New PUBG Account Published!", Toast.LENGTH_SHORT).show()
                }
                showAddAccountDialog = false
            }
        )
    }
}

// ---------------- DASHBOARD TAB ----------------
@Composable
private fun AdminDashboardTab(
    accounts: List<PubgAccount>,
    orders: List<Order>,
    onNavigateToAccounts: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val totalRevenue = orders.filter { it.status == OrderStatus.ACCOUNT_DELIVERED || it.status == OrderStatus.COMPLETED }
        .sumOf { it.finalAmountPkr }
    val pendingCount = orders.count { it.status == OrderStatus.PENDING }
    val availableCount = accounts.count { it.status == AccountStatus.AVAILABLE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Store Performance Overview",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Total Revenue",
                    value = "Rs. ${totalRevenue / 1000}K",
                    subtitle = "Verified Sales",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Pending Verification",
                    value = "$pendingCount",
                    subtitle = "Needs Review",
                    color = if (pendingCount > 0) PendingYellow else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Available Accounts",
                    value = "$availableCount",
                    subtitle = "In Catalog",
                    color = PubgOrange,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Orders",
                    value = "${orders.size}",
                    subtitle = "Lifetime",
                    color = GlacierCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Operational Shortcuts",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToOrders() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PendingActions, contentDescription = null, tint = PendingYellow)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Review Pending Orders ($pendingCount)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                    }

                    Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAccounts() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Inventory, contentDescription = null, tint = PubgOrange)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Manage All PUBG Accounts (${accounts.size})", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}

// ---------------- ACCOUNTS TAB ----------------
@Composable
private fun AdminAccountsTab(
    accounts: List<PubgAccount>,
    repository: StoreRepository,
    onAddAccountClick: () -> Unit,
    onEditAccountClick: (PubgAccount) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Store Inventory (${accounts.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddAccountClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Account", color = CharcoalBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(accounts, key = { it.id }) { account ->
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ID: ${account.id} · ${account.server.displayName}",
                            color = PubgOrangeBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        AccountStatusBadge(status = account.status, language = AppLanguage.ENGLISH)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = account.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Price: Rs. ${account.pricePkr} PKR · Rank: ${account.rankTier.displayName} · Mythics: ${account.mythicItemsCount}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Admin Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status Switcher
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(0.8.dp, DarkCardBorder),
                            modifier = Modifier.clickable {
                                val nextStatus = when (account.status) {
                                    AccountStatus.AVAILABLE -> AccountStatus.RESERVED
                                    AccountStatus.RESERVED -> AccountStatus.SOLD
                                    AccountStatus.SOLD -> AccountStatus.AVAILABLE
                                }
                                repository.updateAccountStatus(account.id, nextStatus)
                            }
                        ) {
                            Text(
                                text = "Status: ${account.status.name}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Edit Button
                        OutlinedButton(
                            onClick = { onEditAccountClick(account) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = TextPrimary, fontSize = 11.sp)
                        }

                        // Delete Button
                        IconButton(
                            onClick = {
                                repository.deleteAccount(account.id)
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AceRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- ORDERS TAB ----------------
@Composable
private fun AdminOrdersTab(
    orders: List<Order>,
    repository: StoreRepository
) {
    val context = LocalContext.current
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filtered = if (statusFilter == null) orders else orders.filter { it.status == statusFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Order Management & Delivery",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${orders.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.PENDING,
                    onClick = { statusFilter = if (statusFilter == OrderStatus.PENDING) null else OrderStatus.PENDING },
                    label = { Text("Pending (${orders.count { it.status == OrderStatus.PENDING }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.ACCOUNT_DELIVERED,
                    onClick = { statusFilter = if (statusFilter == OrderStatus.ACCOUNT_DELIVERED) null else OrderStatus.ACCOUNT_DELIVERED },
                    label = { Text("Delivered", fontSize = 11.sp) }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No orders match this filter.", color = TextMuted)
                }
            }
        } else {
            items(filtered, key = { it.id }) { order ->
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (order.status == OrderStatus.PENDING) PendingYellow else DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.id,
                                color = PubgOrangeBright,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            OrderStatusPill(status = order.status, language = AppLanguage.ENGLISH)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = order.accountTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Buyer: ${order.buyerName} · 📞 ${order.buyerPhone}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "Amount: Rs. ${order.finalAmountPkr} PKR · Method: ${order.paymentMethod.titleEn}",
                            color = PubgOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "TID: ${order.transactionId} (${order.screenshotNote})",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons for Order Processing
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (order.status == OrderStatus.PENDING) {
                                Button(
                                    onClick = {
                                        repository.updateOrderStatus(order.id, OrderStatus.PAYMENT_VERIFIED)
                                        Toast.makeText(context, "Payment marked as Verified!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Verified, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Step 1: Verify Payment", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            if (order.status == OrderStatus.PENDING || order.status == OrderStatus.PAYMENT_VERIFIED) {
                                Button(
                                    onClick = {
                                        repository.updateOrderStatus(order.id, OrderStatus.ACCOUNT_DELIVERED)
                                        Toast.makeText(context, "Account Delivered! Buyer can now view credentials.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Key, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Step 2: Deliver Account Credentials to Buyer", color = CharcoalBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }

                            if (order.status == OrderStatus.ACCOUNT_DELIVERED) {
                                Button(
                                    onClick = {
                                        repository.updateOrderStatus(order.id, OrderStatus.COMPLETED)
                                        Toast.makeText(context, "Order marked Completed!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PubgGold),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Step 3: Mark Order Completed", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        repository.updateOrderStatus(order.id, OrderStatus.REJECTED)
                                        Toast.makeText(context, "Order Rejected", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, AceRed.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject", color = AceRed, fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        repository.updateOrderStatus(order.id, OrderStatus.REFUNDED)
                                        Toast.makeText(context, "Order Refunded", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, TextSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Refund", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SETTINGS TAB ----------------
@Composable
private fun AdminSettingsTab(
    settings: StoreSettings,
    repository: StoreRepository
) {
    val context = LocalContext.current

    var jazzNumber by remember(settings) { mutableStateOf(settings.jazzCashNumber) }
    var jazzTitle by remember(settings) { mutableStateOf(settings.jazzCashTitle) }
    var epNumber by remember(settings) { mutableStateOf(settings.easypaisaNumber) }
    var epTitle by remember(settings) { mutableStateOf(settings.easypaisaTitle) }
    var bankName by remember(settings) { mutableStateOf(settings.bankName) }
    var bankTitle by remember(settings) { mutableStateOf(settings.bankAccountTitle) }
    var bankIban by remember(settings) { mutableStateOf(settings.bankIban) }
    var whatsappNumber by remember(settings) { mutableStateOf(settings.whatsappNumber) }
    var noticeBanner by remember(settings) { mutableStateOf(settings.noticeBanner) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Store Payment & Contact Settings",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "These numbers and accounts appear directly in the Checkout screen for buyers.",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // WhatsApp Support
        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("WhatsApp Customer Support", color = PubgOrangeBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("WhatsApp (without + or dashes)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // JazzCash
        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("JazzCash Payment Details", color = PubgOrangeBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jazzNumber,
                        onValueChange = { jazzNumber = it },
                        label = { Text("JazzCash Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jazzTitle,
                        onValueChange = { jazzTitle = it },
                        label = { Text("Account Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Easypaisa
        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Easypaisa Payment Details", color = PubgOrangeBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = epNumber,
                        onValueChange = { epNumber = it },
                        label = { Text("Easypaisa Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = epTitle,
                        onValueChange = { epTitle = it },
                        label = { Text("Account Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Bank Details
        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Bank Transfer Details", color = PubgOrangeBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bankTitle,
                        onValueChange = { bankTitle = it },
                        label = { Text("Bank Account Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bankIban,
                        onValueChange = { bankIban = it },
                        label = { Text("IBAN") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Announcement Banner
        item {
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Top Announcement Notice", color = PubgOrangeBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noticeBanner,
                        onValueChange = { noticeBanner = it },
                        label = { Text("Notice Text") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PubgOrange,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    repository.updateStoreSettings(
                        StoreSettings(
                            jazzCashNumber = jazzNumber.trim(),
                            jazzCashTitle = jazzTitle.trim(),
                            easypaisaNumber = epNumber.trim(),
                            easypaisaTitle = epTitle.trim(),
                            bankName = bankName.trim(),
                            bankAccountTitle = bankTitle.trim(),
                            bankIban = bankIban.trim(),
                            whatsappNumber = whatsappNumber.trim(),
                            supportEmail = settings.supportEmail,
                            noticeBanner = noticeBanner.trim()
                        )
                    )
                    Toast.makeText(context, "Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Save Store Settings", color = CharcoalBlack, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ---------------- FULL ACCOUNT EDIT / CREATE DIALOG ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditDialog(
    accountToEdit: PubgAccount?,
    onDismiss: () -> Unit,
    onSave: (PubgAccount) -> Unit
) {
    var title by remember { mutableStateOf(accountToEdit?.title ?: "") }
    var description by remember { mutableStateOf(accountToEdit?.description ?: "") }
    var category by remember { mutableStateOf(accountToEdit?.category ?: "glacier") }
    var priceStr by remember { mutableStateOf(accountToEdit?.pricePkr?.toString() ?: "35000") }
    var origPriceStr by remember { mutableStateOf(accountToEdit?.originalPricePkr?.toString() ?: "42000") }
    var levelStr by remember { mutableStateOf(accountToEdit?.level?.toString() ?: "72") }
    var kdStr by remember { mutableStateOf(accountToEdit?.kdRatio?.toString() ?: "4.5") }
    var rank by remember { mutableStateOf(accountToEdit?.rankTier ?: RankTier.ACE_DOMINATOR) }
    var server by remember { mutableStateOf(accountToEdit?.server ?: ServerRegion.ASIA) }
    var season by remember { mutableStateOf(accountToEdit?.season ?: "Cycle 6 Season 17") }
    var mythicsStr by remember { mutableStateOf(accountToEdit?.mythicItemsCount?.toString() ?: "35") }
    var gunsStr by remember { mutableStateOf(accountToEdit?.gunSkinsCount?.toString() ?: "28") }
    var vehiclesStr by remember { mutableStateOf(accountToEdit?.vehicleSkinsCount?.toString() ?: "6") }
    var platform by remember { mutableStateOf(accountToEdit?.linkedPlatform ?: LinkedPlatform.TWITTER) }
    var isFeatured by remember { mutableStateOf(accountToEdit?.isFeatured ?: false) }

    // Credentials
    var loginPlatform by remember { mutableStateOf(accountToEdit?.credentials?.loginPlatform ?: "Twitter / X") }
    var loginUsername by remember { mutableStateOf(accountToEdit?.credentials?.usernameOrEmail ?: "") }
    var loginPassword by remember { mutableStateOf(accountToEdit?.credentials?.password ?: "") }
    var twoFactor by remember { mutableStateOf(accountToEdit?.credentials?.twoFactorSecret ?: "") }
    var transferNotes by remember { mutableStateOf(accountToEdit?.credentials?.transferNotes ?: "Change password & unlink old devices immediately after login.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (accountToEdit == null) "Add New PUBG Account" else "Edit PUBG Account",
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Account Title") },
                        placeholder = { Text("e.g. Glacier M416 Lv7 + Mummy Set") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Price (PKR)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = origPriceStr,
                            onValueChange = { origPriceStr = it },
                            label = { Text("Original Price") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = levelStr,
                            onValueChange = { levelStr = it },
                            label = { Text("Level") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = kdStr,
                            onValueChange = { kdStr = it },
                            label = { Text("K/D") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mythicsStr,
                            onValueChange = { mythicsStr = it },
                            label = { Text("Mythics") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gunsStr,
                            onValueChange = { gunsStr = it },
                            label = { Text("Gun Skins") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text(
                        text = "🔒 Hidden Buyer Credentials",
                        color = PubgGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = { loginUsername = it },
                        label = { Text("Username / Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Account Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = twoFactor,
                        onValueChange = { twoFactor = it },
                        label = { Text("2FA Backup Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Featured on Homepage", color = TextPrimary, fontSize = 12.sp)
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val price = priceStr.toLongOrNull() ?: 30000L
                    val origPrice = origPriceStr.toLongOrNull() ?: price
                    val level = levelStr.toIntOrNull() ?: 70
                    val kd = kdStr.toDoubleOrNull() ?: 4.0
                    val mythics = mythicsStr.toIntOrNull() ?: 25
                    val guns = gunsStr.toIntOrNull() ?: 20
                    val vehicles = vehiclesStr.toIntOrNull() ?: 5

                    val account = PubgAccount(
                        id = accountToEdit?.id ?: "PUBG-${(100..999).random()}",
                        title = title.trim(),
                        description = description.trim().ifEmpty { "High-end PUBG Mobile verified account with exclusive skins." },
                        category = category,
                        pricePkr = price,
                        originalPricePkr = origPrice,
                        rankTier = rank,
                        level = level,
                        kdRatio = kd,
                        server = server,
                        season = season,
                        outfitsCount = 80,
                        gunSkinsCount = guns,
                        vehicleSkinsCount = vehicles,
                        mythicItemsCount = mythics,
                        inventoryValuePkr = price * 3,
                        linkedPlatform = platform,
                        status = accountToEdit?.status ?: AccountStatus.AVAILABLE,
                        isFeatured = isFeatured,
                        highlightSkins = listOf("Upgraded Weapon", "Mythic Suit", "Lobby Title"),
                        imageDrawableRes = "pubg_hero_banner",
                        videoPreviewTitle = "$title Showcase",
                        credentials = AccountCredentials(
                            loginPlatform = loginPlatform,
                            usernameOrEmail = loginUsername.trim(),
                            password = loginPassword.trim(),
                            twoFactorSecret = twoFactor.trim(),
                            transferNotes = transferNotes.trim()
                        ),
                        verificationBadge = "Verified Safe - Store Managed",
                        rating = 5.0f,
                        reviewCount = (5..20).random()
                    )
                    onSave(account)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PubgOrange)
            ) {
                Text("Save Account", color = CharcoalBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
