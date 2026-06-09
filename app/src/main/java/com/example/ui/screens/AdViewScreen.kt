package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AdMobManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdViewScreen(task: EarningTask, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showHistory by remember { mutableStateOf(false) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }
    var lastWatchTime by remember { mutableStateOf(0L) }
    var adsWatched by remember { mutableStateOf(0) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Mock admin settings
    val dailyLimit = 15
    var viewsCompleted by remember { mutableStateOf(5) }
    val breakMinutes = 2
    val rewardAmount = 0.05
    val requiredAdsForReward = 3
    
    LaunchedEffect(Unit) {
        AdMobManager.loadRewardedAd(context)
    }

    if (showHistory) {
        AdHistoryDialog(onDismiss = { showHistory = false })
    }

    fun handleAdReward() {
        if (activity != null) {
            showingAdProgressDialog = true
            AdMobManager.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    adsWatched++
                    if (adsWatched >= requiredAdsForReward) {
                        showingAdProgressDialog = false
                        adsWatched = 0
                        viewsCompleted++
                        lastWatchTime = System.currentTimeMillis()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Ads completed! $$rewardAmount added to wallet.")
                        }
                    } else {
                        showingAdProgressDialog = true
                    }
                },
                onAdDismissed = {
                    showingAdProgressDialog = false
                    if (adsWatched < requiredAdsForReward) {
                        adsWatched = 0
                    }
                }
            )
        }
    }

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
                    CircularProgressIndicator(color = task.color)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ad Progress: $adsWatched / $requiredAdsForReward watched. \nLoading next ad required for reward...",
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

    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(task.title) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        },
                        actions = {
                            IconButton(onClick = { showHistory = true }) { Icon(Icons.Filled.History, contentDescription = "History") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Info, contentDescription = null, tint = task.color)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ad View Guidelines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text("Complete ad views to earn money directly to your wallet. Make sure to wait for the complete duration.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Reward Per Ad:", fontWeight = FontWeight.SemiBold)
                                    Text("$$rewardAmount", color = task.color, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Daily Limit:", fontWeight = FontWeight.SemiBold)
                                    Text("$viewsCompleted / $dailyLimit")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Break time between ads:", fontWeight = FontWeight.SemiBold)
                                    Text("$breakMinutes Minutes")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    item {
                        Button(
                            onClick = { 
                                val elapsedMins = (System.currentTimeMillis() - lastWatchTime) / (1000 * 60)
                                if (viewsCompleted >= dailyLimit) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Daily limit reached!") }
                                } else if (lastWatchTime > 0 && elapsedMins < breakMinutes) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Please wait ${breakMinutes - elapsedMins} more minutes.") }
                                } else {
                                    handleAdReward()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = viewsCompleted < dailyLimit
                        ) {
                            Text("Watch Ad & Earn", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdHistoryDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Ad View History") },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
                    )
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    item {
                        Text("No history yet.", modifier = Modifier.padding(16.dp), color = androidx.compose.ui.graphics.Color.Gray)
                    }
                }
            }
        }
    }
}
