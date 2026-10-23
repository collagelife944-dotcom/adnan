package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

class StoreRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("adnan_pubg_store_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _accounts = MutableStateFlow<List<PubgAccount>>(emptyList())
    val accounts: StateFlow<List<PubgAccount>> = _accounts.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _storeSettings = MutableStateFlow(StoreSettings())
    val storeSettings: StateFlow<StoreSettings> = _storeSettings.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _categories = MutableStateFlow<List<StoreCategory>>(emptyList())
    val categories: StateFlow<List<StoreCategory>> = _categories.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _currentAdminRole = MutableStateFlow(AdminRole.SUPER_ADMIN)
    val currentAdminRole: StateFlow<AdminRole> = _currentAdminRole.asStateFlow()

    init {
        loadData()
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("app_lang", lang.name).apply()
    }

    fun setAdminAuthenticated(authenticated: Boolean, role: AdminRole = AdminRole.SUPER_ADMIN) {
        _isAdminAuthenticated.value = authenticated
        _currentAdminRole.value = role
    }

    fun toggleWishlist(accountId: String) {
        _wishlistIds.update { current ->
            if (current.contains(accountId)) current - accountId else current + accountId
        }
        saveWishlist()
    }

    // Account Management (Admin & Store)
    fun addAccount(account: PubgAccount) {
        _accounts.update { listOf(account) + it }
        saveAccounts()
    }

    fun updateAccount(account: PubgAccount) {
        _accounts.update { list ->
            list.map { if (it.id == account.id) account else it }
        }
        saveAccounts()
    }

    fun deleteAccount(accountId: String) {
        _accounts.update { list -> list.filterNot { it.id == accountId } }
        saveAccounts()
    }

    fun updateAccountStatus(accountId: String, newStatus: AccountStatus) {
        _accounts.update { list ->
            list.map { if (it.id == accountId) it.copy(status = newStatus) else it }
        }
        saveAccounts()
    }

    // Order Management (Buyer & Admin)
    fun createOrder(
        account: PubgAccount,
        paymentMethod: PaymentMethod,
        transactionId: String,
        screenshotNote: String,
        buyerName: String,
        buyerPhone: String,
        buyerEmail: String,
        appliedDiscount: Long
    ): Order {
        val finalPrice = (account.pricePkr - appliedDiscount).coerceAtLeast(0)
        val newOrder = Order(
            id = "ORD-${System.currentTimeMillis().toString().takeLast(6)}",
            accountId = account.id,
            accountTitle = account.title,
            accountPricePkr = account.pricePkr,
            discountAppliedPkr = appliedDiscount,
            finalAmountPkr = finalPrice,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            screenshotNote = screenshotNote,
            status = OrderStatus.PENDING,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            buyerEmail = buyerEmail,
            orderTimestamp = System.currentTimeMillis(),
            deliveredCredentials = null // explicitly hidden until Delivered!
        )

        _orders.update { listOf(newOrder) + it }
        // Mark account as Reserved upon checkout
        updateAccountStatus(account.id, AccountStatus.RESERVED)
        saveOrders()
        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    val matchingAccount = _accounts.value.find { it.id == order.accountId }
                    val creds = if (newStatus == OrderStatus.ACCOUNT_DELIVERED || newStatus == OrderStatus.COMPLETED) {
                        order.deliveredCredentials ?: matchingAccount?.credentials
                    } else {
                        order.deliveredCredentials
                    }

                    // If delivered or completed, mark account as SOLD
                    if (newStatus == OrderStatus.ACCOUNT_DELIVERED || newStatus == OrderStatus.COMPLETED) {
                        updateAccountStatus(order.accountId, AccountStatus.SOLD)
                    } else if (newStatus == OrderStatus.REJECTED || newStatus == OrderStatus.REFUNDED) {
                        updateAccountStatus(order.accountId, AccountStatus.AVAILABLE)
                    }

                    order.copy(status = newStatus, deliveredCredentials = creds)
                } else order
            }
        }
        saveOrders()
    }

    // Settings
    fun updateStoreSettings(settings: StoreSettings) {
        _storeSettings.value = settings
        saveSettings()
    }

    // Coupons
    fun addCoupon(coupon: Coupon) {
        _coupons.update { it + coupon }
        saveCoupons()
    }

    fun validateCoupon(code: String, orderAmount: Long): Coupon? {
        val clean = code.trim().uppercase()
        val coupon = _coupons.value.find { it.code.uppercase() == clean && it.isActive }
        return if (coupon != null && orderAmount >= coupon.minOrderPkr) coupon else null
    }

    private fun loadData() {
        val savedLang = prefs.getString("app_lang", AppLanguage.ENGLISH.name)
        _language.value = try { AppLanguage.valueOf(savedLang ?: "ENGLISH") } catch (e: Exception) { AppLanguage.ENGLISH }

        // Categories
        _categories.value = listOf(
            StoreCategory("all", "All Accounts", "تمام اکاؤنٹس", "🔥", "Hot"),
            StoreCategory("glacier", "Glacier M416", "گلیشیر M416", "❄️", "Max Lv7"),
            StoreCategory("xsuit", "X-Suits", "ایکس سوٹس", "👑", "Pharaoh/Raven"),
            StoreCategory("conqueror", "Conqueror", "کانکرر", "🏆", "S14/C3"),
            StoreCategory("mummy", "Mummy Sets", "ممی سوٹس", "🥋", "Classic"),
            StoreCategory("budget", "Budget Deals", "کم قیمت ڈیلز", "💰", "< 15K PKR"),
            StoreCategory("cars", "Supercars", "سپر کارز", "🏎️", "Tesla/Koenigsegg")
        )

        // Store Settings
        val customSettingsJson = prefs.getString("store_settings", null)
        if (customSettingsJson != null) {
            try {
                val json = JSONObject(customSettingsJson)
                _storeSettings.value = StoreSettings(
                    jazzCashNumber = json.optString("jazzCashNumber", "0302-1570918"),
                    jazzCashTitle = json.optString("jazzCashTitle", "Adnan - Store Owner"),
                    easypaisaNumber = json.optString("easypaisaNumber", "0302-1570918"),
                    easypaisaTitle = json.optString("easypaisaTitle", "Adnan - Store Owner"),
                    bankName = json.optString("bankName", "Meezan Bank Limited"),
                    bankAccountTitle = json.optString("bankAccountTitle", "Adnan PUBG Store"),
                    bankIban = json.optString("bankIban", "PK36MEZN0001234567890123"),
                    whatsappNumber = json.optString("whatsappNumber", "923021570918"),
                    supportEmail = json.optString("supportEmail", "support@adnanpubgstore.com"),
                    noticeBanner = json.optString("noticeBanner", "⚡ 100% Verified PUBG Mobile Accounts with Instant Transfer Guarantee!")
                )
            } catch (e: Exception) {
                _storeSettings.value = StoreSettings()
            }
        } else {
            _storeSettings.value = StoreSettings()
        }

        // Coupons
        _coupons.value = listOf(
            Coupon(code = "PUBG10", discountPercent = 10, minOrderPkr = 10000, isActive = true),
            Coupon(code = "ADNAN1000", discountPkr = 1000, minOrderPkr = 15000, isActive = true),
            Coupon(code = "WELCOME500", discountPkr = 500, minOrderPkr = 5000, isActive = true)
        )

        // Accounts: Load saved or use rich default accounts
        val savedAccounts = prefs.getString("saved_accounts", null)
        if (!savedAccounts.isNullOrEmpty()) {
            try {
                _accounts.value = parseAccountsJson(savedAccounts)
            } catch (e: Exception) {
                _accounts.value = getInitialAccounts()
            }
        } else {
            _accounts.value = getInitialAccounts()
            saveAccounts()
        }

        // Orders
        val savedOrders = prefs.getString("saved_orders", null)
        if (!savedOrders.isNullOrEmpty()) {
            try {
                _orders.value = parseOrdersJson(savedOrders)
            } catch (e: Exception) {
                _orders.value = getInitialOrders()
            }
        } else {
            _orders.value = getInitialOrders()
            saveOrders()
        }

        // Wishlist
        val savedWishlist = prefs.getStringSet("wishlist_ids", emptySet()) ?: emptySet()
        _wishlistIds.value = savedWishlist
    }

    private fun getInitialAccounts(): List<PubgAccount> {
        return listOf(
            PubgAccount(
                id = "PUBG-101",
                title = "Glacier M416 MAX Lv7 + White Mummy Set (Full Kill Message + Loot Crate)",
                titleUrdu = "گلیشیر M416 میکس لیول 7 + وائٹ ممی سوٹ (مکمل کل میسج اور لوٹ کریٹ)",
                description = "Legendary collection account! Glacier M416 upgraded to Level 7 with custom On-Hit effect, Kill Broadcast, and snowman Loot Crate. Includes ultra-rare White Mummy outfit, Fool M416 Lv4, AWM Rainbow, and 450K Popularity. Clean single-link Twitter, 100% safe with full email change support.",
                category = "glacier",
                pricePkr = 45000,
                originalPricePkr = 52000,
                rankTier = RankTier.ACE_DOMINATOR,
                level = 76,
                kdRatio = 5.42,
                server = ServerRegion.ASIA,
                season = "Cycle 6 Season 17",
                outfitsCount = 112,
                gunSkinsCount = 48,
                vehicleSkinsCount = 8,
                mythicItemsCount = 42,
                inventoryValuePkr = 180000,
                linkedPlatform = LinkedPlatform.TWITTER,
                status = AccountStatus.AVAILABLE,
                isFeatured = true,
                highlightSkins = listOf("M416 Glacier Lv7", "White Mummy Set", "Fool M416 Lv4", "AWM God of War"),
                imageDrawableRes = "pubg_hero_banner",
                videoPreviewTitle = "M416 Glacier Lv7 Kill Effect In Action",
                credentials = AccountCredentials(
                    loginPlatform = "Twitter / X",
                    usernameOrEmail = "pubg_glacier_asia101@twitter.com",
                    password = "SafePubgPass2026!#",
                    twoFactorSecret = "849201",
                    transferNotes = "Link your mobile number immediately after login. Backup codes provided."
                ),
                verificationBadge = "Verified Safe - Single Link",
                rating = 5.0f,
                reviewCount = 18
            ),
            PubgAccount(
                id = "PUBG-102",
                title = "Pharaoh X-Suit Lv6 + Fool M416 MAX + Tesla Roadster (Asia Ace)",
                titleUrdu = "فرعون ایکس سوٹ لیول 6 + فول M416 میکس + ٹیسلا روڈسٹر",
                description = "King of the battlefield! Golden Pharaoh X-Suit Lv6 with custom emote and mummy entrance. Fool M416 Lv7 Max with animated mouth kill box. Exclusive Metallic Silver Tesla Roadster Dacia skin. High prestige account for serious streamers and tournament players.",
                category = "xsuit",
                pricePkr = 78000,
                originalPricePkr = 89000,
                rankTier = RankTier.CONQUEROR,
                level = 81,
                kdRatio = 6.18,
                server = ServerRegion.ASIA,
                season = "Cycle 6 Season 17",
                outfitsCount = 165,
                gunSkinsCount = 64,
                vehicleSkinsCount = 14,
                mythicItemsCount = 68,
                inventoryValuePkr = 340000,
                linkedPlatform = LinkedPlatform.EMAIL_LINK,
                status = AccountStatus.AVAILABLE,
                isFeatured = true,
                highlightSkins = listOf("Pharaoh X-Suit Lv6", "Fool M416 Lv7 Max", "Tesla Roadster", "AKM Glacier Lv4"),
                imageDrawableRes = "pubg_hero_banner",
                videoPreviewTitle = "Pharaoh Golden Entrance & Tesla Drive Showcase",
                credentials = AccountCredentials(
                    loginPlatform = "Clean Email Link",
                    usernameOrEmail = "adnan_pharaoh_store@gmail.com",
                    password = "Pharaoh#Royal2026",
                    twoFactorSecret = "619042",
                    transferNotes = "Full Gmail access handed over. You can change recovery email and phone instantly."
                ),
                verificationBadge = "Exclusive Collector Tier",
                rating = 4.9f,
                reviewCount = 24
            ),
            PubgAccount(
                id = "PUBG-103",
                title = "Conqueror S14 Title + Blood Raven X-Suit Lv4 + AWM Field Commander",
                titleUrdu = "سیزن 14 کانکرر ٹائٹل + بلڈ ریوین ایکس سوٹ + AWM فیلڈ کمانڈر",
                description = "OG Season 14 Conqueror frame and title! Rare Blood Raven X-Suit Lv4 with crows flight emote. Upgraded AWM Field Commander with custom sniper kill broadcast. Over 600K popularity, Level 74, Middle East and Asia both 20ms ping.",
                category = "conqueror",
                pricePkr = 62000,
                originalPricePkr = 70000,
                rankTier = RankTier.CONQUEROR,
                level = 74,
                kdRatio = 4.88,
                server = ServerRegion.MIDDLE_EAST,
                season = "S14 Conqueror OG",
                outfitsCount = 98,
                gunSkinsCount = 38,
                vehicleSkinsCount = 6,
                mythicItemsCount = 35,
                inventoryValuePkr = 220000,
                linkedPlatform = LinkedPlatform.FACEBOOK,
                status = AccountStatus.AVAILABLE,
                isFeatured = true,
                highlightSkins = listOf("S14 Conqueror Title", "Blood Raven X-Suit Lv4", "AWM Commander", "DP-28 Gilded"),
                imageDrawableRes = "pubg_mummy_banner",
                videoPreviewTitle = "Blood Raven Wings Emote & Conqueror Lobby",
                credentials = AccountCredentials(
                    loginPlatform = "Facebook",
                    usernameOrEmail = "conqueror_og_s14@fb.com",
                    password = "WinnerChickenDinner!7",
                    twoFactorSecret = "193850",
                    transferNotes = "Facebook account registered on Pakistani SIM, easily transferable."
                ),
                verificationBadge = "OG S14 Conqueror Verified",
                rating = 5.0f,
                reviewCount = 9
            ),
            PubgAccount(
                id = "PUBG-104",
                title = "Budget Starter Account: Glacier M416 Lv4 (Kill Message) + 22 Mythics",
                titleUrdu = "بجٹ اسٹارٹر اکاؤنٹ: گلیشیر M416 لیول 4 (کل میسج) + 22 میتھکس",
                description = "Perfect budget account for competitive players who want the famous Glacier kill message! Level 68, Ace Master rank, 3.8 K/D ratio, Mythic fashion badge, and 12 gun lab materials ready for next upgrade.",
                category = "budget",
                pricePkr = 14500,
                originalPricePkr = 18000,
                rankTier = RankTier.ACE_MASTER,
                level = 68,
                kdRatio = 3.85,
                server = ServerRegion.ASIA,
                season = "C6S16 Ace Master",
                outfitsCount = 62,
                gunSkinsCount = 24,
                vehicleSkinsCount = 3,
                mythicItemsCount = 22,
                inventoryValuePkr = 65000,
                linkedPlatform = LinkedPlatform.TWITTER,
                status = AccountStatus.AVAILABLE,
                isFeatured = false,
                highlightSkins = listOf("M416 Glacier Lv4 Kill Msg", "Vampire Set", "Beryl M762 Deadly", "Buggy Shark"),
                imageDrawableRes = "pubg_hero_banner",
                videoPreviewTitle = "Glacier Lv4 Kill Broadcast Gameplay",
                credentials = AccountCredentials(
                    loginPlatform = "Twitter / X",
                    usernameOrEmail = "budget_glacier_4@twitter.com",
                    password = "GlacierQuick2026*",
                    twoFactorSecret = "552140",
                    transferNotes = "Clean Twitter link with zero other linked accounts."
                ),
                verificationBadge = "Best Value Seller Choice",
                rating = 4.8f,
                reviewCount = 15
            ),
            PubgAccount(
                id = "PUBG-105",
                title = "Supercar Edition: Koenigsegg Jesko White + M416 Lizard Roar Lv7",
                titleUrdu = "سپر کار ایڈیشن: کونگزیگ یسکو وہائٹ + M416 لزرڈ رور لیول 7",
                description = "Ultra high-speed showcase! Features the hypercar Koenigsegg Jesko in Pearl White with custom engine roar sound. M416 Lizard Roar Level 7 maxed out. Over 55 Mythics, RP maxed for past 10 seasons.",
                category = "cars",
                pricePkr = 58000,
                originalPricePkr = 65000,
                rankTier = RankTier.ACE_DOMINATOR,
                level = 79,
                kdRatio = 5.12,
                server = ServerRegion.EUROPE,
                season = "Cycle 6 Season 17",
                outfitsCount = 135,
                gunSkinsCount = 52,
                vehicleSkinsCount = 11,
                mythicItemsCount = 56,
                inventoryValuePkr = 260000,
                linkedPlatform = LinkedPlatform.EMAIL_LINK,
                status = AccountStatus.AVAILABLE,
                isFeatured = false,
                highlightSkins = listOf("Koenigsegg Jesko White", "M416 Lizard Roar Lv7", "UMP45 Marine Lv4", "Godzilla AWM"),
                imageDrawableRes = "pubg_mummy_banner",
                videoPreviewTitle = "Koenigsegg Acceleration Sound & Speed Showcase",
                credentials = AccountCredentials(
                    loginPlatform = "Clean Email Link",
                    usernameOrEmail = "jesko_pubg_eu@gmail.com",
                    password = "SpeedJesko2026@!",
                    twoFactorSecret = "920144",
                    transferNotes = "Google email with 100% full transfer instructions."
                ),
                verificationBadge = "Supercar Certified",
                rating = 5.0f,
                reviewCount = 11
            ),
            PubgAccount(
                id = "PUBG-106",
                title = "Yellow Mummy + Poseidon X-Suit Lv4 + M416 Imperial Splendor",
                titleUrdu = "یلو ممی + پوسیڈن ایکس سوٹ لیول 4 + M416 امپیریل",
                description = "Iconic Yellow Mummy suit with water-themed Poseidon X-Suit. Fully upgraded M416 Imperial Splendor with golden hit effect. 40+ Mythics, Level 72, smooth server ping across all regions.",
                category = "mummy",
                pricePkr = 38000,
                originalPricePkr = 44000,
                rankTier = RankTier.ACE,
                level = 72,
                kdRatio = 4.25,
                server = ServerRegion.ASIA,
                season = "Cycle 6 Season 16",
                outfitsCount = 85,
                gunSkinsCount = 34,
                vehicleSkinsCount = 5,
                mythicItemsCount = 41,
                inventoryValuePkr = 160000,
                linkedPlatform = LinkedPlatform.TWITTER,
                status = AccountStatus.AVAILABLE,
                isFeatured = false,
                highlightSkins = listOf("Yellow Mummy Set", "Poseidon X-Suit Lv4", "Imperial M416 Lv5", "Golden Dacia"),
                imageDrawableRes = "pubg_mummy_banner",
                videoPreviewTitle = "Yellow Mummy Lobby Showcase",
                credentials = AccountCredentials(
                    loginPlatform = "Twitter / X",
                    usernameOrEmail = "yellow_mummy_adnan@twitter.com",
                    password = "YellowMummySafe#9",
                    twoFactorSecret = "339102",
                    transferNotes = "Twitter account phone number can be changed right away."
                ),
                verificationBadge = "Safe Escrow Delivery",
                rating = 4.9f,
                reviewCount = 14
            )
        )
    }

    private fun getInitialOrders(): List<Order> {
        return listOf(
            Order(
                id = "ORD-982104",
                accountId = "PUBG-101",
                accountTitle = "Glacier M416 MAX Lv7 + White Mummy Set",
                accountPricePkr = 45000,
                discountAppliedPkr = 1000,
                finalAmountPkr = 44000,
                paymentMethod = PaymentMethod.JAZZ_CASH,
                transactionId = "JC-9821471029",
                screenshotNote = "JazzCash Receipt #98214 Attached",
                status = OrderStatus.ACCOUNT_DELIVERED,
                buyerName = "Hamza Tariq",
                buyerPhone = "0312-9844123",
                buyerEmail = "hamza.tariq@gmail.com",
                orderTimestamp = System.currentTimeMillis() - 86400000,
                deliveredCredentials = AccountCredentials(
                    loginPlatform = "Twitter / X",
                    usernameOrEmail = "pubg_glacier_asia101@twitter.com",
                    password = "SafePubgPass2026!#",
                    twoFactorSecret = "849201",
                    transferNotes = "Link your mobile number immediately after login. Backup codes provided."
                )
            ),
            Order(
                id = "ORD-419208",
                accountId = "PUBG-104",
                accountTitle = "Budget Starter Account: Glacier M416 Lv4",
                accountPricePkr = 14500,
                discountAppliedPkr = 500,
                finalAmountPkr = 14000,
                paymentMethod = PaymentMethod.EASYPAISA,
                transactionId = "EP-4418290311",
                screenshotNote = "Easypaisa TID 4418290311 Screenshot Attached",
                status = OrderStatus.PAYMENT_VERIFIED,
                buyerName = "Ali Raza",
                buyerPhone = "0345-5512903",
                buyerEmail = "ali.raza.pubg@gmail.com",
                orderTimestamp = System.currentTimeMillis() - 14400000,
                deliveredCredentials = null // locked until delivered
            )
        )
    }

    private fun saveWishlist() {
        prefs.edit().putStringSet("wishlist_ids", _wishlistIds.value).apply()
    }

    private fun saveAccounts() {
        try {
            val arr = JSONArray()
            _accounts.value.forEach { acc ->
                val obj = JSONObject()
                obj.put("id", acc.id)
                obj.put("title", acc.title)
                obj.put("titleUrdu", acc.titleUrdu)
                obj.put("description", acc.description)
                obj.put("category", acc.category)
                obj.put("pricePkr", acc.pricePkr)
                obj.put("originalPricePkr", acc.originalPricePkr)
                obj.put("rankTier", acc.rankTier.name)
                obj.put("level", acc.level)
                obj.put("kdRatio", acc.kdRatio)
                obj.put("server", acc.server.name)
                obj.put("season", acc.season)
                obj.put("outfitsCount", acc.outfitsCount)
                obj.put("gunSkinsCount", acc.gunSkinsCount)
                obj.put("vehicleSkinsCount", acc.vehicleSkinsCount)
                obj.put("mythicItemsCount", acc.mythicItemsCount)
                obj.put("inventoryValuePkr", acc.inventoryValuePkr)
                obj.put("linkedPlatform", acc.linkedPlatform.name)
                obj.put("status", acc.status.name)
                obj.put("isFeatured", acc.isFeatured)
                obj.put("imageDrawableRes", acc.imageDrawableRes)
                obj.put("videoPreviewTitle", acc.videoPreviewTitle)
                obj.put("verificationBadge", acc.verificationBadge)
                obj.put("rating", acc.rating.toDouble())
                obj.put("reviewCount", acc.reviewCount)
                obj.put("createdAt", acc.createdAt)
                
                val creds = JSONObject()
                creds.put("loginPlatform", acc.credentials.loginPlatform)
                creds.put("usernameOrEmail", acc.credentials.usernameOrEmail)
                creds.put("password", acc.credentials.password)
                creds.put("twoFactorSecret", acc.credentials.twoFactorSecret)
                creds.put("transferNotes", acc.credentials.transferNotes)
                obj.put("credentials", creds)

                val skinsArr = JSONArray()
                acc.highlightSkins.forEach { skinsArr.put(it) }
                obj.put("highlightSkins", skinsArr)

                arr.put(obj)
            }
            prefs.edit().putString("saved_accounts", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseAccountsJson(jsonStr: String): List<PubgAccount> {
        val list = mutableListOf<PubgAccount>()
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val skinsList = mutableListOf<String>()
            val skinsArr = obj.optJSONArray("highlightSkins")
            if (skinsArr != null) {
                for (j in 0 until skinsArr.length()) {
                    skinsList.add(skinsArr.getString(j))
                }
            }

            val credsObj = obj.optJSONObject("credentials")
            val creds = if (credsObj != null) {
                AccountCredentials(
                    loginPlatform = credsObj.optString("loginPlatform", "Twitter / X"),
                    usernameOrEmail = credsObj.optString("usernameOrEmail", ""),
                    password = credsObj.optString("password", ""),
                    twoFactorSecret = credsObj.optString("twoFactorSecret", ""),
                    transferNotes = credsObj.optString("transferNotes", "")
                )
            } else AccountCredentials()

            list.add(
                PubgAccount(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    titleUrdu = obj.optString("titleUrdu", ""),
                    description = obj.getString("description"),
                    category = obj.optString("category", "all"),
                    pricePkr = obj.getLong("pricePkr"),
                    originalPricePkr = obj.optLong("originalPricePkr", obj.getLong("pricePkr")),
                    rankTier = try { RankTier.valueOf(obj.getString("rankTier")) } catch (e: Exception) { RankTier.ACE },
                    level = obj.optInt("level", 70),
                    kdRatio = obj.optDouble("kdRatio", 4.0),
                    server = try { ServerRegion.valueOf(obj.getString("server")) } catch (e: Exception) { ServerRegion.ASIA },
                    season = obj.optString("season", "Season 17"),
                    outfitsCount = obj.optInt("outfitsCount", 50),
                    gunSkinsCount = obj.optInt("gunSkinsCount", 25),
                    vehicleSkinsCount = obj.optInt("vehicleSkinsCount", 5),
                    mythicItemsCount = obj.optInt("mythicItemsCount", 20),
                    inventoryValuePkr = obj.optLong("inventoryValuePkr", 100000),
                    linkedPlatform = try { LinkedPlatform.valueOf(obj.getString("linkedPlatform")) } catch (e: Exception) { LinkedPlatform.TWITTER },
                    status = try { AccountStatus.valueOf(obj.getString("status")) } catch (e: Exception) { AccountStatus.AVAILABLE },
                    isFeatured = obj.optBoolean("isFeatured", false),
                    highlightSkins = skinsList,
                    imageDrawableRes = obj.optString("imageDrawableRes", "pubg_hero_banner"),
                    videoPreviewTitle = obj.optString("videoPreviewTitle", "Showcase Video"),
                    credentials = creds,
                    verificationBadge = obj.optString("verificationBadge", "100% Safe Escrow"),
                    rating = obj.optDouble("rating", 5.0).toFloat(),
                    reviewCount = obj.optInt("reviewCount", 10),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun saveOrders() {
        try {
            val arr = JSONArray()
            _orders.value.forEach { ord ->
                val obj = JSONObject()
                obj.put("id", ord.id)
                obj.put("accountId", ord.accountId)
                obj.put("accountTitle", ord.accountTitle)
                obj.put("accountPricePkr", ord.accountPricePkr)
                obj.put("discountAppliedPkr", ord.discountAppliedPkr)
                obj.put("finalAmountPkr", ord.finalAmountPkr)
                obj.put("paymentMethod", ord.paymentMethod.name)
                obj.put("transactionId", ord.transactionId)
                obj.put("screenshotNote", ord.screenshotNote)
                obj.put("status", ord.status.name)
                obj.put("buyerName", ord.buyerName)
                obj.put("buyerPhone", ord.buyerPhone)
                obj.put("buyerEmail", ord.buyerEmail)
                obj.put("orderTimestamp", ord.orderTimestamp)

                if (ord.deliveredCredentials != null) {
                    val creds = JSONObject()
                    creds.put("loginPlatform", ord.deliveredCredentials.loginPlatform)
                    creds.put("usernameOrEmail", ord.deliveredCredentials.usernameOrEmail)
                    creds.put("password", ord.deliveredCredentials.password)
                    creds.put("twoFactorSecret", ord.deliveredCredentials.twoFactorSecret)
                    creds.put("transferNotes", ord.deliveredCredentials.transferNotes)
                    obj.put("deliveredCredentials", creds)
                }

                arr.put(obj)
            }
            prefs.edit().putString("saved_orders", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseOrdersJson(jsonStr: String): List<Order> {
        val list = mutableListOf<Order>()
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val credsObj = obj.optJSONObject("deliveredCredentials")
            val creds = if (credsObj != null) {
                AccountCredentials(
                    loginPlatform = credsObj.optString("loginPlatform", "Twitter / X"),
                    usernameOrEmail = credsObj.optString("usernameOrEmail", ""),
                    password = credsObj.optString("password", ""),
                    twoFactorSecret = credsObj.optString("twoFactorSecret", ""),
                    transferNotes = credsObj.optString("transferNotes", "")
                )
            } else null

            list.add(
                Order(
                    id = obj.getString("id"),
                    accountId = obj.getString("accountId"),
                    accountTitle = obj.getString("accountTitle"),
                    accountPricePkr = obj.getLong("accountPricePkr"),
                    discountAppliedPkr = obj.optLong("discountAppliedPkr", 0),
                    finalAmountPkr = obj.getLong("finalAmountPkr"),
                    paymentMethod = try { PaymentMethod.valueOf(obj.getString("paymentMethod")) } catch (e: Exception) { PaymentMethod.JAZZ_CASH },
                    transactionId = obj.getString("transactionId"),
                    screenshotNote = obj.optString("screenshotNote", "Screenshot Attached"),
                    status = try { OrderStatus.valueOf(obj.getString("status")) } catch (e: Exception) { OrderStatus.PENDING },
                    buyerName = obj.getString("buyerName"),
                    buyerPhone = obj.getString("buyerPhone"),
                    buyerEmail = obj.optString("buyerEmail", ""),
                    orderTimestamp = obj.optLong("orderTimestamp", System.currentTimeMillis()),
                    deliveredCredentials = creds
                )
            )
        }
        return list
    }

    private fun saveSettings() {
        val s = _storeSettings.value
        val obj = JSONObject()
        obj.put("jazzCashNumber", s.jazzCashNumber)
        obj.put("jazzCashTitle", s.jazzCashTitle)
        obj.put("easypaisaNumber", s.easypaisaNumber)
        obj.put("easypaisaTitle", s.easypaisaTitle)
        obj.put("bankName", s.bankName)
        obj.put("bankAccountTitle", s.bankAccountTitle)
        obj.put("bankIban", s.bankIban)
        obj.put("whatsappNumber", s.whatsappNumber)
        obj.put("supportEmail", s.supportEmail)
        obj.put("noticeBanner", s.noticeBanner)
        prefs.edit().putString("store_settings", obj.toString()).apply()
    }

    private fun saveCoupons() {
        // can save coupons if needed
    }
}
