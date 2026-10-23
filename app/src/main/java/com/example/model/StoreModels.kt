package com.example.model

enum class AccountStatus(val labelEn: String, val labelUr: String) {
    AVAILABLE("Available", "دستیاب"),
    RESERVED("Reserved", "محفوظ"),
    SOLD("Sold", "فروخت شدہ")
}

enum class RankTier(val displayName: String) {
    CONQUEROR("Conqueror"),
    ACE_DOMINATOR("Ace Dominator"),
    ACE_MASTER("Ace Master"),
    ACE("Ace"),
    CROWN("Crown"),
    DIAMOND("Diamond")
}

enum class ServerRegion(val displayName: String) {
    ASIA("Asia"),
    MIDDLE_EAST("Middle East"),
    EUROPE("Europe"),
    NORTH_AMERICA("North America"),
    KRJP("KRJP")
}

enum class LinkedPlatform(val displayName: String) {
    TWITTER("Twitter / X"),
    FACEBOOK("Facebook"),
    GOOGLE_PLAY("Google Play"),
    EMAIL_LINK("Clean Email Link"),
    APPLE_ID("Apple ID")
}

enum class PaymentMethod(val titleEn: String, val titleUr: String) {
    JAZZ_CASH("JazzCash", "جاز کیش"),
    EASYPAISA("Easypaisa", "ایزی پیسہ"),
    BANK_TRANSFER("Bank Transfer", "بینک ٹرانسفر")
}

enum class OrderStatus(val titleEn: String, val titleUr: String, val stepIndex: Int) {
    PENDING("Pending Verification", "تصدیق باقی ہے", 0),
    PAYMENT_VERIFIED("Payment Verified", "ادائیگی کی تصدیق ہوگئی", 1),
    ACCOUNT_DELIVERED("Account Delivered", "اکاؤنٹ ڈیلیور ہوگیا", 2),
    COMPLETED("Completed", "مکمل شدہ", 3),
    REJECTED("Rejected", "مسترد", -1),
    REFUND_REQUESTED("Refund Requested", "واپسی کی درخواست", -2),
    REFUNDED("Refunded", "رقم واپس کردی گئی", -3)
}

data class AccountCredentials(
    val loginPlatform: String = "Twitter / X",
    val usernameOrEmail: String = "",
    val password: String = "",
    val twoFactorSecret: String = "",
    val transferNotes: String = "Change password & unlink old devices immediately after login."
)

data class PubgAccount(
    val id: String,
    val title: String,
    val titleUrdu: String = "",
    val description: String,
    val category: String,
    val pricePkr: Long,
    val originalPricePkr: Long = pricePkr,
    val rankTier: RankTier,
    val level: Int,
    val kdRatio: Double,
    val server: ServerRegion,
    val season: String,
    val outfitsCount: Int,
    val gunSkinsCount: Int,
    val vehicleSkinsCount: Int,
    val mythicItemsCount: Int,
    val inventoryValuePkr: Long,
    val linkedPlatform: LinkedPlatform,
    val status: AccountStatus = AccountStatus.AVAILABLE,
    val isFeatured: Boolean = false,
    val highlightSkins: List<String> = emptyList(),
    val imageDrawableRes: String = "pubg_hero_banner", // name of local drawable or url
    val videoPreviewTitle: String = "Glacier M416 Lv7 Hit Effect & Mummy Set Showcase",
    val credentials: AccountCredentials = AccountCredentials(),
    val verificationBadge: String = "100% Safe Escrow",
    val rating: Float = 5.0f,
    val reviewCount: Int = 12,
    val createdAt: Long = System.currentTimeMillis()
)

data class StoreCategory(
    val id: String,
    val nameEn: String,
    val nameUr: String,
    val icon: String,
    val badge: String = ""
)

data class Order(
    val id: String,
    val accountId: String,
    val accountTitle: String,
    val accountPricePkr: Long,
    val discountAppliedPkr: Long = 0,
    val finalAmountPkr: Long,
    val paymentMethod: PaymentMethod,
    val transactionId: String,
    val screenshotNote: String = "Screenshot Uploaded",
    val status: OrderStatus = OrderStatus.PENDING,
    val buyerName: String,
    val buyerPhone: String,
    val buyerEmail: String = "",
    val orderTimestamp: Long = System.currentTimeMillis(),
    val deliveredCredentials: AccountCredentials? = null
)

data class StoreSettings(
    val jazzCashNumber: String = "0302-1570918",
    val jazzCashTitle: String = "Adnan - Store Owner",
    val easypaisaNumber: String = "0302-1570918",
    val easypaisaTitle: String = "Adnan - Store Owner",
    val bankName: String = "Meezan Bank Limited",
    val bankAccountTitle: String = "Adnan PUBG Store",
    val bankIban: String = "PK36MEZN0001234567890123",
    val whatsappNumber: String = "923021570918",
    val supportEmail: String = "support@adnanpubgstore.com",
    val noticeBanner: String = "⚡ Ramadan / Eid Special Discounts Active! All accounts with 100% money-back guarantee."
)

data class Coupon(
    val code: String,
    val discountPercent: Int = 0,
    val discountPkr: Long = 0,
    val minOrderPkr: Long = 0,
    val isActive: Boolean = true
)

data class Review(
    val id: String,
    val accountId: String,
    val buyerName: String,
    val rating: Int,
    val comment: String,
    val dateText: String,
    val verifiedBadge: Boolean = true
)

enum class AppLanguage {
    ENGLISH,
    URDU
}

enum class AdminRole(val title: String) {
    SUPER_ADMIN("Super Admin"),
    MANAGER("Manager"),
    SUPPORT("Support Agent")
}
