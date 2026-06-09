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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun MainScreen(onLogout: () -> Unit) {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(84.dp)
                    .navigationBarsPadding(),
                tonalElevation = 0.dp,
                containerColor = Color.White
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray
                        ),
                        icon = {
                            Box(modifier = Modifier.padding(top = 16.dp)) {
                                Icon(
                                    imageVector = if (index == selectedItemIndex) {
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(32.dp)
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
                4 -> ProfileScreen(onLogout = onLogout)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    
    var adsWatched by remember { mutableStateOf(0) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }
    var lastCheckInTime by remember { mutableStateOf(0L) }
    val dailyRewardAmount = 2.0 // Configure from admin in a real app
    val requiredAdsForReward = 3
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
    // Dynamic settings & user metrics loaded from Firestore
    val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    // Listen to real Firestore User Record
    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        firstName = snapshot.getString("firstName") ?: ""
                        lastName = snapshot.getString("lastName") ?: ""
                        mobile = snapshot.getString("mobile") ?: ""
                        balance = when (val value = snapshot.get("balance")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                    }
                    isLoadingProfile = false
                }
        } else {
            isLoadingProfile = false
        }
    }

    var showEditProfile by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                        
                        // Dynamically update user balance in Firestore
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val uDocRef = db.collection("users").document(currentUserUid)
                        db.runTransaction { tx ->
                            val userSnap = tx.get(uDocRef)
                            val currentBalance = when (val v = userSnap.get("balance")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            tx.update(uDocRef, "balance", currentBalance + dailyRewardAmount)
                        }.addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                snackbarMessage = "ডেইলি রিওয়ার্ড সফলভাবে দাবি করেছেন! ৳$dailyRewardAmount আপনার ওয়ালেটে যোগ হয়েছে।"
                            } else {
                                snackbarMessage = "রিওয়ার্ড যোগ করতে ব্যর্থ হয়েছে!"
                            }
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
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
    }

    // Modern Confirm Delete Account Dialog Block
    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("একাউন্ট ডিলেট করুন", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.Red) },
            text = { Text("আপনি কি নিশ্চিত যে আপনার অ্যাকাউন্টটি স্থায়ীভাবে ডিলিট করতে চান? ডিলিট করার সাথে সাথেই আপনার সব ব্যালেন্স, রেফারাল এবং একাউন্ট তথ্য সম্পূর্ণভাবে ফায়ারস্টোর সার্ভার থেকে মুছে ফেলা হবে।", lineHeight = 20.sp) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        // 1. Hard Delete User document from Firestore Database
                        db.collection("users").document(currentUserUid).delete()
                            .addOnCompleteListener { dbTask ->
                                // 2. Trigger Firebase Auth runtime deletion
                                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                user?.delete()?.addOnCompleteListener {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                } ?: run {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            }
                    }
                ) {
                    Text("হ্যাঁ, ডিলেট করুন", color = Color.Red, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    // FULL SCREEN PROFILE REWRITE DIALOG
    if (showEditProfile) {
        Dialog(
            onDismissRequest = { showEditProfile = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                var editFirstName by remember { mutableStateOf(firstName) }
                var editLastName by remember { mutableStateOf(lastName) }
                var editMobile by remember { mutableStateOf(mobile) }
                var isSaving by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        androidx.compose.material3.TopAppBar(
                            title = { Text("এডিট প্রোফাইল", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            navigationIcon = {
                                androidx.compose.material3.IconButton(onClick = { showEditProfile = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { padValues ->
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padValues)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = editFirstName,
                            onValueChange = { editFirstName = it },
                            label = { Text("First Name (প্রথম নাম)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = editLastName,
                            onValueChange = { editLastName = it },
                            label = { Text("Last Name (শেষ নাম)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = editMobile,
                            onValueChange = { editMobile = it },
                            label = { Text("Mobile Number (মোবাইল নম্বর)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            )
                        )

                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(40.dp))

                        if (isSaving) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = {
                                    isSaving = true
                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    db.collection("users").document(currentUserUid)
                                        .update(
                                            "firstName", editFirstName,
                                            "lastName", editLastName,
                                            "mobile", editMobile
                                        ).addOnCompleteListener { updateTask ->
                                            isSaving = false
                                            if (updateTask.isSuccessful) {
                                                firstName = editFirstName
                                                lastName = editLastName
                                                mobile = editMobile
                                                snackbarMessage = "প্রোফাইল সফলভাবে আপডেট করা হয়েছে! ✅"
                                                showEditProfile = false
                                            } else {
                                                snackbarMessage = "আপডেট ব্যর্থ হয়েছে: ❌ ${updateTask.exception?.localizedMessage}"
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = editFirstName.isNotBlank() && editLastName.isNotBlank() && editMobile.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = "Save"
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Profile", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
            }
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
            
            if (isLoadingProfile) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "${firstName} ${lastName}".trim().ifEmpty { "ব্যবহারকারী" },
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = mobile.ifEmpty { "মোবাইল নম্বর পাওয়া যায়নি" },
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ব্যালেন্স: ৳${String.format("%.2f", balance)}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
            
            var showSettings by remember { mutableStateOf(false) }
            
            if (showSettings) {
                com.example.ui.screens.SettingsScreen(onBack = { showSettings = false })
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
            
            // PROFILE ACTIONS CARDS (EDIT PROFILE & SYSTEM SETTINGS)
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                // Edit Profile Card
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .padding(horizontal = 4.dp),
                    onClick = { showEditProfile = true },
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "এডিট প্রোফাইল",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Settings Card
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .padding(horizontal = 4.dp),
                    onClick = { showSettings = true },
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = "Settings",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "সেটিংস",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            
            // LOGOUT AND DELETE ACCOUNT ROW
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                androidx.compose.material3.Button(
                    onClick = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.ExitToApp, contentDescription = "Logout")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text("লগ আউট", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }

                androidx.compose.material3.Button(
                    onClick = { showDeleteConfirm = true },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text("ডিলেট একাউন্ট")
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Check-in Section
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
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
                        Icon(Icons.Filled.CardGiftcard, contentDescription = "Gift", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Check-in",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Claim your daily reward of ৳$dailyRewardAmount by watching $requiredAdsForReward short ads.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentTime = System.currentTimeMillis()
                    val msIn24Hours =  24 * 60 * 60 * 1000
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

