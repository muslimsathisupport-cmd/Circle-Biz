package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRechargeScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("") }
    var showMessage by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val providers = listOf("Grameenphone", "Banglalink", "Robi", "Airtel", "Teletalk", "Skitto")
    val quickAmounts = listOf("20", "50", "100", "200", "500", "1000")

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
                        title = { Text("Mobile Recharge", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showHistory = true }) {
                                Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black
                        )
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Select Operator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(providers) { provider ->
                                val isSelected = selectedProvider == provider
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedProvider = provider },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = provider,
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount ৳", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickAmounts) { amt ->
                                Card(
                                    modifier = Modifier.clickable { amount = amt },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color.LightGray),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = "৳$amt",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        var isSubmitting by remember { mutableStateOf(false) }
                        val snackbarHostState = remember { SnackbarHostState() }
                        
                        Scaffold(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            containerColor = Color.Transparent
                        ) { _ ->
                            Button(
                                onClick = {
                                    val amtValue = amount.toDoubleOrNull() ?: 0.0
                                    if (phoneNumber.length >= 10 && amtValue > 0 && selectedProvider.isNotBlank()) {
                                        isSubmitting = true
                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        val currentUserUid = UserSession.getUid(context)
                                        
                                        val rechargeData = hashMapOf(
                                            "userId" to currentUserUid,
                                            "amount" to amtValue,
                                            "phoneNumber" to phoneNumber,
                                            "operator" to selectedProvider,
                                            "status" to "pending",
                                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp() // redundant but safe
                                        )
                                        
                                        db.collection("recharge_requests")
                                            .add(rechargeData)
                                            .addOnCompleteListener { task ->
                                                isSubmitting = false
                                                if (task.isSuccessful) {
                                                    coroutineScope.launch {
                                                        phoneNumber = ""
                                                        amount = ""
                                                        selectedProvider = ""
                                                        snackbarHostState.showSnackbar("Recharge request submitted successfully!")
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Error: ${task.exception?.localizedMessage}")
                                                    }
                                                }
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isSubmitting && phoneNumber.isNotBlank() && amount.isNotBlank() && selectedProvider.isNotBlank()
                            ) {
                                if (isSubmitting) {
                                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Recharge Now", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        
        if (showHistory) {
            Dialog(onDismissRequest = { showHistory = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
                    var isLoadingHistory by remember { mutableStateOf(true) }
                    val currentUserUid = UserSession.getUid(context)

                    androidx.compose.runtime.DisposableEffect(currentUserUid) {
                        if (currentUserUid.isNotBlank()) {
                            val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("recharge_requests")
                                .whereEqualTo("userId", currentUserUid)
                                .addSnapshotListener { snapshot, _ ->
                                    if (snapshot != null) {
                                        historyList = snapshot.documents.map { doc ->
                                            val data = doc.data ?: emptyMap<String, Any>()
                                            data + ("id" to doc.id)
                                        }.sortedByDescending { (it["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L }
                                    }
                                    isLoadingHistory = false
                                }
                            onDispose { listener.remove() }
                        } else {
                            isLoadingHistory = false
                            onDispose {}
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { Text("Recharge History") },
                            navigationIcon = {
                                IconButton(onClick = { showHistory = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                            }
                        )
                        
                        if (isLoadingHistory) {
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                        } else if (historyList.isEmpty()) {
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No recharge history found yet.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                items(historyList) { item ->
                                    val operator = item["operator"] as? String ?: ""
                                    val phone = item["phoneNumber"] as? String ?: ""
                                    val amt = item["amount"]?.toString() ?: "0"
                                    val status = item["status"] as? String ?: "pending"
                                    val ts = item["createdAt"] as? com.google.firebase.Timestamp
                                    val dateStr = ts?.toDate()?.let { 
                                        java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(it)
                                    } ?: ""

                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column {
                                                Text(operator, fontWeight = FontWeight.Bold)
                                                Text(phone, style = MaterialTheme.typography.bodySmall)
                                                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("৳$amt", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                val statusColor = when (status.lowercase()) {
                                                    "pending" -> Color(0xFFFF9800)
                                                    "approved", "success" -> Color(0xFF4CAF50)
                                                    else -> Color.Red
                                                }
                                                Text(status.replaceFirstChar { it.uppercase() }, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
