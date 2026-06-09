package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val questions = listOf(
        QuizQuestion("What is the first pillar of Islam?", listOf("Salat", "Zakat", "Shahada", "Hajj"), 2),
        QuizQuestion("Which Prophet built the Kaaba?", listOf("Prophet Muhammad (SAW)", "Prophet Ibrahim (AS) & Ismail (AS)", "Prophet Musa (AS)", "Prophet Isa (AS)"), 1),
        QuizQuestion("In which month was the Quran revealed?", listOf("Muharram", "Rajab", "Dhul-Hijjah", "Ramadan"), 3),
        QuizQuestion("How many obligatory prayers are there in a day?", listOf("Three", "Four", "Five", "Six"), 2),
        QuizQuestion("What is the holy book of Islam?", listOf("Torah", "Zabur", "Injeel", "Quran"), 3)
    )

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var isCheckingAnswer by remember { mutableStateOf(false) }
    var adsWatched by remember { mutableStateOf(0) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val bonusAmount = 0.50 // Admin configured bonus
    val requiredAdsForReward = 3

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
                        showRewardDialog = true
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
                        "Ad Progress: $adsWatched / $requiredAdsForReward watched. \nLoading next ad...",
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

    if (showRewardDialog) {
        Dialog(onDismissRequest = onBack) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Congratulations!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("You got $score out of ${questions.size} correct.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reward Earned: $$bonusAmount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Claim & Go Back")
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Islamic Quiz") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    if (currentQuestionIndex < questions.size) {
                        val currentQuestion = questions[currentQuestionIndex]
                        
                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1) / questions.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "Question ${currentQuestionIndex + 1}/${questions.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            currentQuestion.question,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            val isSelected = selectedOptionIndex == index
                            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    if (!isCheckingAnswer) {
                                        selectedOptionIndex = index
                                    }
                                }
                            ) {
                                Text(
                                    text = optionText,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                if (selectedOptionIndex != null && !isCheckingAnswer) {
                                    isCheckingAnswer = true
                                    coroutineScope.launch {
                                        if (selectedOptionIndex == currentQuestion.correctOptionIndex) {
                                            score++
                                        }
                                        delay(800) // Small delay to show selection
                                        
                                        currentQuestionIndex++
                                        selectedOptionIndex = null
                                        isCheckingAnswer = false
                                        
                                        if (currentQuestionIndex >= questions.size) {
                                            handleAdReward()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = selectedOptionIndex != null && !isCheckingAnswer
                        ) {
                            Text(if (currentQuestionIndex == questions.size - 1) "Submit Quiz" else "Next Question", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
