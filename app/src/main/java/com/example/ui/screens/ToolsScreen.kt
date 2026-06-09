package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Base64
import kotlin.random.Random

data class AppTool(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen() {
    var selectedTool by remember { mutableStateOf<AppTool?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val toolsList = listOf(
        AppTool("Calculator", Icons.Filled.Calculate, Color(0xFFE91E63)),
        AppTool("Password Gen", Icons.Filled.Password, Color(0xFF9C27B0)),
        AppTool("Unit Conv", Icons.Filled.SyncAlt, Color(0xFF673AB7)),
        AppTool("Word Counter", Icons.Filled.TextFormat, Color(0xFF3F51B5)),
        AppTool("Rand Number", Icons.Filled.Casino, Color(0xFF2196F3)),
        AppTool("Quick Note", Icons.Filled.NoteAlt, Color(0xFF03A9F4)),
        AppTool("BMI Calc", Icons.Filled.FitnessCenter, Color(0xFF00BCD4)),
        AppTool("Stopwatch", Icons.Filled.Timer, Color(0xFF009688)),
        AppTool("Base64 Code", Icons.Filled.Code, Color(0xFF4CAF50)),
        AppTool("Age Calc", Icons.Filled.CalendarMonth, Color(0xFF8BC34A))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Utility Tools", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Essential Free Tools", 
                style = MaterialTheme.typography.titleMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(toolsList) { tool ->
                    ToolItemCard(tool = tool) {
                        selectedTool = tool
                    }
                }
            }
        }
    }
    
    if (selectedTool != null) {
        when (selectedTool!!.title) {
            "Password Gen" -> PasswordGeneratorDialog(tool = selectedTool!!, onDismiss = { selectedTool = null })
            "Word Counter" -> WordCounterDialog(tool = selectedTool!!, onDismiss = { selectedTool = null })
            "Rand Number" -> RandomNumberDialog(tool = selectedTool!!, onDismiss = { selectedTool = null })
            "Base64 Code" -> Base64Dialog(tool = selectedTool!!, onDismiss = { selectedTool = null })
            "Age Calc" -> GenericToolDialog(tool = selectedTool!!, onDismiss = { selectedTool = null }) // Can be implemented fully later
            else -> GenericToolDialog(tool = selectedTool!!, onDismiss = { selectedTool = null })
        }
    }
}

@Composable
fun ToolItemCard(tool: AppTool, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(tool.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = tool.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun PasswordGeneratorDialog(tool: AppTool, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(12f) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(tool.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = password.ifEmpty { "Click Generate" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Length: ${length.toInt()}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = length,
                    onValueChange = { length = it },
                    valueRange = 8f..32f,
                    steps = 24
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()"
                        password = (1..length.toInt()).map { chars.random() }.joinToString("")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun WordCounterDialog(tool: AppTool, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    
    val words = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    val chars = text.length
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(tool.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(words.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Words", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(chars.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("Characters", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("Type or paste text here...") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { text = "" }) { Text("Clear") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
fun RandomNumberDialog(tool: AppTool, onDismiss: () -> Unit) {
    var minStr by remember { mutableStateOf("1") }
    var maxStr by remember { mutableStateOf("100") }
    var result by remember { mutableStateOf<Int?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(tool.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = result?.toString() ?: "-",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minStr,
                        onValueChange = { minStr = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = maxStr,
                        onValueChange = { maxStr = it },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val min = minStr.toIntOrNull() ?: 1
                        val max = maxStr.toIntOrNull() ?: 100
                        if (min <= max) {
                            result = Random.nextInt(min, max + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Number")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun Base64Dialog(tool: AppTool, onDismiss: () -> Unit) {
    var isEncode by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(tool.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = isEncode,
                        onClick = { isEncode = true; result = "" },
                        label = { Text("Encode") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !isEncode,
                        onClick = { isEncode = false; result = "" },
                        label = { Text("Decode") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Input Text") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        try {
                            if (isEncode) {
                                result = Base64.getEncoder().encodeToString(text.toByteArray())
                            } else {
                                result = String(Base64.getDecoder().decode(text))
                            }
                        } catch (e: Exception) {
                            result = "Error processing text"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isEncode) "Encode" else "Decode")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = result,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Result") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

@Composable
fun GenericToolDialog(tool: AppTool, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tool: ${tool.title}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Coming soon...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}
