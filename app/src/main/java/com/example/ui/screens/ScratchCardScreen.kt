package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchCardScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()
    
    var userBalance by remember { mutableDoubleStateOf(0.0) }
    var scratchCardAmount by remember { mutableDoubleStateOf(0.0) }
    var isScratched by remember { mutableStateOf(false) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var isSavingReward by remember { mutableStateOf(false) }

    // Listen to user balance
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userBalance = snapshot.getDouble("balance") ?: 0.0
                    }
                }
        }
        // Generate a random reward for this session
        scratchCardAmount = listOf(0.1, 0.2, 0.5, 1.0, 2.0).random()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scratch & Win", fontWeight = FontWeight.Bold) },
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
            // Balance Card
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Current Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "৳${String.format("%.2f", userBalance)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Scratch the card below to reveal your bonus!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // The Scratch Card Implementation
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                // Background (The Reward)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Congratulations!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(
                        "৳${String.format("%.2f", scratchCardAmount)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50)
                    )
                    Text("Bonus Point Added", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                // Scratch Overlay
                val path = remember { mutableStateListOf<Offset>() }
                
                if (!isScratched) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        path.add(startOffset)
                                    }
                                ) { change, _ ->
                                    change.consume()
                                    path.add(change.position)
                                    
                                    // Simple logic to detect if enough is scratched
                                    if (path.size > 80 && !isScratched) {
                                        isScratched = true
                                        saveReward(db, userId, scratchCardAmount) {
                                            isSavingReward = false
                                            showRewardDialog = true
                                        }
                                    }
                                }
                            }
                    ) {
                        // Drawing the scratchable layer (the gold cover)
                        drawRect(
                            color = Color(0xFFFFD700),
                            size = Size(size.width, size.height)
                        )
                        
                        // Decorative pattern on top
                        val dotRadius = 4.dp.toPx()
                        for (x in 0..size.width.toInt() step 60) {
                            for (y in 0..size.height.toInt() step 60) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.2f),
                                    radius = dotRadius,
                                    center = Offset(x.toFloat(), y.toFloat())
                                )
                            }
                        }

                        // Erasing parts with transparency (Scratch effect)
                        path.forEach { pathOffset ->
                            drawCircle(
                                color = Color.Transparent,
                                radius = 60f,
                                center = pathOffset,
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
            
            if (isScratched) {
                Button(
                    onClick = {
                        // Reset for another go (optional, or just go back)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Collect More Rewards")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start scratching to win!", color = Color.Gray, fontSize = 12.sp)
                }
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
            title = { Text("Reward Claimed! 🎁") },
            text = { Text("You have successfully scratched and won ৳${String.format("%.2f", scratchCardAmount)} bonus!") }
        )
    }
}

private fun saveReward(db: FirebaseFirestore, userId: String, amount: Double, onComplete: () -> Unit) {
    if (userId.isEmpty()) return
    db.runTransaction { transaction ->
        val userRef = db.collection("users").document(userId)
        val snapshot = transaction.get(userRef)
        val currentBal = snapshot.getDouble("balance") ?: 0.0
        transaction.update(userRef, "balance", currentBal + amount)
    }.addOnCompleteListener {
        onComplete()
    }
}
