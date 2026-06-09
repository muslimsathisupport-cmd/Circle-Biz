package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MobileAds.initialize(this) {}
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var isLoggedIn by remember { mutableStateOf(false) }
        
        if (isLoggedIn) {
          MainScreen()
        } else {
          AuthScreen(onLoginSuccess = { isLoggedIn = true })
        }
      }
    }
  }
}
