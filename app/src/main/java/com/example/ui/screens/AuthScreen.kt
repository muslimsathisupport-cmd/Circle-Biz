package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isLogin) {
            LoginScreen(
                onNavigateToSignUp = { isLogin = false },
                onLoginSuccess = onLoginSuccess
            )
        } else {
            SignUpScreen(
                onNavigateToLogin = { isLogin = true },
                onSignUpSuccess = onLoginSuccess
            )
        }
    }
}

@Composable
fun LoginScreen(onNavigateToSignUp: () -> Unit, onLoginSuccess: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Slim, slightly rounded inputs
    val inputModifier = Modifier
        .fillMaxWidth()

    val inputShape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number") },
            modifier = inputModifier,
            shape = inputShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = inputModifier,
            shape = inputShape,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (mobileNumber.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    val email = if (mobileNumber.contains("@")) mobileNumber else "$mobileNumber@user.com"
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isLoading = false
                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                // Try fallback check in Firestore users collection
                                db.collection("users")
                                    .whereEqualTo("mobile", mobileNumber)
                                    .get()
                                    .addOnCompleteListener { firestoreTask ->
                                        if (firestoreTask.isSuccessful && !firestoreTask.result.isEmpty) {
                                            val document = firestoreTask.result.documents[0]
                                            val savedPassword = document.getString("password")
                                            if (savedPassword == password) {
                                                // If FirebaseAuth currentUser is null, do a silent anonymous sign in
                                                if (auth.currentUser == null) {
                                                    auth.signInAnonymously().addOnCompleteListener { anonTask ->
                                                        isLoading = false
                                                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess()
                                                    }
                                                } else {
                                                    isLoading = false
                                                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Incorrect Password!", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(
                                                context, 
                                                "Login failed: ${task.exception?.localizedMessage ?: "User not found"}", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = inputShape,
            enabled = !isLoading && mobileNumber.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Login", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Sign Up",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }
    }
}

@Composable
fun SignUpScreen(onNavigateToLogin: () -> Unit, onSignUpSuccess: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val inputModifier = Modifier
        .fillMaxWidth()
    val inputShape = RoundedCornerShape(8.dp)

    val isFormValid = firstName.isNotBlank() && lastName.isNotBlank() && mobileNumber.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank() && referralCode.isNotBlank() &&
            password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign up to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name *") },
                modifier = Modifier.weight(1f),
                shape = inputShape,
                singleLine = true
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name *") },
                modifier = Modifier.weight(1f),
                shape = inputShape,
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number *") },
            modifier = inputModifier,
            shape = inputShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password *") },
            modifier = inputModifier,
            shape = inputShape,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password *") },
            modifier = inputModifier,
            shape = inputShape,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPassword.isNotBlank() && password != confirmPassword
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = referralCode,
            onValueChange = { referralCode = it },
            label = { Text("Referral Code *") },
            modifier = inputModifier,
            shape = inputShape,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    isLoading = true
                    val email = if (mobileNumber.contains("@")) mobileNumber else "$mobileNumber@user.com"
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = task.result?.user?.uid ?: ""
                                val userMap = hashMapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "mobile" to mobileNumber,
                                    "password" to password,
                                    "referralCode" to referralCode,
                                    "balance" to 0.0
                                )
                                db.collection("users").document(uid).set(userMap)
                                    .addOnCompleteListener { dbTask ->
                                        isLoading = false
                                        Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                        onSignUpSuccess()
                                    }
                            } else {
                                // Fallback: try checking if already registered or register locally via anonymous/custom UID
                                db.collection("users")
                                    .whereEqualTo("mobile", mobileNumber)
                                    .get()
                                    .addOnCompleteListener { searchTask ->
                                        if (searchTask.isSuccessful && !searchTask.result.isEmpty) {
                                            isLoading = false
                                            Toast.makeText(context, "Mobile number already registered!", Toast.LENGTH_LONG).show()
                                        } else {
                                            // Register using anonymous auth or generate a unique ID
                                            auth.signInAnonymously().addOnCompleteListener { anonTask ->
                                                val finalUid = if (anonTask.isSuccessful) {
                                                    anonTask.result?.user?.uid ?: java.util.UUID.randomUUID().toString()
                                                } else {
                                                    java.util.UUID.randomUUID().toString()
                                                }
                                                val userMap = hashMapOf(
                                                    "firstName" to firstName,
                                                    "lastName" to lastName,
                                                    "mobile" to mobileNumber,
                                                    "password" to password,
                                                    "referralCode" to referralCode,
                                                    "balance" to 0.0
                                                )
                                                db.collection("users").document(finalUid).set(userMap)
                                                    .addOnCompleteListener { saveTask ->
                                                        isLoading = false
                                                        if (saveTask.isSuccessful) {
                                                            Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                                            onSignUpSuccess()
                                                        } else {
                                                            Toast.makeText(context, "Sign up failed: ${saveTask.exception?.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                            }
                                        }
                                    }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = inputShape,
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Login",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
