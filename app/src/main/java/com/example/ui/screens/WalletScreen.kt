package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class Transaction(
    val id: String,
    val type: String,
    val amount: Double,
    val date: String,
    val status: String, // Success, Pending, Failed
    val method: String,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen() {
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val transactions = emptyList<Transaction>() // Remove mock data

    if (showDepositDialog) {
        DepositDialog(onDismiss = { showDepositDialog = false })
    }
    
    if (showWithdrawDialog) {
        WithdrawDialog(availableBalance = 350.50, onDismiss = { showWithdrawDialog = false })
    }

    if (selectedTransaction != null) {
        TransactionDetailsDialog(transaction = selectedTransaction!!, onDismiss = { selectedTransaction = null })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                WalletCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                QuickActions(
                    onDeposit = { showDepositDialog = true },
                    onWithdraw = { showWithdrawDialog = true },
                    onTransfer = { /* TODO */ },
                    onHistory = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Earnings Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                EarningsOverview()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* TODO */ }) {
                        Text("See All")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(transactions) { transaction ->
                TransactionItem(transaction = transaction) {
                    selectedTransaction = transaction
                }
            }
        }
    }
}

@Composable
fun WalletCard() {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Available Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$350.50",
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WalletStatItem("Earnings", "$850.00")
                    WalletStatItem("Withdraw", "$500.00")
                    WalletStatItem("Deposit", "$150.00")
                }
            }
        }
    }
}

@Composable
fun WalletStatItem(label: String, amount: String) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp
        )
        Text(
            text = amount,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun QuickActions(
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onTransfer: () -> Unit,
    onHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton("Deposit", Icons.Outlined.ArrowDownward, Color(0xFF4CAF50), onDeposit)
        ActionButton("Withdraw", Icons.Outlined.ArrowUpward, Color(0xFFF44336), onWithdraw)
        ActionButton("Transfer", Icons.Outlined.SwapHoriz, Color(0xFF2196F3), onTransfer)
        ActionButton("History", Icons.Filled.History, Color(0xFFFF9800), onHistory)
    }
}

@Composable
fun ActionButton(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EarningsOverview() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EarningCard("Today", "$12.50", Modifier.weight(1f))
            EarningCard("Weekly", "$85.00", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EarningCard("Monthly", "$320.00", Modifier.weight(1f))
            EarningCard("Lifetime", "$1,250.00", Modifier.weight(1f))
        }
    }
}

@Composable
fun EarningCard(title: String, amount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    val statusColor = when (transaction.status) {
        "Success" -> Color(0xFF4CAF50)
        "Pending" -> Color(0xFFFF9800)
        "Failed" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val icon = when (transaction.type) {
        "Deposit" -> Icons.Outlined.ArrowDownward
        "Withdraw" -> Icons.Outlined.ArrowUpward
        "Earning" -> Icons.Filled.MonetizationOn
        else -> Icons.Filled.AttachMoney
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(statusColor.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = statusColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (transaction.type == "Withdraw") "-$${transaction.amount}" else "+$${transaction.amount}",
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == "Withdraw") Color.Red else Color(0xFF4CAF50),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = transaction.status, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositDialog(onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad", "Rocket", "Bank Transfer")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                TopAppBar(
                    title = { Text("Deposit Funds") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Payment Method", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    label = { Text("Transaction ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Submit Deposit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawDialog(availableBalance: Double, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad", "Rocket", "Bank Transfer")
    
    var errorText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                TopAppBar(
                    title = { Text("Withdraw Funds") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Balance", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$$availableBalance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        val reqAmount = it.toDoubleOrNull() ?: 0.0
                        if (reqAmount > availableBalance) {
                            errorText = "Amount exceeds available balance"
                        } else {
                            errorText = ""
                        }
                    },
                    label = { Text("Amount ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorText.isNotEmpty(),
                    supportingText = { if (errorText.isNotEmpty()) Text(errorText) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Withdraw Method", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    label = { Text("Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = amount.isNotBlank() && errorText.isEmpty() && accountNo.isNotBlank()
                ) {
                    Text("Confirm Withdraw", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsDialog(transaction: Transaction, onDismiss: () -> Unit) {
    val statusColor = when (transaction.status) {
        "Success" -> Color(0xFF4CAF50)
        "Pending" -> Color(0xFFFF9800)
        "Failed" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Transaction Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailRow("Transaction ID", transaction.id)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Type", transaction.type)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Amount", "$${transaction.amount}")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Date", transaction.date)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Method", transaction.method)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(transaction.status, fontWeight = FontWeight.Bold, color = statusColor)
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Notes", transaction.notes)
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
