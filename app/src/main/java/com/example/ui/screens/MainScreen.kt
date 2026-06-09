package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.AdMobManager
import kotlinx.coroutines.launch

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        route = "home"
    ),
    BottomNavItem(
        title = "Tools",
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build,
        route = "tools"
    ),
    BottomNavItem(
        title = "Shop",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        route = "shop"
    ),
    BottomNavItem(
        title = "Wallet",
        selectedIcon = Icons.Filled.Wallet,
        unselectedIcon = Icons.Outlined.Wallet,
        route = "wallet"
    ),
    BottomNavItem(
        title = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        route = "profile"
    )
)

@Composable
fun MainScreen() {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp,
                containerColor = Color.White
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        label = { Box(modifier = Modifier.padding(top = 8.dp)) { Text(text = item.title) } },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.White,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        icon = {
                            Box(modifier = Modifier.padding(top = 12.dp)) {
                                Icon(
                                    imageVector = if (index == selectedItemIndex) {
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding).fillMaxSize()) {
            when (selectedItemIndex) {
                0 -> HomeScreen()
                1 -> ToolsScreen()
                2 -> ShopScreen()
                3 -> WalletScreen()
                4 -> ProfileScreen()
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars), 
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "$title Screen coming soon.", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    
    var adsWatched by remember { mutableStateOf(0) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }
    var lastCheckInTime by remember { mutableStateOf(0L) }
    val dailyRewardAmount = 2.0 // Configure from admin in a real app
    val requiredAdsForReward = 3
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
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
                        showingAdProgressDialog = false
                        adsWatched = 0
                        lastCheckInTime = System.currentTimeMillis()
                        snackbarMessage = "Daily Check-in successful! ৳$dailyRewardAmount added to your wallet."
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
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ad Progress: $adsWatched / $requiredAdsForReward watched. \nLoading next ad required for reward...",
                        textAlign = TextAlign.Center,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { handleAdReward() }) {
                        Text("Show Next Ad")
                    }
                }
            }
        }
    }

    // Showing Snackbar message
    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            // Optional: Dismiss after 3 secs
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(100.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "John Doe",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = "john.doe@example.com",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
            var showSettings by remember { mutableStateOf(false) }
            
            if (showSettings) {
                com.example.ui.screens.SettingsScreen(onBack = { showSettings = false })
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.OutlinedButton(onClick = { showSettings = true }) {
                Icon(Icons.Filled.Build, contentDescription = "Settings", modifier = Modifier.size(18.dp))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Text("Settings")
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Check-in Section
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CardGiftcard, contentDescription = "Gift", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Check-in",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Claim your daily reward of ৳$dailyRewardAmount by watching $requiredAdsForReward short ads.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentTime = System.currentTimeMillis()
                    val msIn24Hours = 24 * 60 * 60 * 1000
                    val canCheckIn = (currentTime - lastCheckInTime) > msIn24Hours
                    
                    Button(
                        onClick = { 
                            if (canCheckIn) {
                                handleAdReward()
                            } else {
                                snackbarMessage = "You have already checked in today! Try again later."
                            }
                        }, 
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canCheckIn
                    ) {
                        Text(if (canCheckIn) "Claim Daily Reward" else "Come back tomorrow")
                    }
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
        
        // Referral Section
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Refer & Earn",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Invite friends using your unique code. Both you and your friend earn an instant $5.00 bonus when they sign up and complete their first task!",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = "EARN-2026-XQZ",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Your Referral Code") },
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.IconButton(
                        onClick = { /* Copy to clipboard */ },
                        modifier = Modifier.background(
                            androidx.compose.material3.MaterialTheme.colorScheme.primary, 
                            shape = androidx.compose.foundation.shape.CircleShape
                        ).size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add, /* Should be ContentCopy, fallback to a placeholder if not imported. Will fix icon later. */
                            contentDescription = "Copy code",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Button(
                    onClick = { /* Share link */ }, 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Referral Link")
                }
            }
        }
        
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
    }
    }
    
    // SnackbarHost
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        if (snackbarMessage != null) {
            SnackbarHost(hostState = remember { SnackbarHostState() }.apply { 
                LaunchedEffect(snackbarMessage) { showSnackbar(snackbarMessage!!) }
            })
        }
    }
}

