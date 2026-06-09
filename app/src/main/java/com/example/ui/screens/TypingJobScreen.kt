package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.AdMobManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingJobScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var generatedText by remember { mutableStateOf(generateRandomString(8)) }
    var userInput by remember { mutableStateOf("") }
    var completedCount by remember { mutableStateOf(0) }
    var adsWatched by remember { mutableStateOf(0) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }
    var totalEarnings by remember { mutableStateOf(0.0) }
    var errorMessage by remember { mutableStateOf("") }

    val rewardPer5Tasks = 2.50 // Simulated admin configured amount
    val requiredTasksForAd = 5
    val requiredAdsForReward = 3

    // Preload ad when screen starts
    LaunchedEffect(Unit) {
        AdMobManager.loadRewardedAd(context)
    }

    fun handleAdReward() {
        if (activity != null) {
            showingAdProgressDialog = true
            AdMobManager.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    adsWatched++
                    if (adsWatched >= requiredAdsForReward) {
                        // User finished watching all required ads
                        adsWatched = 0
                        completedCount = 0
                        totalEarnings += rewardPer5Tasks
                        generatedText = generateRandomString(8)
                        showingAdProgressDialog = false
                    } else {
                        // Needs to watch more ads
                        // AdMobManager will load the next ad automatically
                        showingAdProgressDialog = true
                    }
                },
                onAdDismissed = {
                    // Ad was closed early or failed to load
                    showingAdProgressDialog = false
                    if (adsWatched < requiredAdsForReward) {
                        // Reset progress if they close before finishing all ads (or decide depending on logic)
                        adsWatched = 0
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Typing Job", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Progress", style = MaterialTheme.typography.labelMedium)
                        Text("$completedCount / $requiredTasksForAd", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Session Earned", style = MaterialTheme.typography.labelMedium)
                        Text("৳$totalEarnings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Type the exact text below", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Text to copy
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = generatedText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Try to match uppercase specifically for rigorous typing
            OutlinedTextField(
                value = userInput,
                onValueChange = { 
                    userInput = it
                    errorMessage = "" 
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Type here...") },
                singleLine = true,
                isError = errorMessage.isNotEmpty(),
                supportingText = if (errorMessage.isNotEmpty()) {
                    { Text(errorMessage) }
                } else null,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (userInput == generatedText) {
                        completedCount++
                        userInput = ""
                        if (completedCount >= requiredTasksForAd) {
                            handleAdReward()
                        } else {
                            generatedText = generateRandomString(8)
                        }
                    } else {
                        errorMessage = "Text does not match. Try again."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Submit Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Admin Note Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = "Info", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Admin Note: Users earn ৳$rewardPer5Tasks after successfully typing $requiredTasksForAd correct items, requiring watching $requiredAdsForReward ads.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }

    // Showing Ad Progress Dialog
    if (showingAdProgressDialog) {
        Dialog(onDismissRequest = { /* Cannot dismiss ad setup */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ad Progress: $adsWatched / $requiredAdsForReward watched. \nLoading next ad or processing reward...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { handleAdReward() }) {
                        Text("Show Next Ad")
                    }
                }
            }
        }
    }
}

private fun generateRandomString(length: Int): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
