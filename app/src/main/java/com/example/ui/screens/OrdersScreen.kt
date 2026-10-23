package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoreRepository
import com.example.model.*
import com.example.ui.components.copyToClipboard
import com.example.ui.components.openWhatsAppChat
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(
    repository: StoreRepository,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val orders by repository.orders.collectAsState()
    val language by repository.language.collectAsState()
    val settings by repository.storeSettings.collectAsState()

    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

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
                        text = AppStrings.get("order_tracking", language),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (language == AppLanguage.URDU) "آپ کے آرڈرز اور سیکیور کریڈینشلز" else "Track your delivery status & view unlocked account credentials",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = "No Orders",
                        tint = TextMuted,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "ابھی تک کوئی آرڈر نہیں ہے" else "No orders placed yet",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "پب جی اکاؤنٹس دیکھیں اور اپنا آرڈر کریں" else "Browse our catalog and buy your dream PUBG account with 100% safety.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = PubgOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.URDU) "اسٹور دیکھیں" else "Browse Store",
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
                items(orders, key = { it.id }) { order ->
                    OrderItemCard(
                        order = order,
                        language = language,
                        settings = settings,
                        onDisputeClick = {
                            openWhatsAppChat(context, settings.whatsappNumber, "Dispute / Order Help: ${order.accountTitle}", order.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(
    order: Order,
    language: AppLanguage,
    settings: StoreSettings,
    onDisputeClick: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    val formattedDate = remember(order.orderTimestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(order.orderTimestamp))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(
            1.dp,
            when (order.status) {
                OrderStatus.ACCOUNT_DELIVERED, OrderStatus.COMPLETED -> SuccessGreen.copy(alpha = 0.6f)
                OrderStatus.PAYMENT_VERIFIED -> PubgOrange.copy(alpha = 0.6f)
                OrderStatus.PENDING -> PendingYellow.copy(alpha = 0.5f)
                else -> DarkCardBorder
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        color = PubgOrangeBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = formattedDate,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                OrderStatusPill(status = order.status, language = language)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Account Title
            Text(
                text = order.accountTitle,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Amount & Payment Method Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment: ${order.paymentMethod.titleEn} (TID: ${order.transactionId})",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = "Rs. ${order.finalAmountPkr} PKR",
                    color = PubgOrange,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Step Pipeline Visual Tracker
            OrderPipelineProgress(status = order.status)

            Spacer(modifier = Modifier.height(14.dp))

            // CRITICAL: Credentials Section (Locked or Unlocked!)
            val isUnlocked = order.status == OrderStatus.ACCOUNT_DELIVERED || order.status == OrderStatus.COMPLETED
            val credentials = order.deliveredCredentials

            if (!isUnlocked || credentials == null) {
                // LOCKED CREDENTIALS STATE
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, PendingYellow.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = PendingYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Credentials Encrypted & Hidden",
                                color = PendingYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.get("credentials_locked_notice", language),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                // UNLOCKED CREDENTIALS (REVEALED TO BUYER ONLY!)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.5.dp, SuccessGreen.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Key,
                                    contentDescription = "Key",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.get("credentials_unlocked_title", language),
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Platform
                        CredentialFieldRow(
                            label = "Login Platform",
                            value = credentials.loginPlatform,
                            onCopy = { copyToClipboard(context, "Platform", credentials.loginPlatform) }
                        )

                        // Username / Email
                        CredentialFieldRow(
                            label = "Username / Email",
                            value = credentials.usernameOrEmail,
                            onCopy = { copyToClipboard(context, "Username", credentials.usernameOrEmail) }
                        )

                        // Password (with Toggle & Copy)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Password", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = if (showPassword) credentials.password else "••••••••••••",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { copyToClipboard(context, "Password", credentials.password) }) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = PubgOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 2FA Secret
                        if (credentials.twoFactorSecret.isNotBlank()) {
                            CredentialFieldRow(
                                label = "2FA Backup Secret",
                                value = credentials.twoFactorSecret,
                                onCopy = { copyToClipboard(context, "2FA", credentials.twoFactorSecret) }
                            )
                        }

                        // Security Guidance
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚠️ Immediate Action Required:",
                                    color = PubgGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = credentials.transferNotes,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Help & WhatsApp Support buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDisputeClick,
                    border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Chat,
                        contentDescription = "Help",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Need Help on WhatsApp",
                        color = Color(0xFF25D366),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CredentialFieldRow(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Text(
                value,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        IconButton(onClick = onCopy) {
            Icon(
                Icons.Filled.ContentCopy,
                contentDescription = "Copy",
                tint = PubgOrange,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun OrderStatusPill(status: OrderStatus, language: AppLanguage) {
    val (bgColor, textColor, label) = when (status) {
        OrderStatus.PENDING -> Triple(PendingYellow.copy(alpha = 0.2f), PendingYellow, if (language == AppLanguage.URDU) "تصدیق باقی ہے" else "Pending")
        OrderStatus.PAYMENT_VERIFIED -> Triple(PubgOrange.copy(alpha = 0.2f), PubgOrange, if (language == AppLanguage.URDU) "ادائیگی تصدیق شدہ" else "Payment Verified")
        OrderStatus.ACCOUNT_DELIVERED -> Triple(SuccessGreen.copy(alpha = 0.2f), SuccessGreen, if (language == AppLanguage.URDU) "ڈیلیور شدہ" else "Delivered")
        OrderStatus.COMPLETED -> Triple(SuccessGreen.copy(alpha = 0.25f), SuccessGreen, if (language == AppLanguage.URDU) "مکمل شدہ" else "Completed")
        OrderStatus.REJECTED -> Triple(AceRed.copy(alpha = 0.2f), AceRed, if (language == AppLanguage.URDU) "مسترد" else "Rejected")
        OrderStatus.REFUND_REQUESTED -> Triple(PendingYellow.copy(alpha = 0.2f), PendingYellow, if (language == AppLanguage.URDU) "واپسی درخواست" else "Refund Req")
        OrderStatus.REFUNDED -> Triple(TextSecondary.copy(alpha = 0.2f), TextSecondary, if (language == AppLanguage.URDU) "واپس شدہ" else "Refunded")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun OrderPipelineProgress(status: OrderStatus) {
    val currentStep = status.stepIndex

    val steps = listOf("Pending", "Verified", "Delivered", "Completed")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, stepName ->
                val isDone = currentStep >= index && currentStep >= 0
                val isCurrent = currentStep == index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(68.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> SuccessGreen
                                    isCurrent -> PubgOrange
                                    else -> DarkCardBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) CharcoalBlack else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stepName,
                        color = if (isDone || isCurrent) TextPrimary else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (currentStep > index && currentStep >= 0) SuccessGreen else DarkCardBorder
                            )
                    )
                }
            }
        }
    }
}
