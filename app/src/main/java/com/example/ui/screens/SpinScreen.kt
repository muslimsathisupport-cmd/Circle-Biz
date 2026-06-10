package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    var dailyLimit by remember { mutableIntStateOf(10) }
    var breakTimeMinutes by remember { mutableIntStateOf(5) }
    var rewardAmount by remember { mutableDoubleStateOf(0.5) }
    
    var userSpinsCount by remember { mutableIntStateOf(0) }
    var lastSpinTime by remember { mutableLongStateOf(0L) }
    var userBalance by remember { mutableDoubleStateOf(0.0) }
    
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var earnedReward by remember { mutableDoubleStateOf(0.0) }
    
    // Listen to admin settings
    LaunchedEffect(Unit) {
        db.collection("settings").document("spin_settings")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    dailyLimit = snapshot.getLong("daily_limit")?.toInt() ?: 10
                    breakTimeMinutes = snapshot.getLong("break_time")?.toInt() ?: 5
                    rewardAmount = snapshot.getDouble("reward_amount") ?: 0.5
                }
            }
        
        // Listen to user data
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userBalance = snapshot.getDouble("balance") ?: 0.0
                        userSpinsCount = snapshot.getLong("daily_spins_count")?.toInt() ?: 0
                        lastSpinTime = snapshot.getLong("last_spin_timestamp") ?: 0L
                        
                        // Reset count if it's a new day (simple check)
                        val lastDate = snapshot.getLong("last_spin_date") ?: 0L
                        val currentDate = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                        if (lastDate != currentDate) {
                            db.collection("users").document(userId).update(
                                "daily_spins_count", 0,
                                "last_spin_date", currentDate
                            )
                        }
                    }
                }
        }
    }

    val timeLeftMs = (lastSpinTime + (breakTimeMinutes * 60 * 1000)) - System.currentTimeMillis()
    val isRelaxing = timeLeftMs > 0
    val spinsLeft = dailyLimit - userSpinsCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lucky Spin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatCard("Balance", "৳${String.format("%.2f", userBalance)}", Color(0xFF4CAF50))
                StatCard("Spins Left", spinsLeft.toString(), Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Spin Wheel Container
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Wheel(rotationAngle)
                
                // Pointer
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-10).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(90f),
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (isRelaxing) {
                var countdown by remember { mutableStateOf(timeLeftMs / 1000) }
                LaunchedEffect(isRelaxing) {
                    while (countdown > 0) {
                        delay(1000)
                        countdown--
                    }
                }
                
                Text(
                    text = "Break Time: ${countdown / 60}m ${countdown % 60}s",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isSpinning && spinsLeft > 0 && !isRelaxing) {
                        isSpinning = true
                        scope.launch {
                            val extraRotation = 360f * 5 + Random.nextInt(360)
                            animate(
                                initialValue = rotationAngle,
                                targetValue = rotationAngle + extraRotation,
                                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                            ) { value, _ ->
                                rotationAngle = value
                            }
                            
                            isSpinning = false
                            earnedReward = rewardAmount
                            
                            // Update Firestore
                            db.runTransaction { transaction ->
                                val userDoc = db.collection("users").document(userId)
                                val snapshot = transaction.get(userDoc)
                                val currentBal = snapshot.getDouble("balance") ?: 0.0
                                val currentCount = snapshot.getLong("daily_spins_count")?.toInt() ?: 0
                                
                                transaction.update(userDoc, "balance", currentBal + earnedReward)
                                transaction.update(userDoc, "daily_spins_count", currentCount + 1)
                                transaction.update(userDoc, "last_spin_timestamp", System.currentTimeMillis())
                            }.addOnSuccessListener {
                                showRewardDialog = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !isSpinning && spinsLeft > 0 && !isRelaxing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Text(
                    text = if (isSpinning) "Spinning..." else if (spinsLeft <= 0) "Limit Reached" else "Spin Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (spinsLeft <= 0) {
                Text(
                    text = "You've reached your daily limit. Come back tomorrow!",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            confirmButton = {
                Button(onClick = { showRewardDialog = false }) {
                    Text("Great!")
                }
            },
            title = { Text("Congratulations!") },
            text = { Text("You've earned ৳${String.format("%.2f", earnedReward)} from your spin!") },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFFFC107)) }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun Wheel(rotation: Float) {
    val segments = 6
    val colors = listOf(Color(0xFFFF5722), Color(0xFFFFEB3B))
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .rotate(rotation)
    ) {
        val sweepAngle = 360f / segments
        for (i in 0 until segments) {
            drawArc(
                color = colors[i % 2],
                startAngle = i * sweepAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            // Draw segment numbers or dots
            val angle = (i * sweepAngle + sweepAngle / 2) * (PI / 180).toFloat()
            val radius = size.width / 2.5f
            val x = size.width / 2 + radius * cos(angle)
            val y = size.height / 2 + radius * sin(angle)
            
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        // Outer Border (Black Ring)
        drawCircle(
            color = Color(0xFF212121),
            radius = size.width / 2,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx())
        )
        
        // Dots on Black Ring
        val dotCount = 12
        for (i in 0 until dotCount) {
            val angle = (i * (360f/dotCount)) * (PI / 180).toFloat()
            val ringRadius = size.width / 2
            val x = size.width / 2 + ringRadius * cos(angle)
            val y = size.height / 2 + ringRadius * sin(angle)
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        // Inner Circle
        drawCircle(
            color = Color.White,
            radius = 20.dp.toPx()
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 8.dp.toPx()
        )
    }
}
