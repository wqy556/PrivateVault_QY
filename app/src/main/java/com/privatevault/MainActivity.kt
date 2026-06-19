package com.privatevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.privatevault.data.RoomVaultStore
import com.privatevault.data.VaultDatabase
import com.privatevault.data.VaultRepository
import com.privatevault.ui.PrivateVaultApp
import com.privatevault.ui.theme.PrivateVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val repository = remember {
                val database = VaultDatabase.open(applicationContext)
                VaultRepository(RoomVaultStore(database.vaultDao()))
            }
            LaunchedEffect(Unit) {
                repository.seedSampleDataIfEmpty()
            }
            PrivateVaultTheme {
                PrivateVaultApp(repository = repository)
            }
        }
    }
}
