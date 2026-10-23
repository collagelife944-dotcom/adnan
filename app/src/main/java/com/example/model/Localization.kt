package com.example.model

object AppStrings {
    fun get(key: String, language: AppLanguage): String {
        val isUrdu = language == AppLanguage.URDU
        return when (key) {
            "app_name" -> if (isUrdu) "عدنان پب جی اسٹور" else "Adnan PUBG Store"
            "tagline" -> if (isUrdu) "معتبر پب جی اکاؤنٹس، محفوظ ترین ترسیل" else "Trusted PUBG Accounts, Safe Delivery"
            "home" -> if (isUrdu) "ہوم" else "Home"
            "browse" -> if (isUrdu) "براؤز" else "Browse"
            "orders" -> if (isUrdu) "آرڈرز" else "Orders"
            "wishlist" -> if (isUrdu) "پسندیدہ" else "Wishlist"
            "profile" -> if (isUrdu) "پروفائل" else "Profile"
            "admin_panel" -> if (isUrdu) "ایڈمن پینل" else "Admin Panel"
            
            // Home & Banners
            "featured_accounts" -> if (isUrdu) "نمایاں پب جی اکاؤنٹس" else "Featured Accounts"
            "new_arrivals" -> if (isUrdu) "نئی آمد" else "New Arrivals"
            "hot_deals" -> if (isUrdu) "خصوصی رعایت" else "Hot Deals"
            "categories" -> if (isUrdu) "کیٹیگریز" else "Categories"
            "view_all" -> if (isUrdu) "سب دیکھیں" else "View All"
            "safe_guarantee" -> if (isUrdu) "100٪ رقم کی واپسی کی گارنٹی" else "100% Safe Escrow Guarantee"
            "instant_whatsapp" -> if (isUrdu) "واٹس ایپ پر فوری رابطہ" else "Instant WhatsApp Support"
            "verified_credentials" -> if (isUrdu) "مکمل تصدیق شدہ تفصیلات" else "Verified Safe Credentials"
            
            // Account Details
            "price" -> if (isUrdu) "قیمت" else "Price"
            "original_price" -> if (isUrdu) "اصل قیمت" else "Original Price"
            "save" -> if (isUrdu) "بچت" else "Save"
            "buy_now" -> if (isUrdu) "ابھی خریدیں" else "Buy Now"
            "chat_whatsapp" -> if (isUrdu) "واٹس ایپ پر بات کریں" else "Chat on WhatsApp"
            "specs" -> if (isUrdu) "اکاؤنٹ کی تفصیلات" else "Account Specs"
            "rank" -> if (isUrdu) "رینک" else "Rank"
            "level" -> if (isUrdu) "لیول" else "Level"
            "kd" -> if (isUrdu) "کے/ڈی تناسب" else "K/D Ratio"
            "server" -> if (isUrdu) "سرور" else "Server"
            "season" -> if (isUrdu) "سیزن" else "Season"
            "mythic_items" -> if (isUrdu) "میتھک اشیاء" else "Mythic Items"
            "gun_skins" -> if (isUrdu) "گن سکنز" else "Gun Skins"
            "vehicle_skins" -> if (isUrdu) "گاڑیوں کی سکنز" else "Vehicle Skins"
            "outfits" -> if (isUrdu) "لباس / سوٹس" else "Outfits"
            "inventory_value" -> if (isUrdu) "اکاؤنٹ مالیت" else "Inventory Value"
            "linked_via" -> if (isUrdu) "منسلک پلیٹ فارم" else "Linked Platform"
            "video_showcase" -> if (isUrdu) "ویڈیو پریویو (پلے کریں)" else "Video Showcase (Tap to Play)"
            "reviews" -> if (isUrdu) "خریداروں کے تاثرات" else "Buyer Reviews"
            
            // Checkout
            "checkout" -> if (isUrdu) "چیک آؤٹ" else "Checkout"
            "select_payment" -> if (isUrdu) "ادائیگی کا طریقہ منتخب کریں" else "Select Payment Method"
            "transfer_to" -> if (isUrdu) "رقم اس اکاؤنٹ میں بھیجیں:" else "Transfer amount to:"
            "account_title" -> if (isUrdu) "اکاؤنٹ کا نام" else "Account Title"
            "account_number" -> if (isUrdu) "اکاؤنٹ نمبر" else "Account Number"
            "copy" -> if (isUrdu) "کاپی" else "Copy"
            "copied" -> if (isUrdu) "کاپی ہو گیا!" else "Copied!"
            "transaction_id" -> if (isUrdu) "ٹرانزیکشن آئی ڈی (TID)" else "Transaction ID (TID)"
            "payment_screenshot" -> if (isUrdu) "پیمنٹ اسکرین شاٹ" else "Payment Screenshot"
            "screenshot_attached" -> if (isUrdu) "اسکرین شاٹ شامل ہے" else "Screenshot Attached"
            "attach_screenshot" -> if (isUrdu) "اسکرین شاٹ منسلک کریں" else "Attach Screenshot / Receipt"
            "coupon_code" -> if (isUrdu) "کوپن کوڈ" else "Coupon Code"
            "apply" -> if (isUrdu) "لاگو کریں" else "Apply"
            "coupon_applied" -> if (isUrdu) "کوپن رعایت لاگو ہوگئی!" else "Coupon Applied!"
            "buyer_name" -> if (isUrdu) "خریدار کا نام" else "Your Name"
            "buyer_phone" -> if (isUrdu) "واٹس ایپ نمبر" else "WhatsApp / Mobile Number"
            "buyer_email" -> if (isUrdu) "ای میل ایڈریس" else "Email Address"
            "confirm_order" -> if (isUrdu) "آرڈر تصدیق کریں" else "Confirm & Submit Order"
            "order_placed_title" -> if (isUrdu) "آرڈر کامیابی سے درج ہو گیا!" else "Order Placed Successfully!"
            
            // Order Tracking & Credentials
            "order_tracking" -> if (isUrdu) "آرڈر ٹریکنگ" else "Order Tracking"
            "order_id" -> if (isUrdu) "آرڈر آئی ڈی" else "Order ID"
            "status" -> if (isUrdu) "حیثیت" else "Status"
            "credentials_locked_notice" -> if (isUrdu) 
                "🔒 سیکیورٹی وجہ سے لاگ ان تفصیلات چھپی ہوئی ہیں، ایڈمن جیسے ہی آرڈر ڈیلیور کرے گا تفصیلات یہاں ظاہر ہو جائیں گی۔"
                else "🔒 Security Notice: Account credentials remain encrypted and will unlock automatically here once Admin verifies payment and delivers the account."
            "credentials_unlocked_title" -> if (isUrdu) "🎉 اکاؤنٹ لاگ ان کی تفصیلات (ڈیلیور شدہ)" else "🎉 Delivered Account Login Credentials"
            "username_email" -> if (isUrdu) "یوزر نیم / ای میل" else "Login ID / Email"
            "password" -> if (isUrdu) "پاس ورڈ" else "Password"
            "two_factor" -> if (isUrdu) "2FA / سیکیورٹی کوڈ" else "2FA / Security Code"
            "delivery_instructions" -> if (isUrdu) "لاگ ان کے بعد فوری طور پر پاس ورڈ تبدیل کر لیں اور پرانے ڈیوائسز ہٹا دیں۔" 
                else "Important: Please log in, change password immediately and revoke other devices in PUBG Security settings."
                
            // Admin Panel
            "admin_dashboard" -> if (isUrdu) "ایڈمن ڈیش بورڈ" else "Admin Dashboard"
            "manage_accounts" -> if (isUrdu) "اکاؤنٹس مینجمنٹ" else "Account Management"
            "manage_orders" -> if (isUrdu) "آرڈرز مینجمنٹ" else "Order Management"
            "add_new_account" -> if (isUrdu) "نیا اکاؤنٹ شامل کریں" else "Add New Account"
            "edit_account" -> if (isUrdu) "اکاؤنٹ میں ترمیم کریں" else "Edit Account"
            "store_settings" -> if (isUrdu) "اسٹور کی ترتیبات" else "Store Payment Settings"
            "total_sales" -> if (isUrdu) "کل فروخت" else "Total Revenue"
            "total_orders" -> if (isUrdu) "کل آرڈرز" else "Total Orders"
            "pending_orders" -> if (isUrdu) "تصدیق کے منتظر آرڈرز" else "Pending Orders"
            "active_accounts" -> if (isUrdu) "دستیاب اکاؤنٹس" else "Available Accounts"
            "deliver_account" -> if (isUrdu) "اکاؤنٹ ڈیلیور کریں" else "Deliver Account Credentials"
            "verify_payment" -> if (isUrdu) "ادائیگی کی تصدیق کریں" else "Verify Payment"
            "mark_completed" -> if (isUrdu) "آرڈر مکمل کریں" else "Mark Order Completed"
            
            else -> key
        }
    }
}
