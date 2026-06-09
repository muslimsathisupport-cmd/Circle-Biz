package com.example.ui.screens

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellTaskScreen(task: EarningTask, onBack: () -> Unit) {
    var accountInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var profileLinkInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var phoneNumberInput by remember { mutableStateOf("") }
    
    var isTermsAccepted by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showRules by remember { mutableStateOf(false) }
    
    val rewardPerSell = "10.00" // Configure from admin
    val dailyLimit = 5
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showHistory) {
        SubmissionHistoryDialog(onDismiss = { showHistory = false }, taskName = task.title)
    }

    if (showRules) {
        RulesDialog(onDismiss = { showRules = false }, task = task)
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text(task.title, color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showRules = true }) {
                                Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.Black)
                            }
                            IconButton(onClick = { showHistory = true }) {
                                Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black
                        )
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Reward Per Sell", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("৳$rewardPerSell", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daily Limit", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$dailyLimit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Form section
                    item {
                        Text(
                            "Submit Account Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        when {
                            task.title.contains("Gmail") -> {
                                OutlinedTextField(
                                    value = accountInput,
                                    onValueChange = { accountInput = it },
                                    label = { Text("Gmail Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = "SecurePass2026!@#",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Required Password (Copy to use)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                            }
                            task.title.contains("Facebook") -> {
                                OutlinedTextField(
                                    value = profileLinkInput,
                                    onValueChange = { profileLinkInput = it },
                                    label = { Text("Profile Link") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = accountInput,
                                    onValueChange = { accountInput = it },
                                    label = { Text("Username / Email / Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = "SecurePass2026!@#",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Required Password (Copy to use)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                            }
                            task.title.contains("Instagram") -> {
                                OutlinedTextField(
                                    value = accountInput,
                                    onValueChange = { accountInput = it },
                                    label = { Text("Email / Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = "SecurePass2026!@#",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Required Password (Copy to use)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                            }
                            task.title.contains("WhatsApp") || task.title.contains("Telegram") -> {
                                OutlinedTextField(
                                    value = phoneNumberInput,
                                    onValueChange = { phoneNumberInput = it },
                                    label = { Text("Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                            }
                            else -> {
                                OutlinedTextField(
                                    value = accountInput,
                                    onValueChange = { accountInput = it },
                                    label = { Text("Account Identifier") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isTermsAccepted,
                                onCheckedChange = { isTermsAccepted = it }
                            )
                            Text(
                                "I verify that I have followed all rules provided in the info section.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val isFormValid = when {
                            task.title.contains("Gmail") -> accountInput.isNotBlank()
                            task.title.contains("Facebook") -> profileLinkInput.isNotBlank() && accountInput.isNotBlank()
                            task.title.contains("Instagram") -> accountInput.isNotBlank()
                            task.title.contains("WhatsApp") || task.title.contains("Telegram") -> phoneNumberInput.isNotBlank()
                            else -> accountInput.isNotBlank()
                        } && isTermsAccepted

                        Button(
                            onClick = {
                                if (isFormValid) {
                                    coroutineScope.launch {
                                        try {
                                            val data = hashMapOf(
                                                "taskTitle" to task.title,
                                                "accountIdentifier" to accountInput,
                                                "profileLink" to profileLinkInput,
                                                "username" to usernameInput,
                                                "phoneNumber" to phoneNumberInput,
                                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )
                                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                                                data["userId"] = uid
                                            }
                                            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("submissions").add(data)
                                            // Reset fields
                                            accountInput = ""
                                            passwordInput = ""
                                            profileLinkInput = ""
                                            usernameInput = ""
                                            phoneNumberInput = ""
                                            isTermsAccepted = false
                                            snackbarHostState.showSnackbar("Submission sent for review. Check history for updates.")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Failed to submit. ${e.message}")
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please fill all fields and accept terms.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = isFormValid,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Submit Account", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TermsItem(text: String, tint: Color = Color(0xFFE53935)) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle, 
            contentDescription = null, 
            tint = tint,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionHistoryDialog(onDismiss: () -> Unit, taskName: String) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("$taskName History", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black
                        )
                    )
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    item {
                        Text("No history yet.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(account: String, status: String, statusColor: Color, reward: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(status, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
            Text(reward, fontWeight = FontWeight.Bold, color = if (reward != "Waiting" && !reward.startsWith("$0")) Color(0xFF4CAF50) else Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesDialog(onDismiss: () -> Unit, task: EarningTask) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Rules & Guidelines", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.Black)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black
                        )
                    )
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                    if (!task.title.contains("WhatsApp") && !task.title.contains("Telegram")) {
                        item {
                            Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Assignment, contentDescription = null, tint = task.color)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Instructions & Required Password", 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text(
                                    "When creating the ${task.title.replace(" Sell", "")} account, you MUST use the following password:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = "SecurePass2026!@#",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = task.color
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Terms and conditions", 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                
                                TermsItem("Each account must be new and never used on our platform before.")
                                TermsItem("Follow the exact format and country rules given in the instructions.")
                                TermsItem("Wrong, duplicate, or recycled accounts can be rejected without pay.")
                                TermsItem("Do not submit accounts you do not fully control.")
                                TermsItem("Obey applicable laws and platform policies for every submission.")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Warning:", 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                                TermsItem("Using fake, stolen, or duplicate accounts can permanently block your profile.", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}