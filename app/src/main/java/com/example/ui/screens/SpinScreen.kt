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

val spinRewards = listOf(
    Pair("৳5", 5.0),
    Pair("৳1", 1.0),
    Pair("৳0.50", 0.50),
    Pair("৳0.30", 0.30),
    Pair("৳0.20", 0.20),
    Pair("৳0.15", 0.15),
    Pair("৳0.10", 0.10),
    Pair("৳0.05", 0.05),
    Pair("৳0.02", 0.02),
    Pair("৳0.01", 0.01)
)

private fun rollSpin(): Int {
    val chance = Random.nextDouble()
    return when {
        chance < 0.01 -> 1 // ৳1 (1%)
        chance < 0.04 -> 2 // ৳0.50 (3%)
        chance < 0.10 -> 3 // ৳0.30 (6%)
        chance < 0.25 -> 4 // ৳0.20 (15%)
        chance < 0.40 -> 5 // ৳0.15 (15%)
        chance < 0.55 -> 6 // ৳0.10 (15%)
        chance < 0.70 -> 7 // ৳0.05 (15%)
        chance < 0.85 -> 8 // ৳0.02 (15%)
        else -> 9          // ৳0.01 (15%)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    var dailyLimit by remember { mutableIntStateOf(10) }
    var breakTimeMinutes by remember { mutableIntStateOf(5) }
    var isEnabled by remember { mutableStateOf(true) }
    
    var userSpinsCount by remember { mutableIntStateOf(0) }
    var lastSpinTime by remember { mutableLongStateOf(0L) }
    var userBalance by remember { mutableDoubleStateOf(0.0) }
    
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var earnedReward by remember { mutableDoubleStateOf(0.0) }
    var isSavingReward by remember { mutableStateOf(false) }
    
    // Listen to admin settings
    LaunchedEffect(Unit) {
        db.collection("settings").document("daily_spin")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    isEnabled = snapshot.getBoolean("is_enabled") ?: true
                    dailyLimit = snapshot.getLong("daily_limit")?.toInt() ?: 10
                    breakTimeMinutes = snapshot.getLong("break_time")?.toInt() ?: 5
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
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Lucky Spin", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
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

            if (!isEnabled) {
                Text(
                    text = "This feature is currently disabled.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else if (isRelaxing) {
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
                    if (!isSpinning && spinsLeft > 0 && !isRelaxing && isEnabled && !isSavingReward) {
                        isSpinning = true
                        isSavingReward = true
                        scope.launch {
                            val targetIndex = rollSpin()
                            val sweepAngle = 360f / spinRewards.size
                            val segmentCenterAngle = targetIndex * sweepAngle + sweepAngle / 2f
                            
                            var targetRot = 270f - segmentCenterAngle
                            targetRot = (targetRot % 360f + 360f) % 360f
                            
                            val normalizeCurrentRot = (rotationAngle % 360f + 360f) % 360f
                            var rotationDiff = targetRot - normalizeCurrentRot
                            if (rotationDiff < 0) rotationDiff += 360f
                            
                            val extraRotation = 360f * 5 + rotationDiff

                            animate(
                                initialValue = rotationAngle,
                                targetValue = rotationAngle + extraRotation,
                                animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
                            ) { value, _ ->
                                rotationAngle = value
                            }
                            
                            isSpinning = false
                            earnedReward = spinRewards[targetIndex].second
                            
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
                                isSavingReward = false
                                showRewardDialog = true
                            }.addOnFailureListener {
                                isSavingReward = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !isSpinning && spinsLeft > 0 && !isRelaxing && isEnabled && !isSavingReward,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Text(
                    text = if (!isEnabled) "Disabled" else if (isSpinning || isSavingReward) "Spinning..." else if (spinsLeft <= 0) "Limit Reached" else "Spin Now",
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
                    Text("Collect")
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
    val segments = spinRewards.size
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5), 
        Color(0xFF2196F3), Color(0xFF009688), Color(0xFF4CAF50), 
        Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF795548), 
        Color(0xFF607D8B)
    )
    
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .rotate(rotation)
    ) {
        val sweepAngle = 360f / segments
        for (i in 0 until segments) {
            drawArc(
                color = colors[i % colors.size],
                startAngle = i * sweepAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            // Draw text
            drawContext.canvas.nativeCanvas.save()
            val center = Offset(size.width / 2, size.height / 2)
            val angle = i * sweepAngle + sweepAngle / 2
            
            drawContext.canvas.nativeCanvas.translate(center.x, center.y)
            drawContext.canvas.nativeCanvas.rotate(angle)
            
            val textRadius = size.width / 2 * 0.7f
            drawContext.canvas.nativeCanvas.drawText(
                spinRewards[i].first,
                textRadius,
                textPaint.textSize / 3, // adjust vertically
                textPaint
            )
            drawContext.canvas.nativeCanvas.restore()
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

