package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostScreen(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var workerCount by remember { mutableStateOf("") }
    var payPerWorker by remember { mutableStateOf("") }
    var isTermsAccepted by remember { mutableStateOf(false) }
    
    var showHistory by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var depositedBalance by remember { mutableStateOf(0.0) }
    var isLoadingUser by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    DisposableEffect(currentUserUid) {
        if (currentUserUid.isBlank()) {
            isLoadingUser = false
            onDispose {}
        } else {
            val listenerReg = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        depositedBalance = when (val value = snapshot.get("deposited")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                    }
                    isLoadingUser = false
                }
            onDispose {
                listenerReg.remove()
            }
        }
    }

    val totalCost = (workerCount.toIntOrNull() ?: 0) * (payPerWorker.toDoubleOrNull() ?: 0.0)

    if (showHistory) {
        JobPostHistoryDialog(onDismiss = { showHistory = false })
    }

    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Post a Job", color = Color.Black) },
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
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Job Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Job Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Job Description") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = link,
                                    onValueChange = { link = it },
                                    label = { Text("Job Link") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = workerCount,
                                        onValueChange = { workerCount = it },
                                        label = { Text("Workers Needed") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                    )
                                    OutlinedTextField(
                                        value = payPerWorker,
                                        onValueChange = { payPerWorker = it },
                                        label = { Text("Pay per Worker (৳)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Your Deposit Balance:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    Text(
                                        text = "৳${String.format("%.2f", depositedBalance)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (depositedBalance >= totalCost) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total Cost:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                    Text(
                                        text = "৳${String.format("%.2f", totalCost)}", 
                                        fontWeight = FontWeight.Bold, 
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isTermsAccepted, onCheckedChange = { isTermsAccepted = it })
                            Text("I accept the terms and conditions for posting a job.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (isSubmitting) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Button(
                                onClick = {
                                    val reqWorkers = workerCount.toIntOrNull() ?: 0
                                    val reqPay = payPerWorker.toDoubleOrNull() ?: 0.0
                                    val calculatedCost = reqWorkers * reqPay
                                    if (calculatedCost <= 0.0) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("অনুগ্রহ করে সঠিক কর্মী সংখ্যা ও কর্মী প্রতি পেমেন্ট লিখুন।")
                                        }
                                        return@Button
                                    }
                                    if (depositedBalance < calculatedCost) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("আপনার ডিপোজিট ব্যালেন্সে পর্যাপ্ত টাকা নেই। অনুগ্রহ করে প্রথমে ফান্ড ডিপোজিট করুন।")
                                        }
                                        return@Button
                                    }
                                    
                                    isSubmitting = true
                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    val userDocRef = db.collection("users").document(currentUserUid)

                                    db.runTransaction { transaction ->
                                        val userSnapshot = transaction.get(userDocRef)
                                        val currentDeposit = when (val value = userSnapshot.get("deposited")) {
                                            is Number -> value.toDouble()
                                            is String -> value.toDoubleOrNull() ?: 0.0
                                            else -> 0.0
                                        }
                                        if (currentDeposit < calculatedCost) {
                                            throw Exception("Insufficient deposit balance")
                                        }
                                        
                                        // Deduct cost
                                        val newDeposit = currentDeposit - calculatedCost
                                        transaction.update(userDocRef, "deposited", newDeposit)

                                        // Insert in jobs
                                        val jobDoc = db.collection("jobs").document()
                                        val jobId = jobDoc.id
                                        val jobData = hashMapOf(
                                            "id" to jobId,
                                            "userId" to currentUserUid,
                                            "title" to title,
                                            "description" to description,
                                            "link" to link,
                                            "workerCount" to reqWorkers,
                                            "payPerWorker" to reqPay,
                                            "totalCost" to calculatedCost,
                                            "status" to "Pending",
                                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )
                                        transaction.set(jobDoc, jobData)

                                        // Insert in job_posts for admin app layout backup
                                        val jobPostDoc = db.collection("job_posts").document(jobId)
                                        transaction.set(jobPostDoc, jobData)

                                        // Insert notification
                                        val notificationDoc = db.collection("notifications").document()
                                        val notificationData = hashMapOf(
                                            "id" to notificationDoc.id,
                                            "userId" to currentUserUid,
                                            "title" to "Job Post Submitted",
                                            "message" to "আপনার '$title' জবটি সাকসেসফুলি আপলোড হয়েছে। অনুগ্রহ করে এডমিন অ্যাপ্রভালের জন্য অপেক্ষা করুন।",
                                            "type" to "INFO",
                                            "isRead" to false,
                                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )
                                        transaction.set(notificationDoc, notificationData)
                                    }.addOnCompleteListener { taskResult ->
                                        isSubmitting = false
                                        if (taskResult.isSuccessful) {
                                            title = ""
                                            description = ""
                                            link = ""
                                            workerCount = ""
                                            payPerWorker = ""
                                            isTermsAccepted = false
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("আপনার জব পোস্ট সফলভাবে সাবমিট হয়েছে এবং পেমেন্ট কেটে নেওয়া হয়েছে।")
                                            }
                                        } else {
                                            val errMsg = taskResult.exception?.localizedMessage ?: "ত্রুটি ঘটেছে! অনুগ্রহ করে আবার চেষ্টা করুন।"
                                            coroutineScope.launch {
                                                if (errMsg.contains("Insufficient deposit balance")) {
                                                    snackbarHostState.showSnackbar("আপনার ডিপোজিট ব্যালেন্সে পর্যাপ্ত টাকা নেই।")
                                                } else {
                                                    snackbarHostState.showSnackbar("ত্রুটি: $errMsg")
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                enabled = isTermsAccepted && title.isNotBlank() && description.isNotBlank() && workerCount.isNotBlank() && payPerWorker.isNotBlank()
                            ) {
                                Text("Submit Job Post", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostHistoryDialog(onDismiss: () -> Unit) {
    var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("jobs")
                .whereEqualTo("userId", currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val map = doc.data ?: return@mapNotNull null
                            map + ("id" to doc.id)
                        }.sortedWith { a, b ->
                            val tsA = a["timestamp"] as? com.google.firebase.Timestamp
                            val tsB = b["timestamp"] as? com.google.firebase.Timestamp
                            val timeA = tsA?.toDate()?.time ?: 0L
                            val timeB = tsB?.toDate()?.time ?: 0L
                            timeB.compareTo(timeA)
                        }
                        historyList = list
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Job Posts History") },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
                    )
                }
            ) { paddingValues ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("No history yet.", color = androidx.compose.ui.graphics.Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyList) { job ->
                            val jTitle = job["title"] as? String ?: "Job Offer"
                            val jDesc = job["description"] as? String ?: ""
                            val pay = when (val value = job["payPerWorker"]) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val workers = when (val value = job["workerCount"]) {
                                is Number -> value.toInt()
                                is String -> value.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val calculatedCost = when (val value = job["totalCost"]) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val status = job["status"] as? String ?: "Pending"

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(jTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Workers: $workers | Pay: ৳${String.format("%.2f", pay)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("৳${String.format("%.2f", calculatedCost)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val statusColor = when (status) {
                                                "Approved", "Success" -> Color(0xFF4CAF50)
                                                "Pending" -> Color(0xFFFF9800)
                                                "Rejected", "Failed" -> Color(0xFFF44336)
                                                else -> Color.Gray
                                            }
                                            Text(status, color = statusColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    if (jDesc.isNotBlank()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        Text(jDesc, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 3)
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
