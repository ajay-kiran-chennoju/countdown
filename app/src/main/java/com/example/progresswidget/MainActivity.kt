package com.example.progresswidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ConfigScreen(this)
                }
            }
        }
    }
}

@Composable
fun ConfigScreen(context: Context) {
    val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
    var totalDays by remember { mutableStateOf(prefs.getInt("totalDays", 365).toString()) }
    var currentDay by remember { mutableStateOf(prefs.getInt("currentDay", 120).toString()) }
    var colorA by remember { mutableStateOf(prefs.getString("colorA", "#E0E0E0") ?: "#E0E0E0") }
    var colorB by remember { mutableStateOf(prefs.getString("colorB", "#FF5252") ?: "#FF5252") }
    
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Widget Configuration", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = totalDays,
            onValueChange = { totalDays = it },
            label = { Text("Total Days") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = currentDay,
            onValueChange = { currentDay = it },
            label = { Text("Current Day") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = colorA,
            onValueChange = { colorA = it },
            label = { Text("Color A (HEX)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = colorB,
            onValueChange = { colorB = it },
            label = { Text("Color B (HEX)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                prefs.edit()
                    .putInt("totalDays", totalDays.toIntOrNull() ?: 365)
                    .putInt("currentDay", currentDay.toIntOrNull() ?: 120)
                    .putString("colorA", colorA)
                    .putString("colorB", colorB)
                    .apply()
                    
                coroutineScope.launch {
                    ProgressWidget().updateAll(context)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save & Update Widget")
        }
    }
}
