package com.example.progresswidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.progresswidget.ui.BatteryTrackerScreen
import com.example.progresswidget.ui.ConfigScreen
import com.example.progresswidget.ui.HomeScreen
import com.example.progresswidget.service.BatteryAutomationService
import android.content.Intent
import android.os.Build

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        startBatteryService()

        val launchedFromWidget = intent?.getBooleanExtra("fromWidget", false) ?: false
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (launchedFromWidget) "config" else "home"
                    
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToTracker = { navController.navigate("config") },
                                onNavigateToBattery = { navController.navigate("battery") }
                            )
                        }
                        composable("config") {
                            ConfigScreen(this@MainActivity)
                        }
                        composable("battery") {
                            BatteryTrackerScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    private fun startBatteryService() {
        val intent = Intent(this, BatteryAutomationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
