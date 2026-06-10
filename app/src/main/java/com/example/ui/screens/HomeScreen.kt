package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.SpecialEarningCard
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable

data class EarningTask(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

data class Banner(
    val imageUrl: String = "",
    val targetUrl: String = "",
    val isActive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    var selectedTask by remember { mutableStateOf<EarningTask?>(null) }
    var showNotifications by remember { mutableStateOf(false) }
    
    var unreadNotificationsCount by remember { mutableStateOf(0) }
    var firebaseBanners by remember { mutableStateOf<List<Banner>>(emptyList()) }
    var dailyCheckInEnabled by remember { mutableStateOf(true) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUserUid = UserSession.getUid(context)

    androidx.compose.runtime.DisposableEffect(currentUserUid) {
        if (currentUserUid.isBlank()) {
            onDispose {}
        } else {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            val notifListener = db.collection("notifications")
                .whereEqualTo("userId", currentUserUid)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        unreadNotificationsCount = snapshot.size()
                    }
                }
                
            val bannerListener = db.collection("banners")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirebaseBanners", "Listener failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = mutableListOf<Pair<Banner, Any?>>()
                        for (doc in snapshot.documents) {
                            try {
                                val imgUrl = doc.getString("imageUrl") 
                                    ?: doc.getString("image_url") 
                                    ?: doc.getString("image") 
                                    ?: ""
                                val tgtUrl = doc.getString("targetUrl") 
                                    ?: doc.getString("target_url") 
                                    ?: doc.getString("url") 
                                    ?: ""
                                
                                var activeVal = true
                                if (doc.contains("isActive")) {
                                    activeVal = doc.getBoolean("isActive") 
                                        ?: (doc.getString("isActive")?.lowercase() == "true")
                                } else if (doc.contains("is_active")) {
                                    activeVal = doc.getBoolean("is_active") 
                                        ?: (doc.getString("is_active")?.lowercase() == "true")
                                } else if (doc.contains("active")) {
                                    activeVal = doc.getBoolean("active") 
                                        ?: (doc.getString("active")?.lowercase() == "true")
                                }
                                
                                val createdAt = doc.get("createdAt")
                                
                                if (imgUrl.isNotBlank() && activeVal) {
                                    list.add(Pair(Banner(imageUrl = imgUrl, targetUrl = tgtUrl, isActive = activeVal), createdAt))
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("FirebaseBanners", "Error parsing banner doc: ${doc.id}", e)
                            }
                        }
                        
                        // Sort by createdAt descending
                        val sortedBanners = list.sortedWith { a, b ->
                            val timeA = a.second
                            val timeB = b.second
                            when {
                                timeA == null && timeB == null -> 0
                                timeA == null -> 1
                                timeB == null -> -1
                                timeA is com.google.firebase.Timestamp && timeB is com.google.firebase.Timestamp -> {
                                    timeB.compareTo(timeA)
                                }
                                timeA is java.util.Date && timeB is java.util.Date -> {
                                    timeB.compareTo(timeA)
                                }
                                timeA is Number && timeB is Number -> {
                                    timeB.toDouble().compareTo(timeA.toDouble())
                                }
                                else -> {
                                    timeB.toString().compareTo(timeA.toString())
                                }
                            }
                        }.map { it.first }
                        
                        firebaseBanners = sortedBanners
                    }
                }

            val checkInListener = db.collection("settings").document("daily_checkin")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        dailyCheckInEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: dailyCheckInEnabled
                    }
                }

            val checkInListener2 = db.collection("settings").document("checkin_settings")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        dailyCheckInEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: dailyCheckInEnabled
                    }
                }
                
            onDispose {
                notifListener.remove()
                bannerListener.remove()
                checkInListener.remove()
                checkInListener2.remove()
            }
        }
    }
    
    val tasks = mutableListOf(
        EarningTask("Mobile Recharge", Icons.Filled.PhoneAndroid, Color(0xFF4CAF50)),
        EarningTask("Drive Offer", Icons.Filled.LocalOffer, Color(0xFF2196F3)),
        EarningTask("Reselling", Icons.Filled.ShoppingBag, Color(0xFFFF9800)),
        EarningTask("Blood", Icons.Filled.AddCircle, Color(0xFFF44336)),
        EarningTask("Micro Job", Icons.Filled.WorkHistory, Color(0xFFE91E63)),
        EarningTask("Gmail Sell", Icons.Filled.Email, Color(0xFFF44336)),
        EarningTask("Facebook Sell", Icons.Filled.ThumbUp, Color(0xFF1877F2)),
        EarningTask("Instagram Sell", Icons.Filled.AccountCircle, Color(0xFFE1306C)),
        EarningTask("WhatsApp Sell", Icons.Filled.Phone, Color(0xFF25D336)),
        EarningTask("Telegram Sell", Icons.Filled.Send, Color(0xFF0088CC)),
        EarningTask("Job Posts", Icons.Filled.PostAdd, Color(0xFFFF5722)),
        EarningTask("Quiz Job", Icons.Filled.QuestionMark, Color(0xFFFFC107)),
        EarningTask("Typing Job", Icons.Filled.Keyboard, Color(0xFF9C27B0)),
        EarningTask("Ad View", Icons.Filled.PlayCircle, Color(0xFF673AB7))
    )

    var isExpanded by remember { mutableStateOf(false) }
    
    val visibleTasks = if (isExpanded) tasks else tasks.take(12)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "CircleBiz",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            actions = {
                IconButton(onClick = { 
                    showNotifications = true
                    unreadNotificationsCount = 0
                }) {
                    androidx.compose.material3.BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                androidx.compose.material3.Badge { Text(unreadNotificationsCount.toString()) }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Banner Slider
        val pagerState = rememberPagerState(pageCount = { 
            if (firebaseBanners.isEmpty()) 1 else firebaseBanners.size 
        })
        
        // Auto-sliding logic
        LaunchedEffect(firebaseBanners) {
            if (firebaseBanners.size > 1) {
                while (true) {
                    yield()
                    delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % firebaseBanners.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            ) { page ->
                if (firebaseBanners.isEmpty()) {
                    // Placeholder while loading or if empty
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    val banner = firebaseBanners[page]
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = "Promo Banner",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (banner.targetUrl.isNotBlank()) {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(banner.targetUrl)
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("HomeScreen", "Failed to open target url", e)
                                    }
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Pager Indicator
            if (firebaseBanners.size > 1) {
                Row(
                    Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(firebaseBanners.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Earning Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            androidx.compose.material3.TextButton(
                onClick = { isExpanded = !isExpanded },
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Show Less" else "View All",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Grid of services (Manual implementation for scrollability)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            visibleTasks.chunked(4).forEach { rowTasks ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTasks.forEach { task ->
                        Box(modifier = Modifier.weight(1f)) {
                            EarningTaskItem(task, onClick = { selectedTask = task })
                        }
                    }
                    // Fill empty spaces in the last row if needed
                    repeat(4 - rowTasks.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Special Earning Section
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Special Earning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SpecialEarningCard(
                        title = "Daily Spin",
                        subtitle = "Earn ৳0.50 per spin",
                        icon = Icons.Filled.PlayCircle,
                        bgColor = Color(0xFF673AB7),
                        onClick = { selectedTask = EarningTask("Daily Spin", Icons.Filled.PlayCircle, Color(0xFF673AB7)) }
                    )
                }
                item {
                    SpecialEarningCard(
                        title = "Scratch Card",
                        subtitle = "Scratch & Win Coins",
                        icon = Icons.Filled.CardGiftcard,
                        bgColor = Color(0xFFFF9800),
                        onClick = { selectedTask = EarningTask("Scratch Card", Icons.Filled.CardGiftcard, Color(0xFFFF9800)) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (selectedTask != null) {
        if (selectedTask!!.title == "Mobile Recharge") {
            MobileRechargeScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Quiz Job") {
            com.example.ui.screens.QuizScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Ad View") {
            com.example.ui.screens.AdViewScreen(task = selectedTask!!, onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Drive Offer") {
            com.example.ui.screens.DriveOfferScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Job Posts") {
            com.example.ui.screens.JobPostScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Micro Job") {
            com.example.ui.screens.MicroJobScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Daily Spin") {
            com.example.ui.screens.SpinScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Scratch Card") {
            com.example.ui.screens.ScratchCardScreen(onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Reselling" || selectedTask!!.title == "Blood") {
            ComingSoonBottomSheet(task = selectedTask!!, onDismiss = { selectedTask = null })
        } else if (selectedTask!!.title.contains("Sell")) {
            com.example.ui.screens.SellTaskScreen(task = selectedTask!!, onBack = { selectedTask = null })
        } else if (selectedTask!!.title == "Typing Job") {
            com.example.ui.screens.TypingJobScreen(onBack = { selectedTask = null })
        } else {
            TaskVerificationBottomSheet(
                task = selectedTask!!,
                onDismiss = { selectedTask = null }
            )
        }
    }
    
    if (showNotifications) {
        com.example.ui.screens.NotificationScreen(onBack = { showNotifications = false })
    }
}

@Composable
fun EarningTaskItem(task: EarningTask, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = task.color),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.size(52.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = task.icon,
                    contentDescription = task.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            minLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskVerificationBottomSheet(task: EarningTask, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = task.icon,
                contentDescription = null,
                tint = task.color,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Verify Task: ${task.title}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contextual Verification UI based on task type
            when (task.title) {
                "Blood" -> {
                    Text(
                        "Please upload a screenshot confirming you have completed the task requirements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { /* Handle image pick */ },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = "Upload", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to upload screenshot proof", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                "Quiz Job" -> {
                    Text(
                        "Please submit your final score or the required completion phrase from the quiz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Completion Code / Score") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Complete Verification", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit for Verification")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.material3.TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.ReportProblem, contentDescription = "Report Dispute", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Issue / Dispute Task", color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonBottomSheet(task: EarningTask, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(task.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = task.icon,
                    contentDescription = null,
                    tint = task.color,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "কামিং সুন (Coming Soon)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = task.color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "এই ফিচারটি নিয়ে খুব শীঘ্রই কাজ করা হচ্ছে। নতুন আপডেট আসার সাথে সাথেই আপনি ক্যাটাগরি থেকে ইনকাম করতে পারবেন। আমাদের সাথেই থাকুন!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = task.color)
            ) {
                Text(
                    text = "ঠিক আছে",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


