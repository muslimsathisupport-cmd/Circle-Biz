package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Loading states
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Quiz inputs
    var quizReward by remember { mutableStateOf("2.50") }
    var quizBreak by remember { mutableStateOf("25") }

    // Typing inputs
    var typingReward by remember { mutableStateOf("2.50") }
    var typingBreak by remember { mutableStateOf("25") }

    // Ad View inputs
    var adRequired by remember { mutableStateOf("3") }
    var adReward by remember { mutableStateOf("0.15") }
    var adBreak by remember { mutableStateOf("10") }

    // Fetch existing settings
    LaunchedEffect(Unit) {
        db.collection("settings").document("quiz_settings")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    quizReward = snapshot.get("reward_amount")?.toString() ?: "2.50"
                    quizBreak = snapshot.get("break_duration")?.toString() ?: "25"
                }
            }

        db.collection("settings").document("typing_settings")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    typingReward = snapshot.get("reward_amount")?.toString() ?: "2.50"
                    typingBreak = snapshot.get("break_duration")?.toString() ?: "25"
                }
            }

        db.collection("settings").document("ad_settings")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    adRequired = snapshot.get("required_ads")?.toString() ?: "3"
                    adReward = snapshot.get("reward_amount")?.toString() ?: "0.15"
                    adBreak = snapshot.get("break_duration")?.toString() ?: "10"
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (!isLoading) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val qRev = quizReward.toDoubleOrNull()
                        val qBrk = quizBreak.toIntOrNull()
                        val tRev = typingReward.toDoubleOrNull()
                        val tBrk = typingBreak.toIntOrNull()
                        val aReq = adRequired.toIntOrNull()
                        val aRev = adReward.toDoubleOrNull()
                        val aBrk = adBreak.toIntOrNull()

                        if (qRev == null || qBrk == null || tRev == null || tBrk == null || aReq == null || aRev == null || aBrk == null) {
                            Toast.makeText(context, "দয়া করে সব ফাঁকা ঘর সঠিক নাম্বার দিয়ে পূরণ করুন!", Toast.LENGTH_LONG).show()
                            return@ExtendedFloatingActionButton
                        }

                        isSaving = true

                        // Save all settings to Firestore
                        val batch = db.batch()

                        val quizRef = db.collection("settings").document("quiz_settings")
                        batch.set(quizRef, mapOf(
                            "reward_amount" to qRev,
                            "break_duration" to qBrk
                        ))

                        val typingRef = db.collection("settings").document("typing_settings")
                        batch.set(typingRef, mapOf(
                            "reward_amount" to tRev,
                            "break_duration" to tBrk
                        ))

                        val adRef = db.collection("settings").document("ad_settings")
                        batch.set(adRef, mapOf(
                            "required_ads" to aReq,
                            "reward_amount" to aRev,
                            "break_duration" to aBrk
                        ))

                        batch.commit()
                            .addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context, "সেটিংস সফলভাবে আপডেট করা হয়েছে!", Toast.LENGTH_LONG).show()
                                onBack()
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "ব্যর্থ হয়েছে! ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    },
                    icon = { Icon(Icons.Filled.Save, contentDescription = "Save") },
                    text = { Text("Save Changes") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isSaving) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("নতুন সেটিংস ডাটাবেজে সংরক্ষণ করা হচ্ছে...", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quiz Config Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Gamepad, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("১. ইসলামিক কুইজ সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ইউজাররা প্রতি ৫টি কুইজ খেলার পর নিচের পরিমাপ অনুযায়ী বোনাস ব্যালেন্স এবং ব্রেক টাইম কাউন্টডাউন পাবে:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = quizReward,
                            onValueChange = { quizReward = it },
                            label = { Text("প্রতি ৫টি কুইজের পুরস্কার (৳ টাকা)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = quizBreak,
                            onValueChange = { quizBreak = it },
                            label = { Text("ব্রেক টাইম বিরতি (মিনিট)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                // Typing Config Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Keyboard, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("২. টাইপিং জব সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ইউজাররা ৫টি টাইপিং করার পর নিচের পরিমাপ অনুযায়ী বোনাস ব্যালেন্স এবং ব্রেক টাইম পাবে:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = typingReward,
                            onValueChange = { typingReward = it },
                            label = { Text("প্রতি ৫টি টাইপিং কাজের পুরস্কার (৳ টাকা)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = typingBreak,
                            onValueChange = { typingBreak = it },
                            label = { Text("ব্রেক টাইম বিরতি (মিনিট)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                // Ad View Config Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("৩. এড ভিউ ভিডিও সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ইউজার দৈনিক বিজ্ঞাপন দেখার পর নিচে নির্ধারিত প্রয়োজনীয় পরিমাপ অনুযায়ী ব্যালেন্স উপার্জন করবে:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adReward,
                            onValueChange = { adReward = it },
                            label = { Text("বিজ্ঞাপন দেখার পুরস্কার (৳ টাকা)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adRequired,
                            onValueChange = { adRequired = it },
                            label = { Text("প্রয়োজনীয় মোট বিজ্ঞাপন সংখ্যা (Required Ads)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adBreak,
                            onValueChange = { adBreak = it },
                            label = { Text("ব্রেক টাইম বিরতি (মিনিট)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
