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
                val trimmedMobile = mobileNumber.trim()
                val trimmedPassword = password.trim()
                if (trimmedMobile.isNotBlank() && trimmedPassword.isNotBlank()) {
                    isLoading = true
                    val email = if (trimmedMobile.contains("@")) trimmedMobile else "$trimmedMobile@user.com"
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    
                    val securePassword = if (trimmedPassword.length < 6) trimmedPassword.padEnd(6, '0') else trimmedPassword
                    auth.signInWithEmailAndPassword(email, securePassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: ""
                                UserSession.saveSession(context, uid)
                                isLoading = false
                                Toast.makeText(context, "লগইন সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                // Try fallback check in Firestore users collection
                                db.collection("users")
                                    .whereEqualTo("mobile", trimmedMobile)
                                    .get()
                                    .addOnCompleteListener { firestoreTask ->
                                        if (firestoreTask.isSuccessful && !firestoreTask.result.isEmpty) {
                                            val document = firestoreTask.result.documents[0]
                                            val savedPassword = document.getString("password")?.trim() ?: ""
                                            if (savedPassword == trimmedPassword) {
                                                // If FirebaseAuth currentUser is null, do a silent anonymous sign in
                                                if (auth.currentUser == null) {
                                                    auth.signInAnonymously().addOnCompleteListener { anonTask ->
                                                        val finalUid = document.id
                                                        UserSession.saveSession(context, finalUid)
                                                        isLoading = false
                                                        Toast.makeText(context, "লগইন সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess()
                                                    }
                                                } else {
                                                    val finalUid = document.id
                                                    UserSession.saveSession(context, finalUid)
                                                    isLoading = false
                                                    Toast.makeText(context, "লগইন সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "ভুল পাসওয়ার্ড! অনুগ্রহ করে আবার চেষ্টা করুন।", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            isLoading = false
                                            val errorMsg = task.exception?.localizedMessage ?: "অ্যাকাউন্ট পাওয়া যায়নি"
                                            Toast.makeText(
                                                context, 
                                                "লগইন ব্যর্থ: $errorMsg", 
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
            enabled = !isLoading && mobileNumber.trim().isNotBlank() && password.trim().isNotBlank()
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
            password.isNotBlank() && confirmPassword.isNotBlank() &&
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
            label = { Text("Referral Code (যদি থাকে / ঐচ্ছিক)") },
            modifier = inputModifier,
            shape = inputShape,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val trimmedFirstName = firstName.trim()
                val trimmedLastName = lastName.trim()
                val trimmedMobile = mobileNumber.trim()
                val trimmedPassword = password.trim()
                val trimmedConfirm = confirmPassword.trim()

                if (trimmedFirstName.isNotBlank() && trimmedLastName.isNotBlank() && 
                    trimmedMobile.isNotBlank() && trimmedPassword.isNotBlank() && 
                    trimmedConfirm.isNotBlank() && trimmedPassword == trimmedConfirm) {
                    
                    isLoading = true
                    val email = if (trimmedMobile.contains("@")) trimmedMobile else "$trimmedMobile@user.com"
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val enteredReferral = referralCode.trim().uppercase()

                    // Function to design real registration save logic to avoid duplication
                    fun proceedWithRegister(referrerUid: String?) {
                        val securePassword = if (trimmedPassword.length < 6) trimmedPassword.padEnd(6, '0') else trimmedPassword
                        auth.createUserWithEmailAndPassword(email, securePassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result?.user?.uid ?: ""
                                    val generatedMyReferral = (1..8).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
                                    val userMap = hashMapOf(
                                        "firstName" to trimmedFirstName,
                                        "lastName" to trimmedLastName,
                                        "mobile" to trimmedMobile,
                                        "password" to trimmedPassword,
                                        "referralCode" to enteredReferral,
                                        "myReferralCode" to generatedMyReferral,
                                        "balance" to 0.0
                                    )
                                    db.collection("users").document(uid).set(userMap)
                                        .addOnCompleteListener { dbTask ->
                                            if (dbTask.isSuccessful) {
                                                // If there's a referrer, apply the referral bonus dynamically
                                                if (referrerUid != null) {
                                                    db.collection("settings").document("referral").get()
                                                        .addOnCompleteListener { settingTask ->
                                                            var bonusAmount = 10.0 // Default 10 Taka
                                                            var isReferEnabled = true
                                                            if (settingTask.isSuccessful && settingTask.result.exists()) {
                                                                bonusAmount = when (val value = settingTask.result.get("bonus_amount")) {
                                                                    is Number -> value.toDouble()
                                                                    is String -> value.toDoubleOrNull() ?: 10.0
                                                                    else -> 10.0
                                                                }
                                                                isReferEnabled = settingTask.result.getBoolean("is_enabled") ?: true
                                                            }
                                                            
                                                            if (isReferEnabled) {
                                                                // Credit the referrer
                                                                val referrerRef = db.collection("users").document(referrerUid)
                                                                db.runTransaction { tx ->
                                                                    val refSnap = tx.get(referrerRef)
                                                                    val currentRefBalance = when (val bal = refSnap.get("balance")) {
                                                                        is Number -> bal.toDouble()
                                                                        is String -> bal.toDoubleOrNull() ?: 0.0
                                                                        else -> 0.0
                                                                    }
                                                                    tx.update(referrerRef, "balance", currentRefBalance + bonusAmount)
                                                                }.addOnCompleteListener { txTask ->
                                                                    // Write notification for the referrer
                                                                    val notifRef = db.collection("notifications").document()
                                                                    val notifMap = hashMapOf(
                                                                        "title" to "রেফার বোনাস যোগ হয়েছে! 🎁",
                                                                        "message" to "আপনার রেফার কোড ব্যবহার করে $trimmedFirstName অ্যাকাউন্ট খোলার জন্য আপনি ৳$bonusAmount বোনাস পেয়েছেন।",
                                                                        "timestamp" to System.currentTimeMillis(),
                                                                        "userId" to referrerUid
                                                                    )
                                                                    notifRef.set(notifMap)
                                                                }
                                                            }
                                                        }
                                                }
                                                UserSession.saveSession(context, uid)
                                                isLoading = false
                                                Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                                onSignUpSuccess()
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Error: ${dbTask.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    // Fallback: try checking if already registered or register locally via anonymous/custom UID
                                    db.collection("users")
                                        .whereEqualTo("mobile", trimmedMobile)
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
                                                    val generatedMyReferral = (1..8).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
                                                    val userMap = hashMapOf(
                                                        "firstName" to trimmedFirstName,
                                                        "lastName" to trimmedLastName,
                                                        "mobile" to trimmedMobile,
                                                        "password" to trimmedPassword,
                                                        "referralCode" to enteredReferral,
                                                        "myReferralCode" to generatedMyReferral,
                                                        "balance" to 0.0
                                                    )
                                                    db.collection("users").document(finalUid).set(userMap)
                                                        .addOnCompleteListener { saveTask ->
                                                            if (saveTask.isSuccessful) {
                                                                if (referrerUid != null) {
                                                                    db.collection("settings").document("referral").get()
                                                                        .addOnCompleteListener { settingTask ->
                                                                            var bonusAmount = 10.0
                                                                            var isReferEnabled = true
                                                                            if (settingTask.isSuccessful && settingTask.result.exists()) {
                                                                                bonusAmount = when (val value = settingTask.result.get("bonus_amount")) {
                                                                                    is Number -> value.toDouble()
                                                                                    is String -> value.toDoubleOrNull() ?: 10.0
                                                                                    else -> 10.0
                                                                                }
                                                                                isReferEnabled = settingTask.result.getBoolean("is_enabled") ?: true
                                                                            }
                                                                            
                                                                            if (isReferEnabled) {
                                                                                val referrerRef = db.collection("users").document(referrerUid)
                                                                                db.runTransaction { tx ->
                                                                                    val refSnap = tx.get(referrerRef)
                                                                                    val currentRefBalance = when (val bal = refSnap.get("balance")) {
                                                                                        is Number -> bal.toDouble()
                                                                                        is String -> bal.toDoubleOrNull() ?: 0.0
                                                                                        else -> 0.0
                                                                                    }
                                                                                    tx.update(referrerRef, "balance", currentRefBalance + bonusAmount)
                                                                                }.addOnCompleteListener { txTask ->
                                                                                    val notifRef = db.collection("notifications").document()
                                                                                    val notifMap = hashMapOf(
                                                                                        "title" to "রেফার বোনাস যোগ হয়েছে! 🎁",
                                                                                        "message" to "আপনার রেফার কোড ব্যবহার করে $trimmedFirstName অ্যাকাউন্ট খোলার জন্য আপনি ৳$bonusAmount বোনাস পেয়েছেন।",
                                                                                        "timestamp" to System.currentTimeMillis(),
                                                                                        "userId" to referrerUid
                                                                                    )
                                                                                    notifRef.set(notifMap)
                                                                                }
                                                                            }
                                                                        }
                                                                }
                                                                UserSession.saveSession(context, finalUid)
                                                                isLoading = false
                                                                Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                                                onSignUpSuccess()
                                                            } else {
                                                                isLoading = false
                                                                Toast.makeText(context, "Sign up failed: ${saveTask.exception?.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                }
                            }
                    }

                    // Validate referral code first if not empty
                    if (enteredReferral.isNotBlank()) {
                        db.collection("users")
                            .whereEqualTo("myReferralCode", enteredReferral)
                            .get()
                            .addOnCompleteListener { referralQueryTask ->
                                if (referralQueryTask.isSuccessful && !referralQueryTask.result.isEmpty) {
                                    val referrerUid = referralQueryTask.result.documents[0].id
                                    proceedWithRegister(referrerUid)
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "আমন্ত্রিত রেফার কোডটি সঠিক নয়! ❌", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        proceedWithRegister(null)
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
