package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FullScreenDialogModifier() {
    val dialogView = LocalView.current
    SideEffect {
        val window = (dialogView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.let {
            val insetsController = WindowCompat.getInsetsController(it, dialogView)
            
            // Light status bars = true makes the icons BLACK (for WHITE background)
            insetsController.isAppearanceLightStatusBars = true 
            // Light nav bars = true makes the icons BLACK (for WHITE background)
            insetsController.isAppearanceLightNavigationBars = true 
            
            it.statusBarColor = android.graphics.Color.WHITE
            it.navigationBarColor = android.graphics.Color.WHITE
        }
    }
}


