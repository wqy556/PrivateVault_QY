package com.privatevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.privatevault.ui.PrivateVaultApp
import com.privatevault.ui.theme.PrivateVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivateVaultTheme {
                PrivateVaultApp()
            }
        }
    }
}
