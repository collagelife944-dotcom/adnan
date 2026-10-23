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
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    account: PubgAccount,
    repository: StoreRepository,
    onBack: () -> Unit,
    onOrderPlaced: (Order) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val settings by repository.storeSettings.collectAsState()
    val language by repository.language.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.JAZZ_CASH) }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var couponError by remember { mutableStateOf<String?>(null) }

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var hasScreenshot by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Calculate totals
    val basePrice = account.pricePkr
    val discount = when {
        appliedCoupon != null -> {
            if (appliedCoupon!!.discountPercent > 0) {
                (basePrice * appliedCoupon!!.discountPercent) / 100
            } else {
                appliedCoupon!!.discountPkr
            }
        }
        else -> 0L
    }
    val finalTotal = (basePrice - discount).coerceAtLeast(0)

    Scaffold(
        containerColor = CharcoalBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.get("checkout", language),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Payable:",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Rs. $finalTotal",
                                color = PubgOrange,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " PKR",
                                color = PubgOrangeBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (buyerName.isBlank() || buyerPhone.isBlank()) {
                                Toast.makeText(context, "Please enter your Name and WhatsApp phone number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (transactionId.isBlank()) {
                                Toast.makeText(context, "Please enter your Transaction ID (TID) from $selectedPaymentMethod", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmitting = true
                            val order = repository.createOrder(
                                account = account,
                                paymentMethod = selectedPaymentMethod,
                                transactionId = transactionId.trim(),
                                screenshotNote = if (hasScreenshot) "Payment Receipt Verified Screenshot Attached" else "TID Only (Screenshot pending)",
                                buyerName = buyerName.trim(),
                                buyerPhone = buyerPhone.trim(),
                                buyerEmail = buyerEmail.trim(),
                                appliedDiscount = discount
                            )
                            isSubmitting = false
                            Toast.makeText(context, "Order Placed Successfully! Pending Admin verification.", Toast.LENGTH_LONG).show()
                            onOrderPlaced(order)
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = CharcoalBlack, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Lock, contentDescription = "Secure", tint = CharcoalBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get("confirm_order", language),
                                color = CharcoalBlack,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Order Item Summary Card
            item {
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
                                text = "Item to Purchase",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = "ID: ${account.id}",
                                    color = PubgOrangeBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
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
                            text = "Server: ${account.server.displayName} · Rank: ${account.rankTier.displayName} · Level: ${account.level}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Divider(color = DarkCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Account Price", color = TextSecondary, fontSize = 13.sp)
                            Text("Rs. $basePrice PKR", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        if (discount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coupon Discount", color = SuccessGreen, fontSize = 13.sp)
                                Text("- Rs. $discount PKR", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Coupon Code Section
            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = AppStrings.get("coupon_code", language),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = {
                                    couponInput = it
                                    couponError = null
                                },
                                placeholder = { Text("e.g. PUBG10, WELCOME500", color = TextMuted, fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PubgOrange,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val valid = repository.validateCoupon(couponInput, basePrice)
                                    if (valid != null) {
                                        appliedCoupon = valid
                                        couponError = null
                                        Toast.makeText(context, "Coupon Applied Successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        couponError = "Invalid or expired coupon code"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = AppStrings.get("apply", language),
                                    color = CharcoalBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (appliedCoupon != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🎉 Code '${appliedCoupon!!.code}' applied! You saved Rs. $discount PKR",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (couponError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = couponError!!,
                                color = AceRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 3. Payment Method Selection
            item {
                Text(
                    text = AppStrings.get("select_payment", language),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PubgOrange.copy(alpha = 0.15f) else DarkCard,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) PubgOrange else DarkCardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = method }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (method) {
                                        PaymentMethod.JAZZ_CASH -> "📱"
                                        PaymentMethod.EASYPAISA -> "💸"
                                        PaymentMethod.BANK_TRANSFER -> "🏦"
                                    },
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.URDU) method.titleUr else method.titleEn,
                                    color = if (isSelected) PubgOrangeBright else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 4. Payment Details Card (Configured by Admin)
            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, PubgOrange.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transfer Details (${selectedPaymentMethod.name})",
                                color = PubgOrangeBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Verified Store Account",
                                    color = SuccessGreen,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (selectedPaymentMethod) {
                            PaymentMethod.JAZZ_CASH -> {
                                PaymentDetailRow(context, "Account Title", settings.jazzCashTitle)
                                PaymentDetailRow(context, "JazzCash Number", settings.jazzCashNumber, canCopy = true)
                            }
                            PaymentMethod.EASYPAISA -> {
                                PaymentDetailRow(context, "Account Title", settings.easypaisaTitle)
                                PaymentDetailRow(context, "Easypaisa Number", settings.easypaisaNumber, canCopy = true)
                            }
                            PaymentMethod.BANK_TRANSFER -> {
                                PaymentDetailRow(context, "Bank Name", settings.bankName)
                                PaymentDetailRow(context, "Account Title", settings.bankAccountTitle)
                                PaymentDetailRow(context, "IBAN", settings.bankIban, canCopy = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ℹ️ Please transfer Rs. $finalTotal PKR to the account above, note the TID, and attach screenshot.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // 5. Transaction Proof Form
            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment Verification Details",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // TID Input
                        OutlinedTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            label = { Text(AppStrings.get("transaction_id", language)) },
                            placeholder = { Text("e.g. 984102847192") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PubgOrange,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Screenshot Upload Simulation Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasScreenshot) SuccessGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (hasScreenshot) SuccessGreen else DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    hasScreenshot = !hasScreenshot
                                    Toast.makeText(
                                        context,
                                        if (hasScreenshot) "Screenshot attached successfully!" else "Screenshot removed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (hasScreenshot) Icons.Filled.CheckCircle else Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Upload Screenshot",
                                    tint = if (hasScreenshot) SuccessGreen else PubgOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasScreenshot) "✅ Payment Receipt Screenshot Attached" else "📸 Attach Payment Screenshot / Receipt",
                                    color = if (hasScreenshot) SuccessGreen else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 6. Buyer Contact Details
            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Buyer Delivery Details",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = { buyerName = it },
                            label = { Text(AppStrings.get("buyer_name", language)) },
                            placeholder = { Text("e.g. Adnan or Ali") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PubgOrange,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = buyerPhone,
                            onValueChange = { buyerPhone = it },
                            label = { Text(AppStrings.get("buyer_phone", language)) },
                            placeholder = { Text("e.g. 0302-1234567") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PubgOrange,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = buyerEmail,
                            onValueChange = { buyerEmail = it },
                            label = { Text(AppStrings.get("buyer_email", language)) },
                            placeholder = { Text("optional, for receipt") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
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
        }
    }
}

@Composable
private fun PaymentDetailRow(
    context: android.content.Context,
    label: String,
    value: String,
    canCopy: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        if (canCopy) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DarkSurfaceVariant,
                border = BorderStroke(0.8.dp, PubgOrange.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { copyToClipboard(context, label, value) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = PubgOrange, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", color = PubgOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
